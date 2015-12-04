#!/bin/bash

java -Djava.library.path=${DOCUMENTUM_SHARED}/dfc -jar hair.jar -st cons $@
