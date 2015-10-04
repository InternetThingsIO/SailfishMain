var appClientID = ["1093471737235-3kcsj89v5rrek85i2v5e0no7u9n5elu0.apps.googleusercontent.com",
                   "1093471737235-frq9mvlpdsv14dcuvcatvun6jq6ti0ku.apps.googleusercontent.com"];

var nr = require('newrelic');

var app = require('express')();
var http = require('http').Server(app);
var XMLHttpRequest = require('xhr2');

//get stuff for socket.io
var redis = require('socket.io-redis');
var io = require('socket.io')(http);

//get stuff for decrypting google access token
var googleIdToken = require('google-id-token');
var request = require('request');
//cache for google certs so we only have to get them once
var googleCerts;



function main(){

  //redis is installed on load balancer
  io.adapter(redis({ host: '10.132.6.34', port: 6379 }));

  //listen on port 6001
  http.listen(6001, function(){
    console.log('listening on *:6001');
  });

  app.get('/', function(req, res){
    res.sendFile(__dirname + '/admin.html');
  });

  io.on('connection', function(socket){

    socket.on('ping', nr.createWebTransaction('/ws/ping', function (data) {
      socket.emit('pong');
      nr.endTransaction();
    }));


    socket.on('join room', nr.createWebTransaction('/ws/join_room', function (token, email){
      checkToken(token, email, socket, joinRoom, null);
    }));

    socket.on('leave room', nr.createWebTransaction('/ws/leave_room', function (token, email){
      checkToken(token, email, socket, leaveRoom, null);
    }));

    socket.on('send message', nr.createWebTransaction('/ws/send_message', function (token, email, msg){
      checkToken(token, email, socket, messageToExt, [msg]);
    }));

    //deprecated remove later
    socket.on('dismiss_notif_device', nr.createWebTransaction('/ws/dismiss_notif_device', function(token, email, notifId){
      checkToken(token, email, socket, dismissNotification, [notifId]);
    }));

    socket.on('send_message_app', nr.createWebTransaction('/ws/send_message_app', function(token, email, notifId){
      checkToken(token, email, socket, messageToApp, [notifId]);
    }));

  });

}

//check token with google-id-token
function checkToken(access_token, email, socket, callback, args){
	
  var parser = new googleIdToken({ getKeys: getGoogleCerts });
  parser.decode(access_token, requestComplete);

  function requestComplete(err, token){
    if (err){
      console.log('Failed to validate token, tring old method /people/me');
      console.log(err);
      checkToken2(access_token, email, socket, callback, args);
    }else{
      console.log('Successfully decrypted token');
      //console.log(JSON.stringify(token));
      //console.log(JSON.stringify(token));

      if (tokenIsValid(token, email, appClientID)){
        callback(email, socket, args);
      }else{
        console.log('Detected fake token for user: ' + email);
      }
    }
  }

  function tokenIsValid(token, email, acID){
	
    if (token.data.email != email){
      console.log('Token email did not match. Token: ' + token.data.email + ' Provided: ' + email);
      return false;
    }

    for (var id in acID) {
        if (token.data.aud == id)
            return true;
     }
 
    console.log('Token clientID did not match. Token: ' + token.data.aud + 'In Node: ' + acID.join());
    return false;

  }

}

function getGoogleCerts(kid, callback) {
    
  if (!googleCerts){ 
    
    //console.log('getting google cert from server');

    request({uri: 'https://www.googleapis.com/oauth2/v1/certs'}, function(err, response, body){
        if(err && response.statusCode !== 200) {
            err = err || "error while retrieving the google certs";
            console.log(err);
            callback(err, {})
        } else {
	    console.log('got google cert from server');
            googleCerts = JSON.parse(body);
            callback(null, googleCerts[kid]);
        }
    });

  }else{
    //console.log('Using cached cert');
    callback(null, googleCerts[kid]);
  }

}

//gets user's info
function checkToken2(access_token, email, socket, callback, args) {


  requestStart();

  function requestStart() {
    console.log('Making request with token: ' + access_token);
    var xhr = new XMLHttpRequest();
    xhr.open('GET', 'https://www.googleapis.com/plus/v1/people/me');
    xhr.setRequestHeader('Authorization', 'Bearer ' + access_token);
    xhr.onload = requestComplete;
    xhr.send();
  }

  function requestComplete() {
    if (this.status == 200) {

      var user_info = JSON.parse(this.response);

      //check to see if an email in the list matches the one that was sent
      user_info.emails.forEach(function(item){

          if (item.value == email)
            callback(email, socket, args);

      });

      

    }else{
      console.log('(maybe token is bad?) Auth failed with status: ' + this.status + 'response: ' + this.response);
    }
  }
}

function joinRoom(email, socket, args) {
  console.log('join a room');
  socket.join(email);
  nr.endTransaction();
}

function leaveRoom(email, socket, args){
  console.log('leave a room');
  socket.leave(email);
  nr.endTransaction();
}

function messageToExt(email, socket, args){
  console.log('Emitting message to: ' + email);
  io.to(email).emit('message', args[0]);
  nr.endTransaction();
}

//deprecated remove later
function dismissNotification(email, socket, args){
  console.log('Removing notification from device: ' + args[0]);
  io.to(email).emit('dismiss_notif_device', args[0]);
  nr.endTransaction();
}

function messageToApp(email, socket, args){
  console.log('Sending message to app: ' + args[0]);
  io.to(email).emit('receive_message_app', args[0]);
  nr.endTransaction();
}

//start main at end
main();
