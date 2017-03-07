# Build

	mvn package
	mkdockers.sh
	pushdockers.sh

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

## Stops info

	begin;
	create table app.tpgstop (code varchar primary key, osmid1 bigint, destination1 varchar, osmid2 bigint, destination2 varchar);
	insert into app.tpgstop values ('VATH', 1161861705, 'FERNEY-VOLTAIRE', 1161866554, 'VAL-THOIRY');
	insert into app.tpgstop values ('THGA', 1163093869, 'FERNEY-VOLTAIRE', 1163095721, 'VAL-THOIRY');
	insert into app.tpgstop values ('THMA', 1163096998, 'FERNEY-VOLTAIRE', 1163098489, 'VAL-THOIRY');
	insert into app.tpgstop values ('PAGN', 1163102046, 'FERNEY-VOLTAIRE', 1163103186, 'VAL-THOIRY');
	insert into app.tpgstop values ('POLT', 1163105001, 'FERNEY-VOLTAIRE', 1163105239, 'VAL-THOIRY');
	insert into app.tpgstop values ('SGBO', 1163107267, 'FERNEY-VOLTAIRE', 1163108665, 'VAL-THOIRY');
	insert into app.tpgstop values ('SGGA', 1163111715, 'FERNEY-VOLTAIRE', 1163110158, 'VAL-THOIRY');
	insert into app.tpgstop values ('JMON', 1056918692, 'FERNEY-VOLTAIRE', 1056918415, 'VAL-THOIRY');
	insert into app.tpgstop values ('CFUS', 1147990917, 'FERNEY-VOLTAIRE', 1147989553, 'VAL-THOIRY');
	insert into app.tpgstop values ('HTAI', 1147987261, 'FERNEY-VOLTAIRE', 1147988700, 'VAL-THOIRY');
	insert into app.tpgstop values ('PLIO', 1147984369, 'FERNEY-VOLTAIRE', 1147985005, 'VAL-THOIRY');
	insert into app.tpgstop values ('MLVT', 1147982433, 'FERNEY-VOLTAIRE', 1147980792, 'VAL-THOIRY');
	insert into app.tpgstop values ('SHUM', 2616483546, 'FERNEY-VOLTAIRE', 2616483546, 'VAL-THOIRY');
	insert into app.tpgstop values ('CERN', 1266403296, 'FERNEY-VOLTAIRE', 1550672551, 'VAL-THOIRY');
	insert into app.tpgstop values ('MAIX', 1266403290, 'FERNEY-VOLTAIRE', 1550672646, 'VAL-THOIRY');
	insert into app.tpgstop values ('HTOU', 849526630, 'FERNEY-VOLTAIRE', 849526627, 'VAL-THOIRY');
	insert into app.tpgstop values ('MAIL', 849526649, 'FERNEY-VOLTAIRE', 849526651, 'VAL-THOIRY');
	insert into app.tpgstop values ('VROT', 849526643, 'FERNEY-VOLTAIRE', 849526648, 'VAL-THOIRY');
	insert into app.tpgstop values ('PFON', 849526642, 'FERNEY-VOLTAIRE', 849526658, 'VAL-THOIRY');
	insert into app.tpgstop values ('LMLD', 849465151, 'FERNEY-VOLTAIRE', 849465150, 'VAL-THOIRY');
	insert into app.tpgstop values ('ZIMG', 60068512, 'FERNEY-VOLTAIRE', 60068512, 'VAL-THOIRY');
	insert into app.tpgstop values ('PRGM', 849526657, 'FERNEY-VOLTAIRE', 849526656, 'VAL-THOIRY');
	insert into app.tpgstop values ('SIGN', 858297454, 'FERNEY-VOLTAIRE', 858297452, 'VAL-THOIRY');
	insert into app.tpgstop values ('RENF', 858297423, 'FERNEY-VOLTAIRE', 858297427, 'VAL-THOIRY');
	insert into app.tpgstop values ('BLDO', 858297429, 'FERNEY-VOLTAIRE', 858297430, 'VAL-THOIRY');
	insert into app.tpgstop values ('GDHA', 858297435, 'FERNEY-VOLTAIRE', 1599295215, 'VAL-THOIRY');
	insert into app.tpgstop values ('ICC0', 1599295246, 'FERNEY-VOLTAIRE', 858297402, 'VAL-THOIRY');
	insert into app.tpgstop values ('TOCO', 767859762, 'FERNEY-VOLTAIRE', 858297405, 'VAL-THOIRY');
	insert into app.tpgstop values ('WTC0', 767859834, 'FERNEY-VOLTAIRE', 858297414, 'VAL-THOIRY');
	insert into app.tpgstop values ('AERO', 767859830, 'FERNEY-VOLTAIRE', 767859832, 'VAL-THOIRY');
	insert into app.tpgstop values ('AREN', 903992166, 'FERNEY-VOLTAIRE', 767859827, 'VAL-THOIRY');
	insert into app.tpgstop values ('PXPH', 767859911, 'FERNEY-VOLTAIRE', 767859914, 'VAL-THOIRY');
	insert into app.tpgstop values ('FRET', 858297375, 'FERNEY-VOLTAIRE', 858297383, 'VAL-THOIRY');
	insert into app.tpgstop values ('TRTI', 768498663, 'FERNEY-VOLTAIRE', 768498666, 'VAL-THOIRY');
	insert into app.tpgstop values ('GSDN', 768498669, 'FERNEY-VOLTAIRE', 856482284, 'VAL-THOIRY');
	insert into app.tpgstop values ('FVDO', 856482250, 'FERNEY-VOLTAIRE', 856482246, 'VAL-THOIRY');
	insert into app.tpgstop values ('BRUN', 856482238, 'FERNEY-VOLTAIRE', 856482240, 'VAL-THOIRY');
	insert into app.tpgstop values ('JAGI', 856482185, 'FERNEY-VOLTAIRE', 856482201, 'VAL-THOIRY');
	insert into app.tpgstop values ('AJUR', 856482209, 'FERNEY-VOLTAIRE', 856482206, 'VAL-THOIRY');
	insert into app.tpgstop values ('FEMA', 1186330722, 'FERNEY-VOLTAIRE', 1186330722, 'VAL-THOIRY');
	commit;

