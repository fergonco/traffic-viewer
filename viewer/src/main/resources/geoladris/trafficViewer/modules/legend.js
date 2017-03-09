define(
   [ "message-bus", "ui/ui", "module" ],
   function(bus, ui) {

      var template = "\
         <div id='LegendPanel' class='HiddenLegendContent'>\
            <div id='LegendTab'>About</div>\
            <div id='LegendContents'>\
               <div>Legend:</div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: #ff0000'></span><span=''>Less\
                     than 20km/h</span>\
               </div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: #ffba00'></span><span>Between\
                     20km/h and 30km/h</span>\
               </div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: #ffff00'></span><span>Between\
                     30km/h and 40km/h</span>\
               </div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: #00ff00'></span><span>More\
                     40km/h</span>\
               </div>\
               <div>Credits:</div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: gray'></span><span>Developer:</span><a\
                     href='http://fergonco.org/'>Fernando González Cortés</a>\
               </div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: gray'></span><span>Network\
                     data: </span><a href='http://openstreetmap.org/'>©OpenStreetMap</a>\
               </div>\
               <div class='LegendLine'>\
                  <span class='LegendSquare' style='background-color: gray'></span><span>Real\
                     time public transport data: </span><a href='http://data.tpg.ch/'>Transports\
                     publics genevois (TPG)</a>\
               </div>\
            </div>\
         </div>";

      var ON = "VisibleLegendContent";
      var OFF = "HiddenLegendContent";

      var temp = document.createElement('div');
      temp.innerHTML = template;
      var panel = temp.firstElementChild;
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