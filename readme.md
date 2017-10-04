Several projects to make this site work: http://fergonco.org/border-rampage/

* data-gatherer: process that queries TPG API (http://tpg.ch) and OpenWeatherMap API (http://openweathermap.org/) and saves the data on the database  
* db-status: web service that shows if data is being gathered or not
* jpa: JPA entities used by data-gatherer and analyzer to interface with the database
* viewer: web application showing the map and the time navigator
* analyzer: Contains stuff to prepare the data and a R project that creates some regression models to predict traffic speed

# 1 Build

The project can be built with Maven:

	mvn install

Probably you want to disable tests, which are not unit but functional and try to do some database connections, among other things:

	mvn install -DskipTests=true

The .war files generated in the previous step can be packed as docker images using `mkdockers.sh`

# 2 Deploy data gathering services

## 2.1 PostgreSQL/PostGIS

We use the [image created by kartoza](https://hub.docker.com/r/kartoza/postgis/).

1. Create a folder in the server where the postgresql data is to be held
2. Launch a container based on the image

### Create database

1. Connect to the database with PostgreSQL superuser
2. Execute

	create database tpg;
	create user tpg login nosuperuser inherit nocreatedb nocreaterole;
	alter user tpg password '**********';
	alter database tpg owner to tpg;
	\c tpg
	create extension postgis;
	create extension hstore;
3. Close connection

## 2.2 Create database model

In the database, as tpg user:

	psql -h localhost -U tpg -d tpg -p <port> -c "create schema app;"
	psql -h localhost -U tpg -d tpg -p <port> -f jpa/db.sql

## 2.3 Data-gatherer

All docker images accessing the database, like `data-gatherer`, take into account the following environment variables:

* TRAFFIC\_VIEWER\_DB\_URL=<jdbc connection string>
* TRAFFIC\_VIEWER\_DB\_USER=<user name to connect to the database>
* TRAFFIC\_VIEWER\_DB\_PASSWORD=<password to authenticate user>
* TRAFFIC\_VIEWER\_JPA\_LOG\_LEVEL=<value for `eclipselink.logging.level` property>

Execute a docker container with the data-gatherer image created before using the previous environment variables to point to the PostgreSQL/PostGIS container.

A volume can be mounted in `/var/traffic-viewer` in order to get a file with the log.

See logs to check it is working.

## 2.4 dbstatus

Like `data-gatherer`, this image accesses the database.

Execute a docker container with the data-gatherer image created before using the previous environment variables to point to the PostgreSQL/PostGIS container. Map port to 8080 in order to access the service.

Navigate to `http://<server>:<mappedport>/dbstatus` to check if data is being collected or not. 

# 3 Deploy viewer

## 3.1 Views

In the database, as tpg user:

	-- Shift + OSMSegment join
	create materialized view app.geoshift as 
		select 
			osm.geom, osm.startnode, osm.endnode, s.*, r.distance / (s.seconds / (60.0*60)) as speed
		from
			app.osmsegment osm, app.shift s, app.tpgstoproute r, app.tpgstoproute_osmsegment r_osm
		where
			s.seconds > 0
			and
			s."timestamp" > (
				extract ( 
					epoch from localtimestamp
				)*1000 
				-
				24*60*60*1000
			) and
			s.route_id = r.id
			and
			r.id = r_osm.tpgstoproute_id
			and
			r_osm.segments_id = osm.id;
	create index ON app.geoshift ("timestamp");  

	-- navigation timestamps
	create materialized view app.timestamps as
		select 
			generate_series(
				(greatest(
					min("timestamp"),
					extract ( 
						epoch from localtimestamp
					)*1000 - 24*60*60*1000
				)::bigint / 900000) * 900000,
				(max("timestamp")/900000)*900000,
				15*60*1000
			) as millis
		from app.shift;

	-- Create a table adding to the osmshiftinfo a timestamp for drawing
	create materialized view app.timestamped_measured_geoshifts as
		select 
			to_timestamp(timestamps.millis/1000) as draw_timestamp, geoshift.*
		from 
			app.timestamps timestamps,
			app.geoshift geoshift
		where
			timestamps.millis >= geoshift.timestamp
			and
			not exists (
				select
					1
				from
					app.geoshift geoshift2 
				where 
					geoshift.startnode = geoshift2.startnode
					and
					geoshift.endnode = geoshift2.endnode
					and
					timestamps.millis > geoshift2.timestamp
					and
					geoshift2.timestamp > geoshift.timestamp
			);
	create index ON app.timestamped_measured_geoshifts (draw_timestamp);
	
	create materialized view app.predicted_geoshift as 
		select 
			osm.geom, osm.startnode, osm.endnode, s.id, to_timestamp(s.millis/1000) as draw_timestamp, s.speed, s.predictionerror
		from
			app.osmsegment osm, app.predictedshift s
		where
			osm.id=s.segment_id;
	create index ON app.predicted_geoshift (draw_timestamp);  
	
	-- Predicted and measured osmshifts
	create materialized view app.timestamped_geoshift as 
		(select id, geom, draw_timestamp, speed, -1 as predictionerror from app.timestamped_measured_geoshifts)
		union
		(select id, geom, draw_timestamp, speed, predictionerror from app.predicted_geoshift where draw_timestamp > localtimestamp);
	

## 3.2 geoserver

We use [the image built prepared by Óscar Fonts](https://hub.docker.com/r/oscarfonts/geoserver/).

Execute a docker container with this image. Generate a volume on /var/local/geoserver in order to get the configuration saved permanently. Map port to 8080.

### limit CRS output in GetCapabilities

Comma separated and without the "EPSG:" prefix: 

	Servicios > WMS > Lista de SRS limitada > 900913, 4326
	
### Roads layer

1. Create a workspace, a data store and a layer pointing to the app.timestamped_osmshiftinfo view 
2. Create a style

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

3. Set the style in the layer
4. Set `draw_timestamp` as temporal column in the dimension tab.
5. Navigate to `http://<server>:<mappedport>/geoserver/`

## 3.3 Viewer

Execute a docker container with the viewer image created before. This image is refreshing the materialized views regularly so they also take the TRAFFIC\_VIEWER\_DB\_XXX environment variables into account.

* -e GEOLADRIS_MINIFIED=true is necessary in order to get a minified version of the application.
* Map port to 8080 in order to access the service.

Navigate to `http://<server>:<mappedport>/traffic-viewer`.

## 3.4 nginx

We need to get GeoServer and the viewer on the same server and port, so we use nginx as reverse proxy. We use [the nginx:alpine image](https://hub.docker.com/_/nginx/)

## 3.5 Monit

1. Check disk space, as it may get full

	check device disk with path /
		if SPACE usage > 70% then alert

2. Check if data is being collected using the dbstatus service.
 
	check host data-gatherer with address fergonco.org
		if failed        
			port 80 protocol http
			request "/dbstatus/" with content = "weather: success\ntransport: success"
		then alert

3. Check Geoserver is up

	check host border-rampage-geoserver with address fergonco.org
		start = "/usr/bin/docker start gs"
		stop = "/usr/bin/docker stop gs"
		if failed
			port 80 protocol http
			request "/geoserver/ows?service=wms&version=1.3.0&request=GetCapabilities"
		then restart
	
# 4 Release

change pom.xml version if a major change
	mvn versions:set -DnewVersion=1.1
	mvn versions:commit
change Dockerfiles, mkdockers.sh and pushdockers.sh accordingly
mvn clean install
./mkdockers.sh
./pushdockers.sh
update start.sh
stop docker
start new docker
up a minor version in pom.xml, mkdockers and pushdockers

# 5 Data preparation

## 5.1 OSM Data download

	wget 'http://overpass-api.de/api/map?bbox=5.9627,46.2145,6.1287,46.2782' -O ligne-y.osm.xml

## 5.2 Distances between stops. TPGStopRoute* tables

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

## 5.3 Stops info

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
	
	
# 6 Analysis

## 6.1 Load backups in database

	psql -h localhost -U geomatico -d postgres -p54322
	create database tpganalysis;
	\c tpganalysis 
	create extension postgis;
	grant all on DATABASE tpganalysis to tpg;
	psql -h localhost -U tpg -d tpganalysis -p54322
	create schema app;
	pg_restore -h localhost -U tpg -d tpganalysis -p 54322 allnewmodel.backup
	# backup current database
	alter schema app rename to backup;
	create schema app;
	pg_restore -h localhost -U tpg -d tpganalysis -p 54322 allnewmodel.backup
	# merge backups




 