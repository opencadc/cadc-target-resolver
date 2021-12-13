FROM tomcat:9-jdk11-openjdk-slim

RUN rm -rf /usr/local/tomcat/webapps/*
COPY *.war /usr/local/tomcat/webapps/
