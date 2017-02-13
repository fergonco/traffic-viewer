# Build

mvn package
docker build . -t fergonco/traffic-viewer
docker push fergonco/traffic-viewer

# Development

Application is accessed through nginx:

http://localhost/border-rampage

Nginx needs to know host address in order to access through port 80, which is necessary since GeoServer is supposed to be on the same server as the application. Therefore, start with this command:

	docker run --name nginx --link gs:gs --add-host="host:172.17.0.1" -p 80:80 -v /var/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro -d nginx:alpine

and replace 172.17.0.1 with the IP of the host in the docker network. Then include in /var/nginx/default.conf:

    location /border-rampage {
        proxy_pass http://host:8080/traffic-viewer/;
    }

# Deploy

Prerequisites:

* nginx docker mapping /geoserver to a geoserver docker instance
* geoserver docker instance with access to postgresql docker instance
	docker run -d -p 6305:8080 -v /var/geoserver:/var/local/geoserver --name gs --link pg:pg oscarfonts/geoserver
* postgresql/postgis docker instance
	docker run -p54322:5432 -d -t -v /app-conf/postgresql/:/var/lib/postgresql -e POSTGRES_USER=geomatico -e POSTGRES_PASS= --name pg kartoza/postgis:9.3-2.1
* traffic-viewer instance

## Create database

create database tpg;
create user tpg login nosuperuser inherit nocreatedb nocreaterole;
alter user tpg password '**********';
alter database tpg owner to tpg;
\c tpg
create extension postgis;
create extension hstore;

## Create osm and jpa schemas

psql -h localhost -U tpg -p54322 -c "create schema osm"; 
psql -h localhost -U tpg -p54322 -c "create schema app";

## OSM Data download

wget 'http://overpass-api.de/api/map?bbox=5.9627,46.2145,6.1287,46.2782' -O ligne-y.osm.xml

## Postgis loading

osm2pgsql --prefix 'osm' --host localhost --port 54322 --username tpg --password --create --database tpg ligne-y.osm.xml

## Set primary keys and schema to OSM tables

ALTER TABLE osm_point ADD COLUMN fid serial;
ALTER TABLE osm_line ADD COLUMN fid serial;
ALTER TABLE osm_polygon ADD COLUMN fid serial;
ALTER TABLE osm_roads ADD COLUMN fid serial;

ALTER TABLE osm_point SET SCHEMA osm;
ALTER TABLE osm_line SET SCHEMA osm;
ALTER TABLE osm_polygon SET SCHEMA osm;
ALTER TABLE osm_roads SET SCHEMA osm;

## Roads layer

create table app.osm_roads as
	select
		*
	from
		osm.osm_line 
	where
		highway in
			(
				'motorway','trunk','primary','secondary','tertiary', 'unclassified','residential',
				'service','motorway_link','trunk_link','primary_link','secondary_link','tertiary_link'
			);
create index on osm.osm_roads (osm_id);

## Launch app to create stops and other tables

## Transport network (not in use)

create or replace view osmtransport as select * from osm_line where operator in ('RDTA', 'TPG');

## Shift + OSMShift join

create or replace view app.osmshiftinfo as 
	select 
		osmid, forward, speed, timestamp, vehicleid, startpoint, endpoint
	from
		app.osmshift osms, app.shift s
	where
		s.id=osms.shift_id;

## Last speed info (not in use)
		
create or replace view app.osmshiftlastinfo as
	select
		a.*
	from
		app.osmshiftinfo a 
	left outer join
		app.osmshiftinfo b
	on
		(a.osmid=b.osmid and a.timestamp<b.timestamp and a.forward=b.forward)
	where
		b.osmid is null;
		
create or replace view app.osm_speeds as 
	select
		osmid, forward, speed, timestamp, vehicleid, startpoint, endpoint, way 
	from
		(
			select
				*
			from
				osm_line 
			where
				highway in
					(
						'motorway','trunk','primary','secondary','tertiary', 'unclassified','residential',
						'service','motorway_link','trunk_link','primary_link','secondary_link','tertiary_link'
					)
		) as osm
	left outer join
		app.osmshiftlastinfo 
	on 
		(app.osmshiftlastinfo.osmid=osm.osm_id);

## navigable osm speeds

-- Create a table adding to the osmshiftinfo a timestamp for drawing

create materialized view app.timestamped_osmshiftinfo as
-- create table app.timestamped_osmshiftinfo as
	select 
		to_timestamp(a.timestamp/1000) as draw_timestamp, b.*
	from 
		(select distinct timestamp from app.osmshiftinfo) a,
		app.osmshiftinfo b
	where
		a.timestamp >= b.timestamp
		and
		not exists (
			select
				1
			from
				app.osmshiftinfo c 
			where 
				b.osmid = c.osmid
				and
				b.forward = c.forward
				and
				a.timestamp > c.timestamp
				and
				c.timestamp > b.timestamp
		);
create index ON app.timestamped_osmshiftinfo (osmid);
create index ON app.timestamped_osmshiftinfo (draw_timestamp);

-- Create a table to add the geometry

