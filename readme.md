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

# Data preparation

* Generate reference table tpgstops with TPGStopExtractor, getting coordinates from TPG API
* Visualize generated stops with QGIS project in data-gatherer/network/qgis.qgs
* Download OSM data. E.g.: http://overpass-api.de/api/map?bbox=5.9697,46.2026,6.1551,46.3405
* Execute BusLineExtractor to generate tables: osmlines, osmlinenodes and osmstops
* Check these tables contains osm nodes for stops in both directions and the line network

    * It is possible to check in OSM: Select "transport map -> map data" to obtain nodes osm_ids
    * reference table tpgstops
    * in tph.ch it is possible to get the position of the stop in thermometer: "Afficher la position de l'arrêt sur une carte"

* Add manually the stops to app.tpgstop2
* LineRouter use in order to calculate the routes between the stops. A txt file has to be generated with this format:

        name:F # matches line in app.tpgstop2
        forward:GEX # matches destination in app.tpgstop2
        backward:GARE CORNAVIN # matches destination in app.tpgstop2
        TPG stop codes in forward order

  * If a step is not calculated it is most probably due to one-way=yes. Modify it in osm-overrides.xml
* Ejecutar DistanceCalculator para generar las tablas TPGStopRoute y TPGStopRouteSegments

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

insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 792507552, 'GXAI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 983750310, 'GXGC');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 792507541, 'GXPO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 792508180, 'VRTC');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1006118091, 'TGIN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1006118131, 'LHCY');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 983749484, 'CYNT');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1006118105, 'SGNY');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1006118157, 'MCON');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1006118146, 'OXFR');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1114944131, 'PPLA');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', -3, 'ORRO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 1006118191, 'CHVO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482153, 'FEMA');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482206, 'AJUR');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482201, 'JAGI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482240, 'BRUN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482246, 'FVDO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482284, 'GSDN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 858297354, 'TRTI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 864555974, 'PR47');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 864555979, 'SUSE');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 528517474, 'GSAC');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482141, 'POMI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498714, 'MORL');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 936369115, 'OMS0');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498709, 'BIT0');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 856482086, 'APIA');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498773, 'NATI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498766, 'VRBE');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498760, 'VLAI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498763, 'POST');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GARE CORNAVIN', 'F', 768498764, 'CVIN');

insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 2976733717, 'CVIN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498761, 'POST');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 844755267, 'VLAI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498769, 'VRBE');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498772, 'NATI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498728, 'APIA');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 923961689, 'BIT0');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 448298236, 'OMS0');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498717, 'MORL');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 528517472, 'POMI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 856482169, 'GSAC');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498690, 'SUSE');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498691, 'PR47');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498663, 'TRTI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 768498669, 'GSDN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 856482250, 'FVDO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 856482238, 'BRUN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 856482185, 'JAGI');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 856482209, 'AJUR');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 856482153, 'FEMA');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 983750445, 'CHVO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', -2, 'ORRO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 1006118198, 'PPLA');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 289982136, 'OXFR');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 983750372, 'MCON');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 983749436, 'SGNY');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 1006118124, 'CYNT');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 983750406, 'LHCY');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 1006118085, 'TGIN');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 792508187, 'VRTC');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 792507546, 'GXPO');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 1006118064, 'GXGC');
insert into app.tpgstop2 (destination, line, osmid, tpgcode) values('GEX', 'F', 2080973355, 'GXAI');


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
			timestamp > (
				extract ( 
					epoch from localtimestamp
				)*1000 
				-
				24*60*60*1000
			) and 
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

	-- Create a table adding to the osmshiftinfo a timestamp for drawing
	create materialized view app.timestamped_osmshiftinfo as
		select 
			to_timestamp(timestamps.millis/1000) as draw_timestamp, osmshift.*
		from 
			app.timestamps timestamps,
			app.osmshiftinfo osmshift
		where
			timestamps.millis >= osmshift.timestamp
			and
			not exists (
				select
					1
				from
					app.osmshiftinfo osmshift2 
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
	          <Name>Rule_20</Name>
	          <Title>[0, 20[</Title>
	          <ogc:Filter>
	            <ogc:PropertyIsLessThan>
	              <ogc:PropertyName>speed</ogc:PropertyName>
	              <ogc:Literal>20</ogc:Literal>
	            </ogc:PropertyIsLessThan>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>geom</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#ff0000</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	
	        <Rule>
	          <Name>Rule20_30</Name>
	          <Title>[20, 30[</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>20</ogc:Literal>
	              </ogc:PropertyIsGreaterThanOrEqualTo>
	              <ogc:PropertyIsLessThan>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>30</ogc:Literal>
	              </ogc:PropertyIsLessThan>
	            </ogc:And>
	          </ogc:Filter>
	          <LineSymbolizer>
	            <Geometry>
	              <ogc:PropertyName>geom</ogc:PropertyName>
	            </Geometry>     
	            <Stroke>
	              <CssParameter name="stroke">#ffba00</CssParameter>
	              <CssParameter name="stroke-width">4</CssParameter>
	            </Stroke>
	            <PerpendicularOffset>4</PerpendicularOffset>
	          </LineSymbolizer>
	        </Rule>
	        
	        <Rule>
	          <Name>Rule30_40</Name>
	          <Title>[30, 40[</Title>
	          <ogc:Filter>
	            <ogc:And>
	              <ogc:PropertyIsGreaterThanOrEqualTo>
	                <ogc:PropertyName>speed</ogc:PropertyName>
	                <ogc:Literal>30</ogc:Literal>
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
	              <CssParameter name="stroke">#ffff00</CssParameter>
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
	              <CssParameter name="stroke">#00ff00</CssParameter>
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
	