// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

/*
  Displays a notification with the current time. Requires "notifications"
  permission in the manifest file (or calling
  "Notification.requestPermission" beforehand).
*/
function show() {
  var time = /(..)(:..)/.exec(new Date());     // The prettyprinted time.
  var hour = time[1] % 12 || 12;               // The prettyprinted hour.
  var period = time[1] < 12 ? 'a.m.' : 'p.m.'; // The period of the day.
  new Notification(hour + time[2] + ' ' + period, {
    icon: '48.png',
    body: 'Time to make the toast.'
  });
}

new Notification('testing subject', {
  icon: '48.png',
  body: 'first notification'
});
  
var socket = io('http://api.internetthings.io');

socket.emit('join room', 'test');

socket.on('notification', function(msg){

  new Notification('testing subject', {
    icon: 'http://internetthings.io/ChromeMedia/adobe.png',
    body: msg
  });

});


