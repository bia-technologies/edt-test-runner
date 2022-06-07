@echo off
set "TAB=   "
mkdir .\dev\eclipse_project
ECHO Clear project catalog
del .\dev\eclipse_project\* /S /Q /F
ECHO Copy project template
xcopy .\templates\eclipse_project .\dev\eclipse_project /Y /S /E

cd .\dev\eclipse_project\bundles\test_runner

RMDIR src /S /Q
RMDIR resources /S /Q
RMDIR META-INF /S /Q
del plugin.xml

mklink /D src ..\..\..\..\src\main\java
mklink /D resources ..\..\..\..\src\main\resources
mklink /D META-INF ..\..\..\..\META-INF
mklink plugin.xml ..\..\..\..\plugin.xml

cd ..\..\..\..