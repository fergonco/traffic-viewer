(function(){

   let ON = "VisibleLegendContent";
   let OFF = "HiddenLegendContent";

   let panel = document.getElementById("LegendPanel");
   let button = document.getElementById("LegendTab");
   button.addEventListener("click", function() {
      if (panel.className == ON) {
         panel.className = OFF;
      } else {
         panel.className = ON;
      }
   });
   
})();
