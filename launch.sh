#!/bin/bash
docker rm -f $(docker ps -aq)
docker-compose build --no-cache
docker-compose --x-networking up