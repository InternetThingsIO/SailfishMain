function main(){

  document.getElementById('authorize').addEventListener('click',
    authorizeInteractive);

}

function authorizeInteractive(){

  localStorage["authorized"] = "false";

  xhrWithAuth('GET',
              'https://www.googleapis.com/plus/v1/people/me',
              true,
              callback);

}

function callback(error, status, response) {
  if (!error && status == 200) {
    localStorage["authorized"] = "true";

    document.getElementById('lblresult').style.color = 'green';
    document.getElementById('lblresult').innerHTML = 'Authorization successful. <br /> You can now close this window. Thanks!';
  } else {
    console.log('Failed to make request with error: ' + error + ' status: ' + status);
    
    document.getElementById('lblresult').style.color = 'red';
    document.getElementById('lblresult').innerHTML = 'Authorization failed.  <br /> Please try again and click the accept button';
  }
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

main();