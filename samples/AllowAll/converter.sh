#!/bin/sh

JC_HOME=$PWD/java_card_kit-2_2_1
PATH=$PATH:$JC_HOME/bin

java -classpath $JC_HOME/lib/converter.jar:$JC_HOME/lib/api.jar com.sun.javacard.converter.Converter -noverify -i -classdir . -out CAP -exportpath $JC_HOME/api_export_files -applet 0xA0:0x00:0x00:0x01:0x51:0x41:0x43:0x4C:0x00 com.gieseckedevrient.javacard.allowall.AllowAll com.gieseckedevrient.javacard.allowall 0xA0:0x00:0x00:0x01:0x51:0x41:0x43:0x4C 1.0

cp com/gieseckedevrient/javacard/allowall/javacard/allowall.cap .
