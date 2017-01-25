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

Transport network
-------------------

create or replace view osmtransport as select * from osm_line where operator in ('RDTA', 'TPG');

Speeds view
----------------

create or replace view app.osmshiftinfo as select osmid, speed, timestamp, vehicleid, startpoint, endpoint from app.osmshift osms, app.shift s where s.id=osms.shift_id;
create or replace view app.osmshiftlastinfo as select a.* from app.osmshiftinfo a left outer join app.osmshiftinfo b on (a.osmid=b.osmid and a.timestamp<b.timestamp) where b.osmid is null;
create or replace view app.osm_speeds as 
	select osmid, speed, timestamp, vehicleid, startpoint, endpoint, way 
		from (select * from osm_line 
			where highway in ('motorway','trunk','primary','secondary','tertiary', 'unclassified','residential',
					'service','motorway_link','trunk_link','primary_link','secondary_link','tertiary_link')) as osm
		left outer join app.osmshiftlastinfo 
		on (app.osmshiftlastinfo.osmid=osm.osm_id);
