function main(){

  document.getElementById('authorize').addEventListener('click',
    authorizeInteractive);

}

function authorizeInteractive(){

  localStorage["authorized"] = "false";

  getUserInfo(callback);
    
}

function callback(error, status, response) {
  if (!error && status == 200) {
    localStorage["authorized"] = "true";

    document.getElementById('lblresult').style.color = 'green';
    document.getElementById('lblresult').innerHTML = 'Authorization successful. <br /> You can now close this window. Thanks!';
  } else {
    console.log('Failed to make request with error: ' + JSON.stringify(error) + ' status: ' + JSON.stringify(status));
    
    document.getElementById('lblresult').style.color = 'red';
    document.getElementById('lblresult').innerHTML = 'Authorization failed.  <br /> Please try again and click the accept button';
  }
}

main();