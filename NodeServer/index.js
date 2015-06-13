var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var xhr = require('xhr2');

app.get('/', function(req, res){
  res.sendFile(__dirname + '/admin.html');
});

io.on('connection', function(socket){
 
  socket.on('chat message', function(msg){
    io.emit('chat message', msg);
  });

  socket.on('join room', function(token){
    console.log('Trying to join room, token: ' + token);
    checkToken(token, joinRoom);

  });

  socket.on('leave room', function(token, roomID){
    socket.leave(roomID);
  });

  socket.on('send message', function(roomID, msg){
    console.log('Emitting message to: ' + roomID + 'Message: ' + msg);
    io.to(roomID).emit('message', msg);
  });

/*
  socket.on('send image', function(roomID, packageName, image){
    console.log('received image. Length: ' + image.length);
    io.to(roomID).emit('image', packageName, image);
  });
*/
});

http.listen(80, function(){
  console.log('listening on *:80');
});

//gets user's info
function checkToken(access_token, callback) {


  requestStart();

  function requestStart() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', 'https://www.googleapis.com/plus/v1/people/me');
    xhr.setRequestHeader('Authorization', 'Bearer ' + access_token);
    xhr.onload = requestComplete;
    xhr.send();
  }

  function requestComplete() {
    if (this.status == 200) {
      console.log('Success ' + this.response);
      //var user_info = JSON.parse(this.response);



      //callback(null, this.status, this.response);
    }else{
      console.log('(maybe token is bad?) Auth failed with status: ' + this.status + 'response: ' + this.response);
    }
  }
}

function joinRoomfunction(email) {

  socket.join(roomID);

}