var loginBox;
var email;
var pass;
document.addEventListener("DOMContentLoaded", function(event) {
    loginBox = document.getElementById('login-box');
    email = document.getElementById('email');
    pass = document.getElementById('pass');
    loginBox.onsubmit = login;
})
function login() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                var response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("success")) {
                    window.location.href=response["success"];
                } else {
                    addErrorBox(response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox("ERROR:  Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', 'login', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('email=' + email.value + '&pass=' + pass.value);
    return false;
}
var errorBox = null;
function addErrorBox(error){
    if(!errorBox) {
        loginBox.insertAdjacentHTML('afterbegin', "<div class='error' id='errorBox'>ERROR: " + error + "</div>");
        errorBox = document.getElementsByClassName("error")[0];
    }
    else {
        errorBox.innerHTML = "ERROR: "+error;
    }
}