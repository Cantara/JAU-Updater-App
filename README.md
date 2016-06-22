# JAU-Updater-App (MJAUU)

A small application to do a controlled update of the Java Auto Updater application itself


### MJAUU Usage

* Plan your update scenario and create/update the MJAU.json to fit your needs
* Put yur configuration to your existing ConfigService by changing provisionMJAUtoCS to fit your needs and run the script
* Sit back and watch magic happen

![En example flow of controlled JAU client update with MJAUU](https://raw.githubusercontent.com/Cantara/JAU-Updater-App/master/images/MJAUU%20update%20JAU%20process%20example.png) 


### MJAUU Properties

Thes settings will be set in a config JAU uses to launch MJAUU

```
"configurationStores": [
        ....
      {
            "fileName": "mjauu-override.properties",
            "properties": {
                "configservice.url": "http://52.30.23.241/jau/client",
                "configservice.username": "admin",
                "configservice.password": "configservice",
                "configservice.artifactid": "mjauu",
                "updateinterval": "180",
                "stopApplicationOnShutdown": "false",
                "nextApplicationConfigId":"9442768a-758d-4b81-a83a-51b207087e34",
                "customId":"set customId for current client",
                "customId.file":"find customId in a file",
                "customId.regex":"regex used to parse the file"
            }
        }
        ...
```