echo %date% - %time% Start MJAUU  >> mjauu-win.log
start /b java\bin\javaw -jar jau-updater-app-0.8-beta-5-SNAPSHOT.jar >> mjauu-app-win.log
echo %date% - %time% Done >> mjauu-win.log
exit
