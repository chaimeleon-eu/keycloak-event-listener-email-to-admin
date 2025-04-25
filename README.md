# keycloak-event-listener-email-to-admin
A Keycloak extension which provides a new Event Listener named "email-to-admin", 
which sends an email to notify the admin whenever: 
 - a new user is registered
 - a user changes any of the account attributes (name, email, etc.)

The email subject and receivers are customizable, see the section ["Configuration parameters"](#configuration-parameters) below.

## Build
Use maven to generate the JAR file:
```
mvn package
```

## Install
Build your own JAR file or download from [here](/releases)). 
Then simply add it to the directory "providers" within the Keycloak working directory 
(if you use the official container image, it is `/opt/keycloak/providers`).  
And configure if you want, see the section ["Configuration parameters"](#configuration-parameters) below.

After restart the server you will see the new Event Listener "email-to-admin" in the menu "Realm settings", tab "Events", subtab "Event listeners".
Then you shoud "Enable" it just adding to the list of event listeners.

### Compatible versions
It has been tested with Keycloak 26.2.0.

### Configuration parameters
 - `emailSubject`: default value "Keycloak - User account created or updated"
 - `emailReceivers`: default value "", it is a coma-separated list of email addresses
 
All these parameteres can be added: 
 - to the command line, just adding the prefix "--spi-events-listener-email-to-admin-" and replacing camel-case with dashes "-"  
   (example: `start --spi-events-listener-email-to-admin-email-receivers=example@email.com`)
 - or to the environment variables, just adding the prefix "KC_SPI_EVENTS_LISTENER_EMAIL_TO_ADMIN_" and replacing camel-case with underlines "_"  
   (example: `export KC_SPI_EVENTS_LISTENER_EMAIL_TO_ADMIN_EMAIL_RECEIVERS=example@email.com`)

The final assigned values to all the params are shown in the Keycloak log and in the Server Info page.


## References/documentation
Official keycloak documentation about extension development using SPI framwork:  
https://www.keycloak.org/docs/latest/server_development/index.html#_extensions
