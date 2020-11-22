#!/usr/bin/env bash

set -e

DIR=`dirname "$0"`

mvn package
cp ${DIR}/../target/ocr-1.0.0.jar ${DIR}/../../android/app/libs/ocr-1.0.0.jar