var regBox;
var email;
var uname;
var pass;
var passAgain;
document.addEventListener("DOMContentLoaded", function(event) {
    regBox = document.getElementById('reg-box');
    uname = document.getElementById("uname");
    email = document.getElementById('email');
    pass = document.getElementById('pass');
    passAgain = document.getElementById("passAgain");
    regBox.onsubmit = register;
});
var emailStored;
function register() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                var response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("success")) {
                    document.getElementById("center").innerHTML = response["success"].replace("EMAIL_REPLACE", email.value);
                    code = document.getElementById("code");
                    regBox = document.getElementById("codeErrorBox");    // So that addErrorBox works correctly
                    errorBox = null;
                } else {
                    addErrorBox(response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox("ERROR:  Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', 'register', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    emailStored = email.value;
    xhr.send('uname=' + uname.value + '&email=' + email.value + '&pass=' + pass.value + '&passAgain=' + passAgain.value);
    return false;
}
function resend(){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                var response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("success")) {
                    addSuccessBox("Resent verification email.");
                } else {
                    addErrorBox(response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox("ERROR:  Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', 'register', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('resend=true&email=' + emailStored);
    return false;
}
var errorBox;
function addErrorBox(error){
    if(!errorBox) {
        regBox.insertAdjacentHTML('afterbegin', "<div class='error' id='errorBox'>ERROR: " + error + "</div>");
        errorBox = document.getElementsByClassName("error")[0];
    }
    else {
        errorBox.innerHTML = "ERROR: " + error;
        errorBox.className = "error";
    }
}
function addSuccessBox(success) {
    if(!errorBox) {
        regBox.insertAdjacentHTML('afterbegin', "<div class='success' id='errorBox'>" + success + "</div>");
        errorBox = document.getElementsByClassName("success")[0];
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}
var code;
function codeEntered() {
    code = document.getElementById("code");
    if(code.value.length == 6) {   // If the code is fully entered
        // First, put a "verifying" box
        addSuccessBox("Verifying...");

        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(){
            if (xhr.readyState === 4) {
                if (xhr.status == 200) { // If an error occurred
                    var response = JSON.parse(xhr.responseText);
                    if(Object.keys(response).includes("reload")) {
                        window.location.href=response["reload"];
                    } else {
                        addErrorBox(response["error"]);
                    }
                } else {    // A server error occurred. Show an error message
                    addErrorBox("Whoops! A server error occurred. Contact an admin if the problem continues.");
                }
            }
        }
        xhr.open('POST', 'verify', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send('action=register&code=' + code.value);
    }
}