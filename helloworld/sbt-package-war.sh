#!/bin/bash
rm -f deployments/*
sbt clean package
cp target/scala-2.11/helloworld_2.11-1.0.war deployments