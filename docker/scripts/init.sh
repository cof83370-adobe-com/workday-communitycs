#!/bin/bash

PROJECT_DIR=/opt/aem
SCRIPTS_DIR=$PROJECT_DIR/docker/scripts

if [[ ! -d "/crx-quickstart" ]]
then
  echo "Starting base configuration setup"
  .$SCRIPTS_DIR/config-utilities.sh setup-quickstart
  echo "Base configuration setup finished"
fi

echo "Starting ${HOSTNAME} setup"
if [[ $HOSTNAME = "dispatcher" ]]
then
  .$SCRIPTS_DIR/build-utilities.sh dispatch-setup
else
  .$SCRIPTS_DIR/build-utilities.sh start-aem
fi
echo "${HOSTNAME} Setup finished"

tail -f /dev/null
