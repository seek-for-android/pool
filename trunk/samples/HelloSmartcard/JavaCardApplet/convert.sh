#!/bin/sh

export JAVA_HOME=$PWD/jdk
export JC_HOME=$PWD/java_card_kit-2_2_1
export JAVA_SOURCE_ROOT=$PWD/src
export PATH=$PATH:$JC_HOME/bin

#following to converter must be on one line
$JAVA_HOME/bin/java -classpath $JC_HOME/lib/converter.jar:$JC_HOME/lib/api.jar com.sun.javacard.converter.Converter -noverify -i -classdir ./src -out CAP -exportpath $JC_HOME/api_export_files -applet 0xD2:0x76:0x00:0x01:0x18:0x00:0x02:0xFF:0x49:0x50:0x25:0x89:0xC0:0x01:0x9B:0x01 com.gieseckedevrient.javacard.hellosmartcard.HelloSmartcard com.gieseckedevrient.javacard.hellosmartcard 0xD2:0x76:0x00:0x01:0x18:0x00:0x02:0xFF:0x49:0x50:0x25:0x89:0xC0:0x01:0x9B:0x00 1.0

cp $JAVA_SOURCE_ROOT/com/gieseckedevrient/javacard/hellosmartcard/javacard/hellosmartcard.cap .