## Launch app to create JPA tables

## Transport network (not in use)

create or replace view osmtransport as select * from osm_line where operator in ('RDTA', 'TPG');

## Shift + OSMShift join

	create materialized view app.osmshiftinfo as 
		select 
			geom, startnode, endnode, speed, timestamp, vehicleid, startpoint, endpoint
		from
			app.osmshift osms, app.shift s
		where
			s.id=osms.shift_id;
	create index ON app.osmshiftinfo (timestamp);  

## navigable osm speeds

	create materialized view app.timestamps as
		select 
			generate_series(
				greatest(
					min("timestamp"),
					extract ( 
						epoch from localtimestamp
					)*1000 - 24*60*60*1000
				)::bigint,
				max("timestamp"),
				15*60*1000
			) as millis
		from app.shift;

	create materialized view app.recent_osmshiftinfo as
		select
			*
		from
			app.osmshiftinfo
		where
			timestamp > (
				extract ( 
					epoch from localtimestamp
				)*1000 
				-
				24*60*60*1000
			);
	create index ON app.recent_osmshiftinfo (timestamp);  
	

	-- Create a table adding to the osmshiftinfo a timestamp for drawing
	create materialized view app.timestamped_osmshiftinfo as
		select 
			to_timestamp(timestamps.millis/1000) as draw_timestamp, osmshift.*
		from 
			app.timestamps timestamps,
			app.recent_osmshiftinfo osmshift
		where
			timestamps.millis >= osmshift.timestamp
			and
			not exists (
				select
					1
				from
					app.recent_osmshiftinfo osmshift2 
				where 
					osmshift.startnode = osmshift2.startnode
					and
					osmshift.endnode = osmshift2.endnode
					and
					timestamps.millis > osmshift2.timestamp
					and
					osmshift2.timestamp > osmshift.timestamp
			);
	create index ON app.timestamped_osmshiftinfo (draw_timestamp);
	
	-- refresh the materialized view
	refresh materialized view app.timestamped_osmshiftinfo ;

## Geoserver

### limitar el uso de CRS

Separados por comas y sin el prefijo "EPSG:" : 

	Servicios > WMS > Lista de SRS limitada > 900913, 4326
	
### load app.osm\_roads and app.timestamped_osmshiftinfo 

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
	        <Rule>
	          <Name>Rule_15</Name>
	          <Title>[0, 15[</Title>
	          <ogc:Filter>
	            <ogc:PropertyIsLessThan>
	              <ogc:PropertyName>speed</ogc:PropertyName>
	              <ogc:Literal>15</ogc:Literal>
	            </ogc:PropertyIsLessThan>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>geom</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#FF0000</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	        <Rule>
	          <Name>Rule15_40</Name>
	          <Title>[15, 40[</Title>
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
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>geom</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#FFA500</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	        <Rule>
	          <Name>Rule_40</Name>
	          <Title>[40, Inf[</Title>
	          <ogc:Filter>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>speed</ogc:PropertyName>
                <ogc:Literal>40</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>geom</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#0000FF</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	      </FeatureTypeStyle>
	    </UserStyle>
	  </NamedLayer>
	</StyledLayerDescriptor>

## Monit

The service can be monitored in the /dbstatus/ url:

	check host border-rampage with address fergonco.org
	    if failed
	        port 80 protocol http
	        request "/dbstatus/" with content = "weather: success\ntransport: success"
	    then alert


	