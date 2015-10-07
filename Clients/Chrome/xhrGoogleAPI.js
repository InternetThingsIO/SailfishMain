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

    chrome.identity.launchWebAuthFlow(
        {
            'url': this.url, 
            'interactive':false
        }, 
        function(redirectedTo) {
            if (chrome.runtime.lastError) {
                // Example: Authorization page could not be loaded.
                callback(chrome.runtime.lastError);
                return;
            }
            else {
                var response = redirectedTo.split('#', 2)[1];
                callback(getQueryVariable('id_token', response));
            }
        }
    );

}

//logs into the google identity service and clears any expired tokens.  Also makes a request :-)
// @corecode_begin getProtectedData
function xhrWithAuth(method, url, interactive, callback) {
  var id_token;

  var retry = true;

  getToken();

    function getToken() {

        chrome.identity.launchWebAuthFlow(
            {
                'url': this.url, 
                'interactive':interactive
            }, 
            function(redirectedTo) {
                if (chrome.runtime.lastError) {
                    // Example: Authorization page could not be loaded.
                    callback(chrome.runtime.lastError, 500);
                    return;
                }
                else {
                    var response = redirectedTo.split('#', 2)[1];
                    id_token = getQueryVariable('id_token', response);
                    requestStart();
                }
            }
        );

    }

    function requestStart() {
        console.log(url + "?id_token=" + id_token);
        var xhr = new XMLHttpRequest();
        xhr.open(method, url + "?id_token=" + id_token);
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