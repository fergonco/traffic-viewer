FROM tomcat:7.0
ADD target/traffic-viewer-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/traffic-viewer.war
