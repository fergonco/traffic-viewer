define(["message-bus"], function(bus) {
   setInterval(function() {
      bus.send("redraw-layer", "osmspeeds");
   }, 5000);
});