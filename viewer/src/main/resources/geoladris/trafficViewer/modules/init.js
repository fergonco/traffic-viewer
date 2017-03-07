define([ "message-bus", "iso8601" ], function(bus, iso8601) {

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
      bus.send("map:addLayer", {
         "layerId" : "osm_speeds",
         "wms" : {
            "baseUrl" : "/geoserver/wms",
            "wmsName" : "tpg:timestamped_osmshiftinfo"
         }
      });
      bus.send("map:layerVisibility", {
         "layerId" : "osm_speeds",
         "visibility" : false
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
         "url" : "/geoserver/ows?service=wms&version=1.3.0&request=GetCapabilities",
         "dataType" : "text",
         "success" : function(data) {
            data = '<Dimension name=\"time\" default=\"current\" units=\"ISO8601\">2017-03-06T17:24:05.000Z,2017-03-06T17:39:05.000Z,2017-03-06T17:54:05.000Z,2017-03-06T18:09:05.000Z,2017-03-06T18:24:05.000Z,2017-03-06T18:39:05.000Z,2017-03-06T18:54:05.000Z,2017-03-06T19:09:05.000Z,2017-03-06T19:24:05.000Z,2017-03-06T19:39:05.000Z,2017-03-06T19:54:05.000Z,2017-03-06T20:09:05.000Z,2017-03-06T20:24:05.000Z,2017-03-06T20:39:05.000Z,2017-03-06T20:54:05.000Z,2017-03-06T21:09:05.000Z,2017-03-06T21:24:05.000Z,2017-03-06T21:39:05.000Z,2017-03-06T21:54:05.000Z,2017-03-06T22:09:05.000Z,2017-03-06T22:24:05.000Z,2017-03-06T22:39:05.000Z,2017-03-06T22:54:05.000Z,2017-03-06T23:09:05.000Z,2017-03-06T23:24:05.000Z,2017-03-06T23:39:05.000Z,2017-03-06T23:54:05.000Z,2017-03-07T00:09:05.000Z,2017-03-07T00:24:05.000Z,2017-03-07T00:39:05.000Z,2017-03-07T00:54:05.000Z,2017-03-07T01:09:05.000Z,2017-03-07T01:24:05.000Z,2017-03-07T01:39:05.000Z,2017-03-07T01:54:05.000Z,2017-03-07T02:09:05.000Z,2017-03-07T02:24:05.000Z,2017-03-07T02:39:05.000Z,2017-03-07T02:54:05.000Z,2017-03-07T03:09:05.000Z,2017-03-07T03:24:05.000Z,2017-03-07T03:39:05.000Z,2017-03-07T03:54:05.000Z,2017-03-07T04:09:05.000Z,2017-03-07T04:24:05.000Z,2017-03-07T04:39:05.000Z,2017-03-07T04:54:05.000Z,2017-03-07T05:09:05.000Z,2017-03-07T05:24:05.000Z,2017-03-07T05:39:05.000Z,2017-03-07T05:54:05.000Z,2017-03-07T06:09:05.000Z,2017-03-07T06:24:05.000Z,2017-03-07T06:39:05.000Z,2017-03-07T06:54:05.000Z,2017-03-07T07:09:05.000Z,2017-03-07T07:24:05.000Z,2017-03-07T07:39:05.000Z,2017-03-07T07:54:05.000Z,2017-03-07T08:09:05.000Z,2017-03-07T08:24:05.000Z,2017-03-07T08:39:05.000Z,2017-03-07T08:54:05.000Z,2017-03-07T09:09:05.000Z,2017-03-07T09:24:05.000Z,2017-03-07T09:39:05.000Z,2017-03-07T09:54:05.000Z,2017-03-07T10:09:05.000Z,2017-03-07T10:24:05.000Z,2017-03-07T10:39:05.000Z,2017-03-07T10:54:05.000Z,2017-03-07T11:09:05.000Z,2017-03-07T11:24:05.000Z,2017-03-07T11:39:05.000Z,2017-03-07T11:54:05.000Z,2017-03-07T12:09:05.000Z,2017-03-07T12:24:05.000Z,2017-03-07T12:39:05.000Z,2017-03-07T12:54:05.000Z,2017-03-07T13:09:05.000Z,2017-03-07T13:24:05.000Z,2017-03-07T13:39:05.000Z,2017-03-07T13:54:05.000Z,2017-03-07T14:09:05.000Z,2017-03-07T14:24:05.000Z,2017-03-07T14:39:05.000Z,2017-03-07T14:54:05.000Z,2017-03-07T15:09:05.000Z,2017-03-07T15:24:05.000Z,2017-03-07T15:39:05.000Z,2017-03-07T15:54:05.000Z,2017-03-07T16:09:05.000Z,2017-03-07T16:24:05.000Z,2017-03-07T16:39:05.000Z,2017-03-07T16:54:05.000Z</Dimension>';
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

      bus.listen("time-slider.selection", function(e, date) {
         // layer is visible only after first time selection event
         bus.send("map:layerVisibility", {
            "layerId" : "osm_speeds",
            "visibility" : true
         });
         bus.send("map:mergeLayerParameters", {
            "layerId" : "osm_speeds",
            "parameters" : {
               "time" : iso8601.toString(date)
            }
         });
      });
   });
});