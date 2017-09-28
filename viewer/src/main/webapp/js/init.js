(function() {

   let timeline = di["timeline"];

   let trafficLayer = null;
   let map = L.map("fullscreenmap", {
      "minZoom" : 0,
      "maxZoom" : 20,
      "fadeAnimation" : false,
      "attributionControl" : false,
      "zoomControl" : false,
      "scrollWheelZoom" : true,
      "dragging" : true
   });

   // OSM layer
   let osmLayer = new L.TileLayer("http://{s}.tile.osm.org/{z}/{x}/{y}.png");
   osmLayer.setOpacity(0.5);
   map.addLayer(osmLayer);

   // Map configuration
   map.setView([ 46.24, 6.03 ], 13);

   let xhr = new XMLHttpRequest();
   let url = "http://fergonco.org/geoserver/ows?service=wms&version=1.3.0&request=GetCapabilities&nocache="
      + new Date().getTime();
   xhr.open("GET", url, true);
   xhr.send();
   xhr.onreadystatechange = function() {
      if (xhr.readyState == 4 && xhr.status == 200) {
         var timeDimension = xhr.responseText
            .match("<Dimension name=\"time\" default=\"current\" units=\"ISO8601\">(.*)</Dimension>");
         if (timeDimension != null) {
            var timestamps = timeDimension[1];
            var timestampArray = timestamps.split(",");
            timeline.addTimestamps(timestampArray);
            timeline.setTimestamp(new Date().getTime());
         } else {
            alert("No hay datos temporales");
         }
      }
   }

   di["timeSelectionListener"] = function(timestamp) {
      var timeString = new Date(timestamp).toISOString();
      if (trafficLayer == null) {
         trafficLayer = new L.TileLayer.WMS("http://fergonco.org/geoserver/wms", {
            "layers" : "tpg:timestamped_geoshift",
            "format" : 'image/png',
            "transparent" : true
         });
         map.addLayer(trafficLayer);
      }
      trafficLayer.setParams({
         "time" : timeString
      });
   }
})();