var socket;
var userImageURL;
var user_info;

//this is called at the bottom of this file.  Everything here is executed on startup
function main(){

  console.log('Notice Chrome Extension');

  createSocket();

  xhrWithAuth('GET',
                'https://www.googleapis.com/plus/v1/people/me',
                true,
                onUserInfoFetched);


}

function createSocket(){
  socket = io('http://api.internetthings.io');

  socket.on('notification', function(msg){

    new Notification('testing subject', {
      icon: 'http://internetthings.io/ChromeMedia/adobe.png',
      body: msg
    });

  });

  socket.on('connect', function () { 

    //rejoin room if we have one to join
    if (user_info)
      socketJoinRoom(user_info.emails[0].value);

  });

}

function socketJoinRoom(room){

  console.log('Joining room: ' + room);

  socket.emit('join room', room);

  showSimpleNotification('Subscribed', userImageURL, 'Subscribed to your feed ' + room);

}

/*
  Displays a notification with the current time. Requires "notifications"
  permission in the manifest file (or calling
  "Notification.requestPermission" beforehand).
*/
function showSimpleNotification(inTitle, inIcon, inBody) {
  new Notification(inTitle, {
    icon: inIcon,
    body: inBody
  });
}

//logs into the google identity service and clears any expired tokens.  Also makes a request :-)
// @corecode_begin getProtectedData
function xhrWithAuth(method, url, interactive, callback) {
  var access_token;

  var retry = true;

  getToken();

  function getToken() {
    chrome.identity.getAuthToken({ interactive: interactive }, function(token) {
      if (chrome.runtime.lastError) {
        callback(chrome.runtime.lastError);
        return;
      }

      access_token = token;
      requestStart();
    });
  }

  function requestStart() {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url);
    xhr.setRequestHeader('Authorization', 'Bearer ' + access_token);
    xhr.onload = requestComplete;
    xhr.send();
  }

  function requestComplete() {
    if (this.status == 401 && retry) {
      retry = false;
      chrome.identity.removeCachedAuthToken({ token: access_token },
                                            getToken);
    } else {
      callback(null, this.status, this.response);
    }
  }
}


function onUserInfoFetched(error, status, response) {
  if (!error && status == 200) {
    //changeState(STATE_AUTHTOKEN_ACQUIRED);
    console.log(response);
    user_info = JSON.parse(response);
    
    console.log('Using email: ' + user_info.emails[0].value);

    if (user_info.image && user_info.image.url)
      userImageURL = user_info.image.url;
    socketJoinRoom(user_info.emails[0].value);

  } else {
    console.log('Failed to make request with error: ' + error + ' status: ' + status);
  }
}


//call main to get this party started
main();


