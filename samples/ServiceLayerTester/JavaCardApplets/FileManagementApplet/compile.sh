#!/bin/sh

export JAVA_HOME=$PWD/jdk
export JC_HOME=$PWD/java_card_kit-2_2_1
export JAVA_SOURCE_ROOT=$PWD/src
export PATH=$PATH:$JC_HOME/bin

#following call to javac must be on one line
$JAVA_HOME/bin/javac -g -source 1.3 -target 1.1 -classpath $JC_HOME/lib/api.jar $JAVA_SOURCE_ROOT/com/gieseckedevrient/javacard/fileviewtest/*.java
