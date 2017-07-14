drop database tpgtest;
create database tpgtest;
\c tpgtest
create extension postgis;
grant all on DATABASE tpgtest to tpg;
