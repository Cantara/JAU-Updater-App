# JAU-Updater-App (MJAUU)

A small application to do a controlled update of the Java Auto Updater application itself

![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/Cantara/JAU-Updater-App) - [![Build Status](https://jenkins.quadim.ai/buildStatus/icon?job=JAU-Updater-App)](https://jenkins.quadim.ai/job/JAU-Updater-App/) [![Known Vulnerabilities](https://snyk.io/test/github/Cantara/JAU-Updater-App/badge.svg)](https://snyk.io/test/github/Cantara/JAU-Updater-App)

### MJAUU Usage

* Plan your update scenario and create/update the MJAU.json to fit your needs
* Put your configuration to your existing ConfigService by changing provisionMJAUtoCS to fit your needs and run the script
* Sit back and watch magic happen

![En example flow of controlled JAU client update with MJAUU](https://raw.githubusercontent.com/Cantara/JAU-Updater-App/master/images/MJAUU%20update%20JAU%20process%20example.png) 


### MJAUU Properties

Thes settings will be set in a config JAU uses to launch MJAUU

```
{
	"name": "mjauu_0.8-alpha-10",
	"lastChanged": "2016-06-23T09:19:42.636Z",
	"downloadItems": [{
		"url": "https://raw.githubusercontent.com/Cantara/JAU-Updater-App/master/scripts/windows/start-mjauu.bat",
		"username": null,
		"password": null,
		"metadata": {
			"groupId": "no.cantara.jau",
			"artifactId": "start-mjauu",
			"version": "0.8-alpha-10",
			"packaging": "bat",
			"lastUpdated": null,
			"buildNumber": null
		}
	}, {
		"url": "http://mvnrepo.cantara.no/content/repositories/releases/no/cantara/jau/jau-updater-app/0.8-alpha-8/jau-updater-app-0.8-alpha-10.jar",
		"username": null,
		"password": null,
		"metadata": {
			"groupId": "no.cantara.jau",
			"artifactId": "jau-updater-app",
			"version": "0.8-alpha-10",
			"packaging": "jar",
			"lastUpdated": null,
			"buildNumber": null
		}
	}, {
		"url": "http://mvnrepo.cantara.no/content/repositories/releases/no/cantara/jau/java-auto-update/0.8-beta-5/java-auto-update-0.8-beta-5.zip",
		"username": null,
		"password": null,
		"metadata": {
			"groupId": "no.cantara.jau",
			"artifactId": "java-auto-update",
			"version": "0.8-beta-5",
			"packaging": "zip",
			"lastUpdated": null,
			"buildNumber": null
		}
	}],
	"configurationStores": [{
		"fileName": "config.properties",
		"properties": {
			"version": "0.8-alpha-10"
		}
	}, {
		"fileName": "new-jau.properties",
		"properties": {
			"configservice.url": "https://<host>/jau/client",
			"configservice.username": "reader",
			"configservice.password": "readOnly",
			"configservice.artifactid": "mjauu",
			"updateinterval": "180",
			"stopApplicationOnShutdown": "false",
			"http.useProxy": "true",
			"http.proxyHost": "10.10.10.10",
			"http.proxyPort": "8080",
			"https.proxyHost": "10.10.10.10",
			"https.proxyPort": "8080"
		}
	}, {
		"fileName": "mjauu-override.properties",
		"properties": {
			"configservice.url": "https://<host>/jau/client",
			"configservice.username": "configServiceAdmin",
			"configservice.password": "adminPassword",
			"configservice.artifactid": "mjauu",
			"updateinterval": "180",
			"stopApplicationOnShutdown": "false",
			"nextApplicationConfigId": "UUID/configId for application to be launched when jau is updated and restarted",
			"mjauuApplicationConfigId": "UUID/configId for mjauu config",
			"mjauu.version": "java-auto-update-version e.g. '0.10.5'",
			"http.useProxy": "true",
			"http.proxyHost": "10.10.10.10",
			"http.proxyPort": "8080",
			"https.proxyHost": "10.10.10.10",
			"https.proxyPort": "8080",
			"customId.regex": "\\\"clientNameOnHost\\\"\\:\\\"[0-9]*\\\"",
			"customId.file": "logs/containingClientNameOnHost.log"
		}
	}],
	"startServiceScript": "cmd /c start-mjauu-0.8-alpha-10.bat"
}
```

### Things to be aware of
- As MJAUU uses the [admin API of ConfigService CS](https://wiki.cantara.no/display/JAU/ConfigService+Admin+API), it needs admin credentials in the ApplicationConfig (`mjauu-override.properties`). You should rotate the admin password in CS after using MJAUU to update clients,
as the admin credentials are now available for every client as well as in a ApplicationConfig
