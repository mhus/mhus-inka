!include "registerExtension.nsh"
; example2.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install example2.nsi into a directory that the user selects,

;--------------------------------

; The name of the installer
Name "CryptPad"

; The file to write
OutFile "CryptPad_setup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\MHU\CryptPad

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\CryptPad" "Install_Dir"

;Call IsDotNETInstalled
;Pop $0
;StrCmp $0 1 found.NETFramework no.NETFramework

;--------------------------------

; Pages

PageEx license
   LicenseText "GPL"
   LicenseData gpl.txt
PageExEnd
Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles


;--------------------------------


; The stuff to install
Section "CryptPad (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File "bin\Release\CryptPad.exe"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\CryptPad "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\CryptPad" "DisplayName" "CryptPad"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\CryptPad" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\CryptPad" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\CryptPad" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\MHU"
  CreateDirectory "$SMPROGRAMS\MHU\Uninstall"
  CreateShortCut "$SMPROGRAMS\MHU\Uninstall\CryptPad.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\MHU\CryptPad.lnk" "$INSTDIR\CryptPad.exe" "" "$INSTDIR\CryptPad.exe" 0
  
SectionEnd


Section "File Association (*.ctxt)"
  ${registerExtension} "$INSTDIR\CryptPad.exe" ".ctxt" "Crypted Text File"
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ${unregisterExtension} ".ctxt" "Crypted Text File"

  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\CryptPad"
  DeleteRegKey HKLM SOFTWARE\CryptPad

  ; Remove files and uninstaller
  Delete $INSTDIR\CryptPad.exe
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\MHU\CryptPad.lnk"
  Delete "$SMPROGRAMS\MHU\Uninstall\CryptPad.lnk"

  ; Remove directories used
  RMDir "$INSTDIR"

SectionEnd

 Function IsDotNETInstalled
   Push $0
   Push $1

   StrCpy $0 1
   System::Call "mscoree::GetCORVersion(w, i ${NSIS_MAX_STRLEN}, *i) i .r1"
   StrCmp $1 0 +2
     StrCpy $0 0

   Pop $1
   Exch $0
 FunctionEnd
