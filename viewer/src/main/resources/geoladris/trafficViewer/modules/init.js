define([ "message-bus", "iso8601" ], function(bus, iso8601) {

   var speedsLayerAdded = false;

   var timeSlider = document.createElement('div');
   timeSlider.id = "TimeSlider";
   document.body.append(timeSlider);

   var femap = document.createElement('div');
   femap.id = "fullscreenmap";
   document.body.append(femap);

   setInterval(function() {
      bus.send("redraw-layer", "osmspeeds");
   }, 5000);

   bus.listen("modules-loaded", function(e, message) {
      bus.send("map:addLayer", {
         "layerId" : "osm_roads",
         "osm" : {
            "osmUrls" : [ "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png",
               "http://b.tile.openstreetmap.org/${z}/${x}/${y}.png",
               "http://c.tile.openstreetmap.org/${z}/${x}/${y}.png" ]
         }
      });
      bus.send("map:setLayerOpacity", {
         "layerId" : "osm_roads",
         "opacity" : 0.7
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
      bus.send("ajax", {
         "url" : "/geoserver/ows?service=wms&version=1.3.0&request=GetCapabilities&nocache=" + new Date().getTime(),
         "dataType" : "text",
         "success" : function(data) {
            var timeDimension = data
               .match("<Dimension name=\"time\" default=\"current\" units=\"ISO8601\">(.*)</Dimension>");
            if (timeDimension != null) {
               var timestamps = timeDimension[1];
               var timestampArray = timestamps.split(",");
               bus.send("add-timestamps", {
                  "timestamps" : timestampArray
               });
            } else {
               alert("No hay datos temporales");
            }
         },
         "complete" : function() {
         },
         "errorMsg" : "Cannot load measure timestamps"
      });

      bus.listen("timeline:selection", function(e, message) {
         var timeString = new Date(message.timestamp).toISOString();
         if (!speedsLayerAdded) {
            speedsLayerAdded = true;
            bus.send("map:addLayer", {
               "layerId" : "osm_speeds",
               "wms" : {
                  "baseUrl" : "/geoserver/wms",
                  "wmsName" : "tpg:timestamped_osmshiftinfo",
                  "parameters" : {
                     "time" : timeString
                  }
               }
            });
         } else {
            bus.send("map:mergeLayerParameters", {
               "layerId" : "osm_speeds",
               "parameters" : {
                  "time" : timeString
               }
            });
         }
      });
   });
});