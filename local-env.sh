set -e

mvn package
./mkdockers.sh

docker start pg gs nginx 

PASSWORD=tpg

set +e
docker stop traffic-viewer
docker rm traffic-viewer
set -e
docker run -d -p 8081:8080 --link pg:pg -e TRAFFIC_VIEWER_DB_URL=jdbc:postgresql://pg:5432/tpg -e TRAFFIC_VIEWER_DB_USER=tpg -e TRAFFIC_VIEWER_DB_PASSWORD=$PASSWORD -e GEOLADRIS_MINIFIED=true --name traffic-viewer fergonco/traffic-viewer

set +e
docker stop traffic-viewer-datagatherer
docker rm traffic-viewer-datagatherer
set -e
docker run -d -p 8084:8080 --link pg:pg -v /var/traffic-viewer/:/var/traffic-viewer -e TRAFFIC_VIEWER_DB_URL=jdbc:postgresql://pg:5432/tpg -e TRAFFIC_VIEWER_DB_USER=tpg -e TRAFFIC_VIEWER_DB_PASSWORD=$PASSWORD -e TRAFFIC_VIEWER_CONFIGURATION_FOLDER=/var/traffic-viewer -e TRAFFIC_VIEWER_OSM_NETWORK=/var/traffic-viewer/ligne-y.osm.xml --name traffic-viewer-datagatherer fergonco/traffic-viewer-datagatherer

set +e
docker stop traffic-viewer-dbstatus
docker rm traffic-viewer-dbstatus
set -e
docker run -d -p 8082:8080 --link pg:pg -e TRAFFIC_VIEWER_DB_URL=jdbc:postgresql://pg:5432/tpg -e TRAFFIC_VIEWER_DB_USER=tpg -e TRAFFIC_VIEWER_DB_PASSWORD=$PASSWORD --name traffic-viewer-dbstatus fergonco/traffic-viewer-dbstatus

echo 'Navigate to http://localhost/border-rampage-docker/'


