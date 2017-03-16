FROM tomcat:7-jre8
ADD target/data-gatherer-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/data-gatherer.war

RUN set -x \
	&& apt-get update \
	&& apt-get install -y \
		openjdk-8-jdk