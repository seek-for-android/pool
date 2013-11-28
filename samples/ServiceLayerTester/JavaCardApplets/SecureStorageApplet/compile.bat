set JAVA_HOME=.\jdk
set JC_HOME=.\java_card_kit-2_2_1
set JAVA_SOURCE_ROOT=.\src

:: following call to javac must be on one line !
%JAVA_HOME%\bin\javac -g -source 1.3 -target 1.1 -classpath %JC_HOME%\lib\api.jar %JAVA_SOURCE_ROOT%\com\gieseckedevrient\javacard\securestoragetester\*.java

