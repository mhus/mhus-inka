
!addplugindir ".\nsisunz\Release"

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"
  !include "Sections.nsh"

;--------------------------------

!define JAR "hair.jar"
!define VER_DISPLAY "2.1.2"
!define HAIR_ZIP "mhu-hair2-${VER_DISPLAY}-bin.zip"

; The name of the installer
Name "Hair"
Caption "Hair ${VER_DISPLAY} Setup"

; The file to write
;Icon "hair.ico"
WindowIcon off
OutFile "mhu-hair_install-${VER_DISPLAY}.exe"
SetCompressor lzma
Icon "hair.ico"

; The default installation directory
InstallDir $PROGRAMFILES\mhu-hair2

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\mhu-hair2" "Install_Dir"


;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_BITMAP "header.bmp"
  !define MUI_WELCOMEFINISHPAGE_BITMAP "welcome.bmp"
  !define MUI_UNWELCOMEFINISHPAGE_BITMAP "welcome.bmp"

;--------------------------------

; Pages

!define MUI_WELCOMEPAGE_TITLE "Welcome to the Hair2 ${VER_DISPLAY} Setup Wizard"
!define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the installation of Hair2 ${VER_DISPLAY}.\r\n\r\n$_CLICK"

!insertmacro MUI_PAGE_WELCOME

!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_LINK "Visit the mhu website for the latest news, FAQs and support."
!define MUI_FINISHPAGE_LINK_LOCATION "http://www.mhus.de/"

!define MUI_FINISHPAGE_RUN "$INSTDIR\hair2\hair.exe"
!define MUI_FINISHPAGE_NOREBOOTSUPPORT

!insertmacro MUI_PAGE_FINISH

;------------
  
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

;Page components
;Page directory
;Page instfiles

;UninstPage uninstConfirm
;UninstPage instfiles

;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"
  !insertmacro MUI_LANGUAGE "German"

;--------------------------------

; Section ""

; IfFileExists $INSTDIR\uninstall.exe +1 noUninstallerFound
;	Exec $INSTDIR\uninstall.exe
; noUninstallerFound:

; SectionEnd

; The stuff to install
Section "Hair2 (required)" SecMain

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  ; CreateDirectory $INSTDIR\target
  SetOutPath $INSTDIR\target
  File /r "..\..\target\${HAIR_ZIP}"
  
  SetOutPath $INSTDIR
  nsisunz::Unzip "$INSTDIR\target\${HAIR_ZIP}" "$INSTDIR"
  
  IfFileExists $INSTDIR\local +1 noLocalDirectory
	CopyFiles $INSTDIR\local\*.* $INSTDIR\hair2
  noLocalDirectory:
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\SamsonsHair "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mhu-hair2" "DisplayName" "Hair2"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mhu-hair2" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mhu-hair2" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mhu-hair2" "NoRepair" 1
  WriteUninstaller "uninstall.exe"

  ; Start Menu Shortcut
  CreateDirectory "$SMPROGRAMS\MHU"
  CreateShortCut "$SMPROGRAMS\MHU\Hair2.lnk" "$INSTDIR\hair2\hair.exe" "" "$INSTDIR\hair2\hair_run.ico" 0

  ; start java init
  ; --- dont create run.bat, use run.exe
  ; Call GetJRE
  ; Pop $R0
  ; StrCpy $0 '"$R0" -jar "$INSTDIR\${JAR}" -install_win "$INSTDIR" "$R0"'
  ; Exec $0

SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mhu-hair2"
  DeleteRegKey HKLM SOFTWARE\mhu-hair2

  ;ReadRegStr $R0 HKCR ".ncb" ""
  ;StrCmp $R0 "NCB.File" 0 +2
  ;  DeleteRegKey HKCR ".ncb"

  ;DeleteRegKey HKCR "NCB.File"

  ; Remove files and uninstaller
  RMDir /r $INSTDIR\hair2
  RMDir /r $INSTDIR\target
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\MHU\Hair2.*"

  ; Remove directories used
  ; RMDir /r "$INSTDIR"

SectionEnd

;--------------------------------

; find JRE

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
  StrCpy $R0 "$EXEDIR\jre\bin\java.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""

  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\java.exe"
  IfErrors 0 JreFound

  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\java.exe"

  IfErrors 0 JreFound
  StrCpy $R0 "java.exe"
        
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecMain ${LANG_ENGLISH} "The required parts of Hair2."
  LangString DESC_SecDoc ${LANG_ENGLISH} "Install extended documentation."
  LangString DESC_SecExamples ${LANG_ENGLISH} "Install example projects."

  LangString DESC_SecMain ${LANG_GERMAN} "Die benötigten Programmteile von Hair2."
  LangString DESC_SecDoc ${LANG_GERMAN} "Installiert erweiterte Dokumentationen."
  LangString DESC_SecExamples ${LANG_GERMAN} "Installiert Beispielprojekte."


  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMain} $(DESC_SecMain)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDoc} $(DESC_SecDoc)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecExamples} $(DESC_SecExamples)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END
