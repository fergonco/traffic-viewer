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

Populate stop table
----------------------

psql -h localhost -U tpg -p54322 -f src/main/sql/tpgstops.sql 
