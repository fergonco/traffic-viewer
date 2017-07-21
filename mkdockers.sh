set -e
VERSION=1.0.1
docker build dbstatus -t fergonco/traffic-viewer-dbstatus:$VERSION
docker build viewer -t fergonco/traffic-viewer:$VERSION
docker build data-gatherer -t fergonco/traffic-viewer-datagatherer:$VERSION

