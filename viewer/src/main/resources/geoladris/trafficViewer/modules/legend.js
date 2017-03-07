define([ "message-bus", "ui/ui", "module" ], function(bus, ui, module) {

   var ON = "VisibleLegendContent";
   var OFF = "HiddenLegendContent";

   var temp = document.createElement('div');
   temp.innerHTML = module.config().template;
   var panel = temp.firstChild;
   document.body.append(panel);

   var button = document.getElementById("LegendTab");
   button.addEventListener("click", function() {
      if (panel.className == ON) {
         panel.className = OFF;
      } else {
         panel.className = ON;
      }
   });

});