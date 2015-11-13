#!/bin/bash

# Usage: execute.sh [WildFly mode] [configuration file]
#
# The default mode is 'standalone' and default configuration is based on the
# mode. It can be 'standalone.xml' or 'domain.xml'.

JBOSS_HOME=/opt/jboss/wildfly
JBOSS_CLI=$JBOSS_HOME/bin/jboss-cli.sh
# It doesn't contains support for messaging,Jacorb, CMP (java EE web profile)
#JBOSS_PROFILE=standalone.xml
# It contains support like messaging,Jacorb, CMP (java EE full EE stack profile)
JBOSS_PROFILE=standalone-full.xml

function wait_for_server() {
  until `$JBOSS_CLI -c "ls /deployment" &> /dev/null`; do
    sleep 1
  done
}

echo "=> Starting WildFly server"
$JBOSS_HOME/bin/standalone.sh -c $JBOSS_PROFILE > /dev/null &

echo "=> Waiting for the server to boot"
wait_for_server

echo "=> Executing the commands: $1"
$JBOSS_CLI -c --file=/opt/config/$1

echo "=> Shutting down WildFly"
$JBOSS_CLI -c ":shutdown"
