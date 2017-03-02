define([ "message-bus", "ui/ui" ], function(bus, ui) {

   var ON = "VisibleLegendContent";
   var OFF = "HiddenLegendContent";

   var content = ui.create("div", {
      "id" : "LegendContent",
      "parent" : document.body,
      "css" : ON
   });

   var button = ui.create("div", {
      "id" : "LegendTab",
      "parent" : "LegendContent",
      "html" : "Legend"
   });

   function addLine(color, text) {

      var line = ui.create("div", {
         "parent" : "LegendContent",
         "css" : "LegendLine"
      });
      ui.create("span", {
         "id" : color + "LegendSquare",
         "parent" : line,
         "css" : "LegendSquare"
      });
      ui.create("span", {
         "parent" : line,
         "html" : text
      });
   }

   addLine("Red", "< 15");
   addLine("Orange", "[15,40[");
   addLine("Blue", ">50");

   button.addEventListener("click", function() {
      if (content.className == ON) {
         content.className = OFF;
      } else {
         content.className = ON;
      }
   });

});