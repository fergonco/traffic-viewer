define([ "message-bus" ], function(bus) {

   var femap = document.createElement('div');
   femap.id = "fullscreenmap";
   document.body.append(femap);

   setInterval(function() {
      bus.send("redraw-layer", "osmspeeds");
   }, 5000);

   bus.listen("modules-loaded", function(e, message) {
      bus.send("add-layer", {
         "id" : "meteo-eeuu",
         "groupId" : "landcover",
         "label" : "Radar EEUU",
         "active" : "true",
         "mapLayers" : [ {
            "baseUrl" : "http://localhost:6305/geoserver/wms",
            "wmsName" : "tpg:osm_speeds"
         } ]
      });
      bus.send("layers-loaded");
      bus.send("layer-visibility", ["meteo-eeuu", true]);
   });
});