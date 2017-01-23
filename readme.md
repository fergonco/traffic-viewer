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

Populate stop table
----------------------

psql -h localhost -U tpg -d tpgtest -p54322 -f src/main/sql/tpgstops.sql 
