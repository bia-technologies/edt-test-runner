cd eclipse_project\bundles\ru.biatech.edt.xtest
RMDIR src /S /Q
RMDIR resources /S /Q
RMDIR META-INF /S /Q
del plugin.xml

mklink /D src ..\..\..\src
mklink /D resources ..\..\..\resources
mklink /D META-INF ..\..\..\META-INF
mklink plugin.xml ..\..\..\plugin.xml
cd ..\..\..