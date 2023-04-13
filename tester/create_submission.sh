#!/usr/bin/env bash

set -e

tester_directory=$(dirname "$0")
tmp_directory=$(mktemp -d)

cp "$tester_directory/../src/main/java/com/jaspervanmerle/tcmm141/$1/TrafficController.java" "$tmp_directory/TrafficController.java"
sed -i -z "s/package com.jaspervanmerle.tcmm141.$1;\n\n//g" "$tmp_directory/TrafficController.java"

rm -f "$tester_directory/../submission.zip"
zip -j "$tester_directory/../submission.zip" "$tmp_directory/TrafficController.java"

rm -rf "$tmp_directory"
