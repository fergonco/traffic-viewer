define(
   [ "message-bus", "Mustache" ],
   function(bus, Mustache) {
      var template = '\
       <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"\
         height="100%" width="100%">\
         <!-- background -->\
         <rect id="TimelineRect" x="0" y="0" height="100%" width="100%"\
            style="stroke:#000000; fill: #eeeeee" />\
         <!-- one layer timeline -->\
         <line x1="0%" x2="100%" y1="50%" y2="50%" style="stroke:#ff0000" />\
         {{#timestamps}}\
           <circle cx="{{x}}%" cy="50%" r="5" style="fill: #00ccff;" />\
           {{#text}}\
             <text x="{{x}}%" y="45%" transform="rotate(-35)" style="transform-origin: {{x}}% 45%; font-size:xx-small">{{.}}</text>\
           {{/text}}\
         {{/timestamps}}\
       </svg>';

      // <!-- current timestamp -->\
      // <line x1="38%" x2="38%" y1="0%" y2="100%" stroke-dasharray="5, 5"\
      // style="stroke:#0000ff" />\
      // <!-- mouseover -->\
      // <circle cx="64%" cy="50%" r="10" style="fill: #7777ff;/>';

      var timeline = document.createElement('div');
      timeline.id = "TimeLine";
      document.body.append(timeline);

      bus.listen("add-timestamps", function(e, message) {
         var dateTimestamps = message.timestamps;
         var timestamps = [];
         var min = Number.MAX_VALUE;
         var max = Number.MIN_VALUE;
         for (var i = 0; i < dateTimestamps.length; i++) {
            var timestamp = new Date(dateTimestamps[i]).getTime();
            timestamps.push(timestamp);
            if (timestamp < min) {
               min = timestamp;
            }
            if (timestamp > max) {
               max = timestamp;
            }
         }
         var range = max - min;
         var timestampData = [];
         for (var i = 0; i < timestamps.length; i++) {
            var timestamp = timestamps[i];
            var percent = 100 * (timestamp - min) / range;
            var date = new Date(timestamp);
            var timestampEntry = {
               "x" : percent
            }
            if (i % 4 == 0) {
               timestampEntry.text = date.getHours() + "h" + date.getMinutes();
            }
            timestampData.push(timestampEntry);
         }

         var view = {
            "timestamps" : timestampData
         }
         timeline.innerHTML = Mustache.render(template, view);

         var rect = document.getElementById("TimelineRect");
         rect.addEventListener("click", function(event) {
            var rect = this.getBoundingClientRect();
            var x = event.clientX - rect.left;
            var ratioWidth = x / rect.width;
            var time = ratioWidth * range + min;
            var date = new Date(time);
            console.log(date.getHours() + "h" + date.getMinutes());
         });
      });

   });