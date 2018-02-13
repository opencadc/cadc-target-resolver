FROM opencadc/tomcat:alpine

# Uncomment this when we can configure it externally.
#COPY TargetResolver.properties /root/config/

COPY *.war webapps/
