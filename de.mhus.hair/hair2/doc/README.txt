
--------
License
--------

Copyright (C) 2008  Mike Hummel

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.


--------
Install
--------

unzip the package set the documentum environment (DOCUMENTUM_SHARED) and start hair.sh or hair.exe.
The first time it will ask you for the documentum directory to setup the dfc.properties file. Please
restart hair after the setup.

-------------
Requirements
-------------

- EMC Documentum 5.3 or higher
- Java 5 or higher

------------
Use Sources
------------

- install dctm libs into the maven repo

First check which field index you need to extract the filenames of the jar files, try this command (in my case its “8″):

 for x in `ls $DOCUMENTUM_SHARED/dfc/*.jar` ; do echo $x|cut -d '/' -f 8|cut -d '.' -f 1 ; done

The result should be something like this:

All-MB
bpmutil
bsf
ci
collaboration
dfcbase
dfc
DmcRecords
log4j
messageArchive
messageService
subscription
workflow
xalan
xercesImpl
xml-apis
xmlParserAPIs
xtrim-api

Now you can change and run the following string:

for x in `ls $DOCUMENTUM_SHARED/dfc/*.jar` ; do

  n=`echo $x|cut -d '/' -f 8|cut -d '.' -f 1`;
  mvn install:install-file -DgroupId=emc.dctm -DartifactId=$n -DgeneratePom=true -Dversion=5.3.3 -Dpackaging=jar -Dfile=$x ;
done

- install wcm.jar into the maven repo

  mvn install:install-file -DgroupId=emc.dctm -DartifactId=wcm -Dversion=5.3.3 -Dpackaging=jar -Dfile=/home/.../libs/wcm.jar -DgeneratePom=true

- install idw-gpl into the maven repo

  http://www.infonode.net/

  mvn install:install-file -DgroupId=infonode -DartifactId=idw-gpl -Dversion=1.5 -Dpackaging=jar -Dfile=/home/../libs/idw-gpl.jar -DgeneratePom=true
