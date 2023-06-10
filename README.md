# ettmulti-java

It's a Spring Boot application.

First you need the jar: `mvn build` (you might want to set the properties correctly in the src/main/resources folder)

Then you run it: `java -jar <jar name>`

Congratulations

It works by allowing websocket connections to the server ... and uhh yeah


### Actual instructions

Install Java 11.

Create a file `setenv` with these contents:
```
#!/bin/sh

export DB_USER=???
export DB_PASS=???
export DB_URL=jdbc:???
export DB_DRIVER=???
export SOCKET_PORT=???
export LOGIN_PROVIDER_CLASS=com.etterna.multi.services.login.ApiSupportedByDbLogin
export LOGIN_API_URL=???
export LOGIN_API_KEY=???
```

Run `chmod 755 ./setenv` so you can run that

Make a file `start.sh` with these contents:
```
#!/bin/bash

. ./setenv

nohup ./mvnw spring-boot:run > log.txt &
```

Run `chmod 755 ./start.sh` so you can run that

To start the server, simply run `./start.sh` and it will open in the background.

You can use `watch tail -50 ./log.txt` to watch the log of the application in real time.

If you need a script that finds and kills the server for you, use this:
```
#!/bin/bash
ps -ef | grep alteo-multi | grep -v grep | awk '{print $2}' | xargs kill`
```

