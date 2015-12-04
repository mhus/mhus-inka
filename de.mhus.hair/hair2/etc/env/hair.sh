#!/bin/bash

JAVA_BIN=java

if [ -x $JAVA_HOME/bin/java ] ; then
  JAVA_BIN=$JAVA_HOME/bin/java
fi

start_hair=1
while [ "x$start_hair" = "x1" ] ; do
  start_hair=0
  $JAVA_BIN -Djava.library.path=${DOCUMENTUM_SHARED}/dfc -jar hair.jar -st gui
  if [ "x$?" = "x100" ] ; then
    start_hair=1
  fi
done
