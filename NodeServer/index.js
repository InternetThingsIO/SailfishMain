var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var XMLHttpRequest = require('xhr2');

//gets user's info
function checkToken(access_token, email, callback) {


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

      var user_info = JSON.parse(this.response);

      //check to see if an email in the list matches the one that was sent
      user_info.emails.forEach(function(item){
          console.log(item.value);
          if (item.value == email)
            callback(email);

      });

      

    }else{
      console.log('(maybe token is bad?) Auth failed with status: ' + this.status + 'response: ' + this.response);
    }
  }
}

function joinRoom(email) {
  console.log('Successfully authed and joining room');
  socket.join(roomID);

}

app.get('/', function(req, res){
  res.sendFile(__dirname + '/admin.html');
});

io.on('connection', function(socket){

  socket.on('join room', function(token, email){
    console.log('Trying to join room with token');
    checkToken(token, email, joinRoom);

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
