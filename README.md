---

app.version: 1006

---


# cadc-target-resolver
Name resolving proxy that calls multiple target name resolvers.

<a href="https://travis-ci.org/opencadc/cadc-target-resolver"><img src="https://travis-ci.org/opencadc/cadc-target-resolver.svg?branch=master" /></a>


# Running

## Docker

The currently containerized version is on the Docker Hub at [https://hub.docker.com/r/opencadc/cadc-target-resolver/](https://hub.docker.com/r/opencadc/cadc-target-resolver/).

```
docker run -p 8080:8080 -p 5555:5555 opencadc/cadc-target-resolver
```

will start the server.  You can point your browser to http://localhost:8080/cadc-target-resolver/ for the documentation page.

## Existing deployment of a Servlet Container

You can build the WAR yourself with

```
gradle clean build
```

Then deploy the `build/libs/cadc-target-resolver##1006.war` to your existing Servlet Container.


## Integration tests

In the `integrationTest` folder:

```
export REGISTRY_HOST=<your registry host>
sed -i 's/@MYHOST@/'"${REGISTRY_HOST}"'/g' root/resource-caps.config
docker-compose up -d
```

To start a proxy server and the resolver web application.

Then in the working directory, run:

```
gradle -i -Dca.nrc.cadc.reg.client.RegistryClient.host=$REGISTRY_HOST intTest
```

Where `<your registry host>` matches the one running the server above.
