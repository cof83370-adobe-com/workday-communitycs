#!/bin/bash

PROJECT_DIR=/opt/aem
SCRIPTS_DIR=$PROJECT_DIR/docker/scripts

echo "Starting ${HOSTNAME} setup"
.$SCRIPTS_DIR/build-utilities.sh start-aem

tail -f /dev/null