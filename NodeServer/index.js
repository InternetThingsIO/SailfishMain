var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.get('/', function(req, res){
  res.sendFile(__dirname + '/admin.html');
});

io.on('connection', function(socket){
 
  socket.on('chat message', function(msg){
    io.emit('chat message', msg);
  });

  socket.on('join room', function(roomID){
    socket.join(roomID);
  });

  socket.on('leave room', function(roomID){
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