Installation
=============

Create database
---------------------

create database tpg;
create user tpg login nosuperuser inherit nocreatedb nocreaterole;
alter user tpg password '**********';
alter database tpg owner to tpg;
\c tpg
create extension postgis;
create extension hstore;

Create osm and jpa schemas
----------------------------------------

psql -h localhost -U tpg -p54322 -c "create schema osm"; 
psql -h localhost -U tpg -p54322 -c "create schema app";

Launch app to create stops
----------------------------

OSM Data download
--------------------

wget 'http://overpass-api.de/api/map?bbox=5.9627,46.2145,6.1287,46.2782' -O ligne-y.osm.xml

Postgis loading
--------------------

osm2pgsql --prefix '' --host localhost --port 54322 --username tpg --password --create --database tpg ligne-y.osm.xml

Set primary keys and schema to OSM tables
-----------------------------------------

ALTER TABLE osm_point ADD COLUMN fid serial;
ALTER TABLE osm_line ADD COLUMN fid serial;
ALTER TABLE osm_polygon ADD COLUMN fid serial;
ALTER TABLE osm_roads ADD COLUMN fid serial;

ALTER TABLE osm_point SET SCHEMA osm;
ALTER TABLE osm_line SET SCHEMA osm;
ALTER TABLE osm_polygon SET SCHEMA osm;
ALTER TABLE osm_roads SET SCHEMA osm;

Roads layer
------------

create table app.osm_roads as
	select
		*
	from
		osm_line 
	where
		highway in
			(
				'motorway','trunk','primary','secondary','tertiary', 'unclassified','residential',
				'service','motorway_link','trunk_link','primary_link','secondary_link','tertiary_link'
			);
create index on app.osm_roads (osm_id);

Transport network
-------------------

create or replace view osmtransport as select * from osm_line where operator in ('RDTA', 'TPG');

Shift + OSMShift join
------------------------

create or replace view app.osmshiftinfo as 
	select 
		osmid, forward, speed, timestamp, vehicleid, startpoint, endpoint
	from
		app.osmshift osms, app.shift s
	where
		s.id=osms.shift_id;

Last speed info
------------------
		
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

navigable osm speeds
----------------------

-- Create a table adding to the osmshiftinfo a timestamp for drawing

create table app.timestamped_osmshiftinfo as
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


Geoserver
----------

# limitar el uso de CRS

Separados por comas y sin el prefijo "EPSG:"

# estilo

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
