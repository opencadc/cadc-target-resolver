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

## checking the container
```
docker run --rm -it cadc-target-resolver:latest /bin/bash
```

# running

```
docker run --rm -it -p 8080:8080 -p 5555:5555 cadc-target-resolver:latest
```

will start the server.  You can point your browser to http://localhost:8080/cadc-target-resolver/ for the documentation page.

# checking the health of the service

```
curl http://localhost:8080/cadc-target-resolver/availability
```
result
```
<?xml version="1.0" encoding="UTF-8"?>
<vosi:availability xmlns:vosi="http://www.ivoa.net/xml/VOSIAvailability/v1.0">
  <vosi:available>true</vosi:available>
  <vosi:note>*** Service NED is available. ***   *** Service SIMBAD is available. ***   *** Service VIZIER is not available. ***</vosi:note>
  <!--<clientip>172.17.0.1</clientip>-->
</vosi:availability>
```

# usage example

```
curl http://localhost:8080/cadc-target-resolver/find\?target=m31
```
result

```
target=m31
service=ned(ned.ipac.caltech.edu)
coordsys=ICRS
ra=10.684799
dec=41.269076
time(ms)=2508
```
