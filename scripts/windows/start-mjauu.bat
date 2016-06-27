echo %date% - %time% Start MJAUU  >> mjauu-win.log
start /b java\bin\javaw -jar jau-updater-app-0.8-alpha-10.jar >> mjauu-app-win.log
echo %date% - %time% Done >> mjauu-win.log
exit
