#!/bin/sh

export JC_HOME=/home/manuel/Downloads/javacard
export PATH=$PATH:$JC_HOME/bin

javac -g -source 1.3 -target 1.1 -classpath $JC_HOME/lib/api.jar com/gieseckedevrient/javacard/oath/*.java
