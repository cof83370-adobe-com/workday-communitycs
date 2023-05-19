#!/bin/bash

PROJECT_DIR=/opt/aem
cd $PROJECT_DIR

# Set the package & Port based on type
PACKAGE_FLAG=""
PORT=0

if [[ $HOSTNAME = "publish" ]]
then
  PACKAGE_FLAG="PautoInstallSinglePackagePublish"
  PORT=4503
elif [[ $HOSTNAME = "author" ]]
then
  PACKAGE_FLAG="PautoInstallSinglePackage"
  PORT=4502
fi

MVN="mvn -s .cloudmanager/maven/settings.xml"

function unit-tests() {
  $MVN clean test
}

function integration-tests() {
  $MVN clean verify -Plocal
}

function build-no-tests() {
 $MVN clean install -${PACKAGE_FLAG} -Dmaven.test.skip=true -Daem.port=${PORT}
}

function pipeline-check() {
    $MVN --batch-mode org.apache.maven.plugins:maven-dependency-plugin:3.1.2:resolve-plugins
    $MVN --batch-mode org.apache.maven.plugins:maven-clean-plugin:3.1.0:clean -Dmaven.clean.failOnError=false
    $MVN --batch-mode org.jacoco:jacoco-maven-plugin:prepare-agent package
}

function start-aem() {
  cd /
  # Start server
  nohup java -jar /aem.jar -r "${HOSTNAME}" -quickstart.server.port ${PORT} &

  # Build and deploy
  cd ${PROJECT_DIR}
  build-no-tests
}

function dispatch-setup() {
  # TODO: https://jira2.workday.com/browse/WCDEVOPS-5714
    chmod a+x aem-sdk-dispatcher-tools-unix.sh
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
