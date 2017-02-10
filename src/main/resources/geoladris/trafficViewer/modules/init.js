define([ "message-bus", "iso8601" ], function(bus, iso8601) {

   var femap = document.createElement('div');
   femap.id = "fullscreenmap";
   document.body.append(femap);

   setInterval(function() {
      bus.send("redraw-layer", "osmspeeds");
   }, 5000);

   bus.listen("modules-loaded", function(e, message) {
      bus.send("map:addLayer", {
         "layerId" : "osm_roads",
         "wms" : {
            "baseUrl" : "http://localhost:6305/geoserver/wms",
            "wmsName" : "tpg:osm_roads"
         }
      });
      bus.send("map:addLayer", {
         "layerId" : "osm_speeds",
         "wms" : {
            "baseUrl" : "http://localhost:6305/geoserver/wms",
            "wmsName" : "tpg:timestamped_osm_speeds"
         }
      });
      bus.send("zoom-to", {
         "x" : 6.03,
         "y" : 46.24,
         "zoomLevel" : 13
      });
      bus.send("map:createControl", {
         "controlId" : "navigation",
         "controlType" : "navigation"
      });
      bus.send("map:activateControl", {
         "controlId" : "navigation"
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
      bus.send("ajax", {
         "url" : "http://localhost:6305/geoserver/ows?service=wms&version=1.3.0&request=GetCapabilities",
         "dataType": "text",
         "success" : function(data) {
            var timestamps = data.match("<Dimension name=\"time\" default=\"current\" units=\"ISO8601\">(.*)</Dimension>")[1];
            var timestampArray = timestamps.split(",");
            bus.send("add-layer", {
               "timestamps" : timestampArray
            });
            bus.send("layers-loaded");
         },
         "complete" : function() {
         },
         "errorMsg" : "Cannot load measure timestamps"
      });

      bus.listen("time-slider.selection", function(e, date) {
         bus.send("map:mergeLayerParameters", {
            "layerId" : "osm_speeds",
            "parameters" : {
               "time" : iso8601.toString(date)
            }
         });
      });
   });
});