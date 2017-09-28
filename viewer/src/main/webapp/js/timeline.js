(function(){

   let timestamps = [];
   let scaleFactor = 0.00009;
   let minTimestamp = null;
   let maxTimestamp = null;
   let selectedTimestamp = null;
   let interval = null;
   let template = '\
      <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"\
        height="100%" width="{{totalWidth}}">\
        <!-- background -->\
        <rect id="TimelineRect" x="0" y="0" height="100%" width="100%"\
           style="stroke:#000000; fill: #ffba00" />\
        <!-- selection -->\
        <rect id="TimelineSelection" x="0%" y="5%" width="20" height="90%" stroke-dasharray="5, 5" style="stroke:#ff0000; fill: none" />\
        <!-- now line -->\
        <line x1="{{now_x}}" x2="{{now_x}}" y1="0%" y2="100%" stroke-width="3" style="stroke:#ff0000; pointer-events:none"></line>\
        <text x="{{now_x}}" y="16%" font-size="1.3em" text-anchor="end" style="pointer-events:none">Measured&nbsp;&nbsp;</text>\
        <text x="{{now_x}}" y="16%" font-size="1.3em" text-anchor="start" style="pointer-events:none">&nbsp;&nbsp;Predicted</text>\
        <!-- one layer timeline -->\
        <line x1="0%" x2="100%" y1="50%" y2="50%" style="stroke:#ff0000; pointer-events:none" />\
        {{#timestamps}}\
          <circle cx="{{x}}" cy="50%" r="5" style="fill: #666666; pointer-events:none" />\
          {{#text}}\
            <text x="{{x}}" y="40%" font-size="1.3em" text-anchor="middle" style="pointer-events:none">{{.}}</text>\
          {{/text}}\
        {{/timestamps}}\
      </svg>\
    ';
   let container = document.getElementById("Timeline");

   function addTimestamps(dateTimestamps) {
      timestamps = [];
      minTimestamp = Number.MAX_VALUE;
      maxTimestamp = Number.MIN_VALUE;
      for (var i = 0; i < dateTimestamps.length; i++) {
         var timestamp = new Date(dateTimestamps[i]).getTime();
         timestamps.push(timestamp);
         if (timestamp < minTimestamp) {
            minTimestamp = timestamp;
         }
         if (timestamp > maxTimestamp) {
            maxTimestamp = timestamp;
         }
      }
      applyTemplate();
      container.scrollLeft = (maxTimestamp - minTimestamp) * scaleFactor;
      setNearestTime(new Date().getTime());
   }
   
   function setTimestamp(timestamp) {
      setNearestTime(timestamp, false);
   }

   function setNearestTime(time, animate) {
      // Find nearest timestamp
      var nearestTimestamp = null;
      var nearestDistance = Number.MAX_VALUE;
      for (var i = 0; i < timestamps.length; i++) {
         var distance = Math.abs(timestamps[i] - time);
         if (nearestTimestamp == null || distance < nearestDistance) {
            nearestDistance = distance;
            nearestTimestamp = timestamps[i];
         }
      }

      selectedTimestamp = nearestTimestamp;

      di["timeSelectionListener"](selectedTimestamp);

      updateSelection(animate);
   }

   function updateSelection(doAnimate) {
      if (selectedTimestamp != null) {
         // Scroll to it
         var current = container.scrollLeft;
         var target = (selectedTimestamp - minTimestamp) * scaleFactor;
         if (doAnimate) {
            animate({
               delay : 10,
               duration : Math.abs(target - current),
               delta : function(progress) {
                  return progress;
               },
               step : function(delta) {
                  container.scrollLeft = current + (target - current) * delta;
                  if (delta == 1) {
                     centerSelection();
                  }
               }
            });
         } else {
            container.scrollLeft = target;
            centerSelection();
         }
      }
   }

   function centerSelection() {
      var containerBounds = container.getBoundingClientRect();
      var timelineSelection = document.getElementById("TimelineSelection");
      timelineSelection.setAttribute("x", container.scrollLeft + containerBounds.width / 2 - 10);
   }
   
   function applyTemplate() {
      var timestampData = [];
      var containerBounds = container.getBoundingClientRect();
      for (var i = 0; i < timestamps.length; i++) {
         var timestamp = timestamps[i];
         var x = (timestamp - minTimestamp) * scaleFactor + containerBounds.width / 2;
         var date = new Date(timestamp);
         var timestampEntry = {
            "x" : x,
            text : date.getHours() + "h" + ("0" + date.getMinutes()).slice(-2)
         }
         timestampData.push(timestampEntry);
      }

      var now = new Date().getTime();
      var view = {
         "timestamps" : timestampData,
         "totalWidth" : (maxTimestamp - minTimestamp) * scaleFactor + containerBounds.width,
         "now_x" : (now - minTimestamp) * scaleFactor + containerBounds.width / 2
      }
      container.innerHTML = Mustache.render(template, view);

      var rect = document.getElementById("TimelineRect");
      rect.addEventListener("click", function(event) {
         var rect = this.getBoundingClientRect();
         var x = event.clientX - rect.left;
         var xForMin = containerBounds.width / 2;
         var time = ((x - xForMin) / scaleFactor) + minTimestamp;
         setNearestTime(time, true);
      });

      updateSelection();
   }

   function animate(opts) {

      var start = new Date();

      var id = setInterval(function() {
         var timePassed = new Date - start
         var progress = timePassed / opts.duration

         if (progress > 1)
            progress = 1

         var delta = opts.delta(progress)
         opts.step(delta)

         if (progress == 1) {
            clearInterval(id)
         }
      }, opts.delay || 10)

   }

   window.onresize = applyTemplate;

   di["timeline"] = {
      "addTimestamps":addTimestamps,
      "setTimestamp":setTimestamp
   };
})();