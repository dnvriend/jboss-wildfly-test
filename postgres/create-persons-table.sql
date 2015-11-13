#!/bin/bash

echo "******CREATING DATABASES AND TABLES******"
gosu postgres postgres --single <<- EOSQL
 CREATE DATABASE test;
 GRANT ALL PRIVILEGES ON DATABASE test to postgres;
EOSQL

gosu postgres postgres --single test <<- EOSQL
   ALTER TABLE persons OWNER TO postgres;
EOSQL

echo ""
echo "******DATABASES AND TABLES CREATED******"

