#!/usr/bin/env bash
set -e
./gradlew clean distZip --refresh-dependencies
cp build/distributions/*.zip ../../files/enocean.zip
