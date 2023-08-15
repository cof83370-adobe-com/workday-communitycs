#!/bin/bash

PROJECT_DIR=/opt/aem
PORT=0
MVN="mvn -s .cloudmanager/maven/settings.xml"

function unit-tests() {
  $MVN clean test
}

function integration-tests() {
  $MVN clean verify -Plocal
}

function build-no-tests() {
   $MVN clean install -PautoInstallPackage -PautoInstallPackagePublish -Dmaven.test.skip -Daem.host=community-aem-author-1 -Daem.port=4502  -Daem.publish.host=community-aem-publish-1 -Daem.publish.port=4503
}

function pipeline-check() {
    $MVN --batch-mode org.apache.maven.plugins:maven-dependency-plugin:3.1.2:resolve-plugins
    $MVN --batch-mode de.qaware.maven:go-offline-maven-plugin:1.2.8:resolve-dependencies
    $MVN --batch-mode org.apache.maven.plugins:maven-clean-plugin:3.1.0:clean -Dmaven.clean.failOnError=false
    $MVN --batch-mode org.jacoco:jacoco-maven-plugin:prepare-agent package
}

function start-aem-server() {
  nohup java -jar /aem.jar -r "${HOSTNAME}" -quickstart.server.port ${PORT} &
}

function setup-replication-agent() {
     curl -u admin:admin \
     -F enabled="true"  \
     -F transportPassword="admin" \
     -F transportUri="http://community-aem-publish-1:4503/bin/receive?sling:authRequestLogin=1" \
     -F transportUser="admin" \
     -F userId="" \
    'http://localhost:4502/etc/replication/agents.author/publish/jcr:content'
}

function start-aem() {
  if [[ $HOSTNAME = "publish" ]]
  then
    PORT=4503
    start-aem-server
  elif [[ $HOSTNAME = "author" ]]
  then
    PORT=4502
    start-aem-server
    # Build and deploy to author
    cd ${PROJECT_DIR}
    build-no-tests
    sleep 30s
    setup-replication-agent
  fi
}

if declare -f "$1" > /dev/null
then
  # call arguments verbatim
  "$@"
else
  # Show a helpful error
  echo "'$1' is not a known function name" >&2
  exit 1
fi
