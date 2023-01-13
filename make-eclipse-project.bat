@echo off
set "TAB=   "
mkdir .\dev\eclipse_project
ECHO Clear project catalog
del .\dev\eclipse_project\* /S /Q /F
ECHO Copy project template
xcopy .\templates\eclipse_project .\dev\eclipse_project /Y /S /E
ECHO Make source symlinks
cd .\dev\eclipse_project\bundles\
for %%s in ("viewer") do (
    ECHO ============%%s============
    ECHO %tab%remove %%s
    RMDIR %%s /S /Q
    ECHO %TAB%make link %%s
    mklink /D %%s ..\..\..\%%s
)

cd ..\..\..