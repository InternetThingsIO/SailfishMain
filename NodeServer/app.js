var app = require('express')();
var https = require('https');
var fs = require('fs');
var XMLHttpRequest = require('xhr2');


var options = {
  key: fs.readFileSync('/etc/ssl/certs/privatekey.pem'),
  cert: fs.readFileSync('/etc/ssl/certs/certificate.pem'),
  ca: fs.readFileSync('/etc/ssl/certs/intermediate.pem')
};

var server = https.createServer(options, app);
var io = require('socket.io').listen(server);

server.listen(443);

function main(){

  app.get('/', function(req, res){
    res.sendFile(__dirname + '/admin.html');
  });

  io.on('connection', function(socket){

    socket.on('join room', function(token, email){
      checkToken(token, email, socket, joinRoom, null);
    });

    socket.on('leave room', function(token, email){
      checkToken(token, email, socket, leaveRoom, null);
    });

    socket.on('send message', function(token, email, msg){
      checkToken(token, email, socket, messageToClient, [msg]);
    });

    socket.on('dismiss_notif_device', function(token, email, notifId){
      checkToken(token, email, socket, dismissNotification, [notifId]);
    });

  });

}

//gets user's info
function checkToken(access_token, email, socket, callback, args) {


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
}

function leaveRoom(email, socket, args){
  console.log('leave a room');
  socket.leave(email);
}

function messageToClient(email, socket, args){
  console.log('Emitting message to: ' + email);
  io.to(email).emit('message', args[0]);
}

function dismissNotification(email, socket, args){
  console.log('Removing notification from device: ' + args[0]);
  io.to(email).emit('dismiss_notif_device', args[0]);
}

//run the main function at the end
main();
