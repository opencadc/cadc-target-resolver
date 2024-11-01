FROM images.opencadc.org/library/cadc-tomcat:1

COPY build/libs/cadc-target-resolver.war /usr/share/tomcat/webapps
