define([ "message-bus", "ui/ui"], function(bus, ui) {

   var ON = "VisibleLegendContent";
   var OFF = "HiddenLegendContent";

   var content = ui.create("div", {
      "id" : "LegendContent",
      "parent" : document.body,
      "css" : OFF
   });

   var button = ui.create("div", {
      "id" : "LegendTab",
      "parent" : "LegendContent",
      "html" : "About"
   });
   ui.create("div", {
      "id" : "LegendLineContainer",
      "parent" : "LegendContent"
   });
   /*
    * Legend
    */
   ui.create("div", {
      "parent" : "LegendLineContainer",
      "html" : "Legend:"
   });
   function addLine(color, text) {

      var line = ui.create("div", {
         "parent" : "LegendLineContainer",
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
   addLine("Red", "Less than 15km/h");
   addLine("Orange", "Between 15km/h and 40km/h");
   addLine("Blue", "More than 50km/h");
   /*
    * Credits
    */
   ui.create("div", {
      "parent" : "LegendLineContainer",
      "html" : "Credits:"
   });
   addLine("gray", "Developer: http://fergonco.org");
   addLine("gray", "Network data: ©OpenStreetMap");
   addLine("gray", "Real time public transport data: Transports publics genevois (TPG)");

   button.addEventListener("click", function() {
      if (content.className == ON) {
         content.className = OFF;
      } else {
         content.className = ON;
      }
   });

});