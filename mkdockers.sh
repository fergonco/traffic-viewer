set -e

docker build dbstatus -t fergonco/traffic-viewer-dbstatus
docker build segment-speeds -t fergonco/traffic-viewer-segmentspeeds
docker build viewer -t fergonco/traffic-viewer
docker build data-gatherer -t fergonco/traffic-viewer-datagatherer

