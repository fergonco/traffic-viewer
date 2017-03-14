define([ "message-bus", "module", "leaflet" ], function(bus, module, L) {

   var map = null;
   var layerInfos = {};
   var rootLayer = L.layerGroup();

   bus.listen("modules-initialized", function(e, message) {
      var config = module.config();
      var htmlId = null;
      if (config.hasOwnProperty("htmlId")) {
         htmlId = config.htmlId;
      }

      map = L.map(htmlId, {
         "minZoom" : 0,
         "maxZoom" : config.numZoomLevels || 20,
         "fadeAnimation" : false,
         "attributionControl" : false,
         "zoomControl" : false,
         "scrollWheelZoom" : false,
         "dragging" : false
      });
      map.addLayer(rootLayer);
   });

   bus.listen("zoom-to", function(e, message) {
      map.setView([ message.y, message.x ], message.zoomLevel);
   });

   bus.listen("map:addLayer", function(e, message) {
      var layer = null;
      if (message.osm) {
         var url = message.osm.osmUrls[0];
         url = url.replace("://a", "://{s}").replace(/\$({.})/g, "$1");
         layer = new L.TileLayer(url);
      } else if (message.wms) {
         layer = new L.TileLayer.WMS(message.wms.baseUrl, {
            "layers" : message.wms.wmsName,
            "format" : message.wms.imageFormat || 'image/png',
            "transparent" : true
         });
      } else {
         console.error("layer type not supported");
      }
      if (layer != null) {
         layerInfos[message.layerId] = {
            "leafletId" : L.stamp(layer)
         }
         rootLayer.addLayer(layer);
         bus.send("map:layerAdded", [ message ]);
      }
   });

   function getLayer(layerId) {
      var leafletId = layerInfos[layerId].leafletId;
      return rootLayer.getLayer(leafletId);
   }

   bus.listen("map:setLayerOpacity", function(event, message) {
      var layer = getLayer(message.layerId);
      layer.setOpacity(message.opacity);
   });

   bus.listen("map:mergeLayerParameters", function(e, message) {
      var layer = getLayer(message.layerId);
      layer.setParams(message.parameters);
   });

   bus.listen("map:createControl", function(e, message) {
   });
   bus.listen("map:activateControl", function(e, message) {
      if (message.controlId == "navigation") {
         map.addHandler("scrollWheelZoom", L.ScrollWheelZoom);
         map.scrollWheelZoom.enable();
         map.addHandler("dragging", L.Drag);
         map.dragging.enable();
      }
   });
});