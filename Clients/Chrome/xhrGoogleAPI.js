// Using chrome.identity
var manifest = chrome.runtime.getManifest();

var clientId = encodeURIComponent(manifest.oauth2.client_id);
var scopes = encodeURIComponent(manifest.oauth2.scopes.join(' '));
var redirectUri = encodeURIComponent('https://' + chrome.runtime.id + '.chromiumapp.org');

var url = 'https://accounts.google.com/o/oauth2/auth' + 
          '?client_id=' + clientId + 
          '&response_type=id_token' + 
          '&access_type=offline' + 
          '&redirect_uri=' + redirectUri + 
          '&scope=' + scopes;

function getUserInfo(interactive, callback){

  xhrWithAuth('GET',
              'https://www.googleapis.com/oauth2/v1/tokeninfo',
              interactive,
              callback);

}

function getIDToken(callback) {

    chrome.identity.getAuthToken({ interactive: false }, function(token) {
      if (chrome.runtime.lastError) {
        callback(chrome.runtime.lastError);
        return;
      }
      
      callback(token);
    });
}

//logs into the google identity service and clears any expired tokens.  Also makes a request :-)
// @corecode_begin getProtectedData
function xhrWithAuth(method, url, interactive, callback) {
    var id_token;

    var retry = true;

    console.log(interactive);
    
    getToken();
    

    function getToken() {
        chrome.identity.getAuthToken({ interactive: interactive }, function(token) {
            if (chrome.runtime.lastError) {
                callback(chrome.runtime.lastError);
                return;
            }
            console.log(id_token);
            id_token = token;
            requestStart();
        });
    }

    function requestStart() {

        var xhr = new XMLHttpRequest();
        xhr.open(method, url + "?access_token=" + id_token);
        xhr.onload = requestComplete;
        xhr.send();
    }

    function requestComplete() {
        if (this.status == 401 && retry) {
            retry = false;
            requestStart();
        } else {
            callback(null, this.status, this.response);
        }

    }

}

function getQueryVariable(variable, query) {
    var vars = query.split('&');
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split('=');
        if (decodeURIComponent(pair[0]) == variable) {
            return decodeURIComponent(pair[1]);
        }
    }
    console.log('Query variable %s not found', variable);
}