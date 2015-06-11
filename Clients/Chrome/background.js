var socket;
var userImageURL;
var user_info;
var currentState;

var ACTION_POST = 'POST_NOTIFICATION';
var ACTION_REMOVE = 'REMOVE_NOTIFICATION';

//this is called at the bottom of this file.  Everything here is executed on startup
function main(){

  console.log('Notice Chrome Extension');

  //setup idle / active detection so that we only issue notifications when the user is active on their computer
  //chrome.idle.queryState(integer detectionIntervalInSeconds, function callback);
  //if we have been idle for an hour, we are idle hour = 3600 seconds
  chrome.idle.setDetectionInterval(3600);
  chrome.idle.onStateChanged.addListener(chromeStateListener);

  createSocket();

  xhrWithAuth('GET',
                'https://www.googleapis.com/plus/v1/people/me',
                true,
                onUserInfoFetched);


}

function chromeStateListener(newState){

  currentState = newState;

  //if we are idle, leave the room
  if (isComputerIdle(currentState)){
      console.log('Computer is idle, leaving room');
      socketLeaveRoom(user_info.emails[0].value);
  }else
  {
    //if we are not idle join the room
    if (user_info != null){
      console.log('Computer is no longer idle, joining room');
      socketJoinRoom(user_info.emails[0].value);
    }
  }

}

function createSocket(){

  socket = io('http://api.internetthings.io');

  socket.on('message', function(jsonStr){

    console.log('Received message');
    console.log(jsonStr);

    var jsonObj = isJSON(jsonStr);

    //parse out the JSON
    if (jsonObj == false)
    {

      new Notification('raw string', {
        icon: '48.png',
        body: jsonStr
      });

    }else{

      if (jsonObj.Action == ACTION_POST){


        chrome.notifications.create(jsonObj.ID, {   
          type: 'basic', 
          iconUrl: 'data:image/*;base64,' + jsonObj.Base64Image, 
          title: jsonObj.Subject, 
          message: jsonObj.Body,
          eventTime: jsonObj.PostTime
        });


      }else if (jsonObj.Action == ACTION_REMOVE) {

        chrome.notifications.clear(jsonObj.ID);

      }
    }

  });

/*
  socket.on('image', function(packageName, image){
    console.log(image);

    new Notification('Image: ' + packageName, {
      icon: 'data:image/*;base64,' + image,
      body: 'Image Size: ' + image.length
    });

  });
*/

  socket.on('connect', function () { 

    //rejoin room if we have one to join
    if (user_info)
      socketJoinRoom(user_info.emails[0].value);

  });

}

function isJSON(jsonString){

  try {
        var o = JSON.parse(jsonString);

        // Handle non-exception-throwing cases:
        // Neither JSON.parse(false) or JSON.parse(1234) throw errors, hence the type-checking,
        // but... JSON.parse(null) returns 'null', and typeof null === "object", 
        // so we must check for that, too.
        if (o && typeof o === "object" && o !== null) {
            return o;
        }
    }
    catch (e) { }

    return false;

}

function isComputerIdle(state){

  if (state != null && (state == 'locked' || state == 'idle'))
    return true;

  return false;
}

function socketJoinRoom(room){

  //bail if we are idle, but only if the currentState has been set
  if (isComputerIdle(currentState)){
    console.log('Can not join room when idle');
    return;
  }

  console.log('Joining room: ' + room);

  socket.emit('join room', room);

  showSimpleNotification('Subscribed', userImageURL, 'Subscribed to your feed ' + room);

}

function socketLeaveRoom(room){

  console.log('Leaving room: ' + room);

  socket.emit('leave room', room);

  showSimpleNotification('Un-Subscribed', userImageURL, 'Computer is idle, not displaying notifications');

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


