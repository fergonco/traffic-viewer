set -e

docker build dbstatus -t fergonco/traffic-viewer-dbstatus
docker build viewer -t fergonco/traffic-viewer
docker build data-gatherer -t fergonco/traffic-viewer-datagatherer

