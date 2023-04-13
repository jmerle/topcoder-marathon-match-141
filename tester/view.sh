#!/usr/bin/env bash

set -e

tester_directory=$(dirname "$0")
java -jar "$tester_directory/tester.jar" -seed "$2" -loadSolOutput "$tester_directory/results/$1" -delay 1 -infoScale 150
