
; Java Launcher
;--------------

Name "run"
Caption "Java Launcher"
Icon "run.ico"
OutFile "hair.exe"

SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

Var jar
Var javaopt
Var javaexe
Var javadir
Var debug
Var progopt
Var javaver

Section ""

  ; get options file
  IfFileExists "$EXEDIR\run.ini" +1 no_file
    ReadINIStr $jar "$EXEDIR\run.ini" exec jar
    ReadINIStr $javaopt "$EXEDIR\run.ini" exec javaopt
	ReadINIStr $progopt "$EXEDIR\run.ini" exec opt
	ReadINIStr $debug "$EXEDIR\run.ini" exec debug
	ReadINIStr $javaexe "$EXEDIR\run.ini" java exe
	ReadINIStr $javadir "$EXEDIR\run.ini" java dir
    ReadINIStr $javaver "$EXEDIR\run.ini" java version
	
  no_file:

  ; check options
  StrCmp $javaexe "" 0 +2
    StrCpy $javaexe 'java.exe'

  ; get JRE java.exe
  StrCpy $R0 '$javadir\$javaexe'
  StrCmp "$javadir" "" +1 +3
    Call GetJRE
    Pop $R0

  ; get parameters
  call GetParameters
  Pop $R1

  ; create execution string
  StrCpy $0 '"$R0" $javaopt -jar "$jar" $R1 $progopt'

  ; if debug ask for execution
  StrCmp "$debug" "debug" +1 +2
    MessageBox MB_YESNO "$0" IDNO the_end

  ; change dir and execute
  SetOutPath $EXEDIR  

  the_cmd:

  ExecWait $0 $1

  IntCmp $1 100 the_cmd


  the_end:

SectionEnd

; ReadConfString
; Read one line from file $0
; and remove last <cr> and/or <lf>

Function ReadConfString

  Push $R0
  Push $R1
  Push $R2
  Push $R3

  FileRead $0 $R1
  StrLen $R3 $R1
  StrCpy $R2 0

 loop:
   StrCpy $R0 $R1 1 $R2
   StrCmp $R0 "$\n" get
   StrCmp $R0 "$\r" get
   StrCmp $R2 $R3 get
   IntOp $R2 $R2 + 1
   Goto loop
  
  get:
    
  StrCpy $R0 $R1 $R2
   
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0

FunctionEnd

; GetParameters
; input, none
; output, top of stack (replaces, with e.g. whatever)
; modifies no other variables.
 
Function GetParameters
 
   Push $R0
   Push $R1
   Push $R2
   Push $R3
   
   StrCpy $R2 1
   StrLen $R3 $CMDLINE
   
   ;Check for quote or space
   StrCpy $R0 $CMDLINE $R2
   StrCmp $R0 '"' 0 +3
     StrCpy $R1 '"'
     Goto loop
   StrCpy $R1 " "
   
   loop:
     IntOp $R2 $R2 + 1
     StrCpy $R0 $CMDLINE 1 $R2
     StrCmp $R0 $R1 get
     StrCmp $R2 $R3 get
     Goto loop
   
   get:
     IntOp $R2 $R2 + 1
     StrCpy $R0 $CMDLINE 1 $R2
     StrCmp $R0 " " get
     StrCpy $R0 $CMDLINE "" $R2
   
   Pop $R3
   Pop $R2
   Pop $R1
   Exch $R0
 
FunctionEnd

Function GetJRE
;
;  Find JRE (Java.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume java.exe in current dir or PATH

  Push $R0
  Push $R1

  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\$javaexe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""

  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\$javaexe"
  IfErrors 0 JreFound

  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\$javaexe"

  IfErrors 0 JreFound
  StrCpy $R0 "$javaexe"
        
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd
