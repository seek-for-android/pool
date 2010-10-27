#!/bin/sh

JC_HOME=$PWD/java_card_kit-2_2_1
PATH=$PATH:$JC_HOME/bin

java -classpath $JC_HOME/lib/converter.jar:$JC_HOME/lib/api.jar com.sun.javacard.converter.Converter -noverify -i -classdir . -out CAP -exportpath $JC_HOME/api_export_files -applet 0xD2:0x76:0x00:0x01:0x18:0x01:0x01 com.gieseckedevrient.javacard.apdutester.Test com.gieseckedevrient.javacard.apdutester 0xD2:0x76:0x00:0x01:0x18:0x01:0x00 1.0

cp com/gieseckedevrient/javacard/apdutester/javacard/apdutester.cap .
