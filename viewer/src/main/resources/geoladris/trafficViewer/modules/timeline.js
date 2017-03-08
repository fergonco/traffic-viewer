define(
   [ "message-bus", "Mustache" ],
   function(bus, Mustache) {

      var timestamps = [];
      var scaleFactor = 0.00007;
      var minTimestamp = null;
      var maxTimestamp = null;
      var selectedTimestamp = null;
      var interval = null;

      var template = '\
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"\
          height="100%" width="{{totalWidth}}">\
          <!-- background -->\
          <rect id="TimelineRect" x="0" y="0" height="100%" width="100%"\
             style="stroke:#000000; fill: #eeeeee" />\
          <!-- selection -->\
          <rect id="TimelineSelection" x="0%" y="5%" width="50" height="90%" stroke-dasharray="5, 5" style="stroke:#ff0000; fill: none" />\
          <!-- one layer timeline -->\
          <line x1="0%" x2="100%" y1="50%" y2="50%" style="stroke:#ff0000; pointer-events:none" />\
          {{#timestamps}}\
            <circle cx="{{x}}" cy="50%" r="5" style="fill: #00ccff; pointer-events:none" />\
            {{#text}}\
              <text x="{{x}}" y="40%" text-anchor="middle" style="pointer-events:none">{{.}}</text>\
            {{/text}}\
          {{/timestamps}}\
        </svg>\
      ';

      var container = document.createElement("div");
      container.id = "Timeline";
      document.body.append(container);

      bus.listen("add-timestamps", function(e, message) {
         var dateTimestamps = message.timestamps;
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
      });

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

         var view = {
            "timestamps" : timestampData,
            "totalWidth" : (maxTimestamp - minTimestamp) * scaleFactor + containerBounds.width

         }
         container.innerHTML = Mustache.render(template, view);

         var rect = document.getElementById("TimelineRect");
         rect.addEventListener("click", function(event) {
            var rect = this.getBoundingClientRect();
            var x = event.clientX - rect.left;
            var xForMin = containerBounds.width / 2;
            var time = ((x - xForMin) / scaleFactor) + minTimestamp;
            setNearestTime(time);
         });

         updateSelection();
      }

      function setNearestTime(time) {
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

         bus.send("timeline:selection", {
            "timestamp" : selectedTimestamp
         });

         updateSelection();
      }

      function updateSelection() {
         if (selectedTimestamp != null) {
            // Scroll to it
            var current = container.scrollLeft;
            var target = (selectedTimestamp - minTimestamp) * scaleFactor;
            animate({
               delay : 10,
               duration : Math.abs(target - current) * 2,
               delta : function(progress) {
                  return progress;
               },
               step : function(delta) {
                  container.scrollLeft = current + (target - current) * delta;
                  if (delta == 1) {
                     // center selection
                     var containerBounds = container.getBoundingClientRect();
                     var timelineSelection = document.getElementById("TimelineSelection");
                     timelineSelection.setAttribute("x", container.scrollLeft + containerBounds.width / 2 - 25);
                  }
               }
            });
         }
      }

      window.onresize = applyTemplate;

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

   });