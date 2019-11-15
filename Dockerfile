FROM opencadc/tomcat:8.5-jdk11-slim

# Uncomment this when we can configure it externally.
#COPY TargetResolver.properties /root/config/

COPY *.war webapps/
