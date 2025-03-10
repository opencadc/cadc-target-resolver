# cadc-target-resolver
Name resolving proxy that calls multiple target name resolvers.

### deployment
The `cadc-target-resolver` war file can be renamed at deployment time in order to support an alternate service name, including introducing
additional path elements (see war-rename.conf).

### configuration

The following runtime configuration might be made available via the `/config` directory.

### catalina.properties
This file contains java system properties to configure the tomcat server and some of the java libraries used in the service.

See <a href="https://github.com/opencadc/docker-base/tree/master/cadc-tomcat">cadc-tomcat</a>
for system properties related to the deployment environment.


## building it
```
gradle clean build
docker build -t cadc-target-resolver -f Dockerfile .
```

# Running


```
docker run --rm -it -p 8080:8080 -p 5555:5555 cadc-target-resolver:latest
```

will start the server.  You can point your browser to http://localhost:8080/cadc-target-resolver/ for the documentation page.
