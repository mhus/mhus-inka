#!/bin/bash

JAVA_BIN=java

if [ -x $JAVA_HOME/bin/java ] ; then
  JAVA_BIN=$JAVA_HOME/bin/java
fi

$JAVA_BIN -jar hair.jar -hair_classpath_config
 