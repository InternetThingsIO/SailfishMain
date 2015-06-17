var socket;
var user_info;
var currentState;

var ACTION_POST = 'POST_NOTIFICATION';
var ACTION_REMOVE = 'REMOVE_NOTIFICATION';

var authIntervalID;

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
                false,
                onUserInfoFetched);

}

function tryGoogleAuthorization(){

  var authorized  = localStorage["authorized"];

  console.log('Storage Authorized value: ' + localStorage["authorized"]);

  if (authorized != null && authorized == "true"){

    xhrWithAuth('GET',
                'https://www.googleapis.com/plus/v1/people/me',
                false,
                onUserInfoFetched);

    if (authIntervalID)
      clearInterval(authIntervalID);

  }


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

function onUserInfoFetched(error, status, response) {
  if (!error && status == 200) {

    localStorage["authorized"] = "true";

    console.log('User Info Fetched');
    console.log(response);
    user_info = JSON.parse(response);
    
    console.log('Using email: ' + user_info.emails[0].value);

    if (user_info.image && user_info.image.url)
      localStorage["userImageURL"] = user_info.image.url;

    socketJoinRoom(user_info.emails[0].value);

  } else {

    console.log('Failed to make request with error: ' + error + ' status: ' + status);

    //set interval to pickup auth once user has finished with settings
    authIntervalID = setInterval(tryGoogleAuthorization, 2000);

    //we need to re-authorize this junk
    localStorage["authorized"] = "false";
    chrome.tabs.create({ 'url': 'chrome://extensions/?options=' + chrome.runtime.id });

  }
}

function createSocket(){

  socket = io('https://api.internetthings.io');

  socket.on('message', function(jsonStr){

    handleMessage(jsonStr);

  });

  socket.on('connect', function () { 

    //rejoin room if we have one to join
    if (user_info)
      socketJoinRoom(user_info.emails[0].value);

  });

}

//this function parses out messages we receive from the server
function handleMessage(jsonStr){

  console.log('Received message');
  console.log(jsonStr);

  var jsonObj = isJSON(jsonStr);

  //parse out the JSON
  if (jsonObj == false)
  {
    //print out a test notification
    new Notification('raw string', {
      icon: '48.png',
      body: jsonStr
    });

  }else{

    determineActions(jsonObj);

  }
}

//this function determines what actions to take based upon JSON
function determineActions(jsonObj){

    if (jsonObj.Action == ACTION_POST){

      createBasicNotif(jsonObj);

    }else if (jsonObj.Action == ACTION_REMOVE) {

      chrome.notifications.clear(jsonObj.ID);

    }

}

//creates a basic notification. other types to come
function createBasicNotif(jsonObj){
  chrome.notifications.create(jsonObj.ID, {
    type: 'basic', 
    iconUrl: 'data:image/*;base64,' + jsonObj.Base64Image, 
    title: jsonObj.Subject, 
    message: jsonObj.Body,
    eventTime: jsonObj.PostTime
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

  emitSocket('join room', room);

  showSimpleNotification('Subscribed', localStorage['userImageURL'], 'Subscribed to your feed ' + room);

}

function socketLeaveRoom(room){

  console.log('Leaving room: ' + room);

  emitSocket('leave room', room);

}

//Emits to the socket with a token
function emitSocket(name, arg1, arg2){

  chrome.identity.getAuthToken({ interactive: false }, function(token) {

    socket.emit(name, token, arg1, arg2);
  

  });

}

/*
  Displays a notification with the current time. Requires "notifications"
  permission in the manifest file (or calling
  "Notification.requestPermission" beforehand).
*/
function showSimpleNotification(inTitle, inIcon, inBody) {

  console.log('Notification Icon: ' + inIcon);

  var unixTime = new Date().getTime();

  chrome.notifications.create(unixTime.toString(), {
    type: 'basic', 
    iconUrl: '128.png', 
    title: inTitle, 
    message: inBody,
    eventTime: unixTime
  });
}


//call main to get this party started
main();


