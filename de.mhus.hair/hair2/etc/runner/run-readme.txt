javaRun 1.0
------------

Executes a java jar file, use the run.conf to configure jar file and
java options.

JavaRun changes directory to the EXE Directory and then executes java
with the given JAR. If no java directory is specified, it search for the
jre or jdk.

All commandline options will be redirect to the java main.

Usage - new Icon
-----------------

1. Set Icon run.ico
2. Compile run.nsi with nsis

File Format run.conf
----------------------
1. jar-file
2. java optionen, e.g. "-Xmx128M" or "-Xms6M -Xmx128M"
3. java exe file (or nothing), e.g "java.exe" or "javaw.exe"
4. java directory (if fix; if you need automatic, leave empty)
5. write "debug" if you want a messagebox with the executed command