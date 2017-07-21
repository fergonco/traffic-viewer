set -e

VERSION=1.0.1

docker push fergonco/traffic-viewer:$VERSION
docker push fergonco/traffic-viewer-dbstatus:$VERSION
docker push fergonco/traffic-viewer-datagatherer:$VERSION