create or replace view app.timestamped_osm_speeds as 
	select
		osmid, forward, speed, draw_timestamp, timestamp, vehicleid, way 
	from
		app.osm_roads osm,
		app.timestamped_osmshiftinfo osmshift 
	where
		osmshift.osmid=osm.osm_id;

-- refresh the materialized view
refresh materialized view app.timestamped_osmshiftinfo ;

## Geoserver

### limitar el uso de CRS

Separados por comas y sin el prefijo "EPSG:"

### estilo

	<?xml version="1.0" encoding="ISO-8859-1"?>
	<StyledLayerDescriptor version="1.0.0" 
	                       xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
	                       xmlns="http://www.opengis.net/sld" 
	                       xmlns:ogc="http://www.opengis.net/ogc" 
	                       xmlns:xlink="http://www.w3.org/1999/xlink" 
	                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	  <!-- a named layer is the basic building block of an sld document -->
	
	  <NamedLayer>
	    <Name>Default Line</Name>
	    <UserStyle>
	      <!-- they have names, titles and abstracts -->
	
	      <Title>A boring default style</Title>
	      <Abstract>A sample style that just prints out a green line</Abstract>
	      <!-- FeatureTypeStyles describe how to render different features -->
	      <!-- a feature type for lines -->
	
	      <FeatureTypeStyle>
<!-- 	        <Rule> -->
<!-- 	          <Name>Rule all</Name> -->
<!-- 	          <Title>Green Line</Title> -->
<!-- 	          <LineSymbolizer> -->
<!-- 	            <Geometry> -->
<!-- 	              <ogc:PropertyName>way</ogc:PropertyName> -->
<!-- 	            </Geometry>      -->
<!-- 	            <Stroke> -->
<!-- 	              <CssParameter name="stroke">#000000</CssParameter> -->
<!-- 	              <CssParameter name="stroke-width">1</CssParameter> -->
<!-- 	            </Stroke> -->
<!-- 	          </LineSymbolizer> -->
<!-- 	        </Rule> -->
	
	
	        <Rule>
	          <Name>Rule_15_f</Name>
	          <Title>[0, 15[ forward</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsLessThan>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>15</ogc:Literal>
	              </ogc:PropertyIsLessThan>
	              <ogc:PropertyIsEqualTo>
	                <ogc:PropertyName>forward</ogc:PropertyName>
	                <ogc:Literal>true</ogc:Literal>
	              </ogc:PropertyIsEqualTo>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>way</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#FF0000</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	        <Rule>
	          <Name>Rule_15_fb</Name>
	          <Title>[0, 15[ backward</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsLessThan>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>15</ogc:Literal>
	              </ogc:PropertyIsLessThan>
	              <ogc:PropertyIsEqualTo>
	                <ogc:PropertyName>forward</ogc:PropertyName>
	                <ogc:Literal>false</ogc:Literal>
	              </ogc:PropertyIsEqualTo>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>way</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#FF0000</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>-4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	
	        <Rule>
	          <Name>Rule15_40F</Name>
	          <Title>[15, 40[ forward</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>15</ogc:Literal>
	              </ogc:PropertyIsGreaterThanOrEqualTo>
	              <ogc:PropertyIsLessThan>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>40</ogc:Literal>
	              </ogc:PropertyIsLessThan>
	              <ogc:PropertyIsEqualTo>
	                <ogc:PropertyName>forward</ogc:PropertyName>
	                <ogc:Literal>true</ogc:Literal>
	              </ogc:PropertyIsEqualTo>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>way</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#FFA500</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	        <Rule>
	          <Name>Rule 15_40B</Name>
	          <Title>[15, 40[ backward</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>15</ogc:Literal>
	              </ogc:PropertyIsGreaterThanOrEqualTo>
	              <ogc:PropertyIsLessThan>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>40</ogc:Literal>
	              </ogc:PropertyIsLessThan>
	              <ogc:PropertyIsEqualTo>
	                <ogc:PropertyName>forward</ogc:PropertyName>
	                <ogc:Literal>false</ogc:Literal>
	              </ogc:PropertyIsEqualTo>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>way</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#FFA500</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>-4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	
	        <Rule>
	          <Name>Rule_40F</Name>
	          <Title>[40, Inf[ forward</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>40</ogc:Literal>
	              </ogc:PropertyIsGreaterThanOrEqualTo>
	              <ogc:PropertyIsEqualTo>
	                <ogc:PropertyName>forward</ogc:PropertyName>
	                <ogc:Literal>true</ogc:Literal>
	              </ogc:PropertyIsEqualTo>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>way</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#0000FF</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	        <Rule>
	          <Name>Rule_40B</Name>
	          <Title>[40, Inf[ backward</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>40</ogc:Literal>
	              </ogc:PropertyIsGreaterThanOrEqualTo>
	              <ogc:PropertyIsEqualTo>
	                <ogc:PropertyName>forward</ogc:PropertyName>
	                <ogc:Literal>false</ogc:Literal>
	              </ogc:PropertyIsEqualTo>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>way</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#0000FF</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>-4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	      </FeatureTypeStyle>
	    </UserStyle>
	  </NamedLayer>
	</StyledLayerDescriptor>
