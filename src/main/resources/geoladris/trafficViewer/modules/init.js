define([ "message-bus" ], function(bus) {

   var femap = document.createElement('div');
   femap.id = "fullscreenmap";
   document.body.append(femap);

   setInterval(function() {
      bus.send("redraw-layer", "osmspeeds");
   }, 5000);

   bus.listen("modules-loaded", function(e, message) {
      bus.send("map:addLayer", {
         "layerId" : "osm_speeds",
         "wms" : {
            "baseUrl" : "http://localhost:6305/geoserver/wms",
            "wmsName" : "tpg:osm_speeds"
         }
      });
      bus.send("zoom-to", {
         "x" : 6.03,
         "y" : 46.24,
         "zoomLevel" : 14
      });
      // portal.properties
      // map.centerLonLat=6.03,46.24
      // map.initialZoomLevel=14

      // bus.send("add-layer", {
      // "id" : "meteo-eeuu",
      // "groupId" : "landcover",
      // "label" : "Radar EEUU",
      // "active" : "true",
      // "mapLayers" : [ {
      // "baseUrl" : "http://localhost:6305/geoserver/wms",
      // "wmsName" : "tpg:osm_speeds"
      // } ]
      // });
      // bus.send("layers-loaded");
      // bus.send("layer-visibility", ["meteo-eeuu", true]);
   });
});