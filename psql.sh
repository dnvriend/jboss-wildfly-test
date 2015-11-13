#!/bin/bash
echo "Help for psql"
echo "============="
echo "\l or \list : shows all databases"
echo "\dt : shows all tables in the current database"
echo "\connect database_name : switch to a database"
docker run -it --net=jbosswildflytest -e PGPASSWORD=postgres postgres psql -U postgres -h postgres