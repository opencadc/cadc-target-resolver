FROM tomcat:9-jdk11-slim

# Uncomment this when we can configure it externally.
#COPY TargetResolver.properties /root/config/

RUN rm -rf /usr/local/tomcat/webapps/*

COPY *.war /usr/local/tomcat/webapps/
