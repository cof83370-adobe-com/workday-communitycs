#!/bin/bash

set -e

PROJECT_DIR=/opt/aem
S3_URI=s3://community-aem/container/images

function create-new-quickstart-package() {
  tar -czvf ${HOSTNAME}-crz-quickstart.tar.gz crx-quickstart
}

function push-quickstart-package() {
aws s3 cp $PROJECT_DIR/docker/${HOSTNAME}-crx-quickstart.tar.gz ${S3_URI}/wc_aem_${HOSTNAME}/
}

function get-quickstart-package() {
aws s3 cp ${S3_URI}/wc_aem_${HOSTNAME}/${HOSTNAME}-crx-quickstart.tar.gz $PROJECT_DIR/docker
}

function setup-quickstart() {
  PACKAGE=$PROJECT_DIR/docker/${HOSTNAME}-crx-quickstart.tar.gz
  if [[ ! -f "$PACKAGE" ]]
  then
    get-quickstart-package
    sleep 1
  fi
  tar -xvf $PACKAGE -C /
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
