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
/**
 * Change the html to receive an email. Then, checks if the email exists. If it does, sends an email with a verification
 * code and displays a box letting them type it in. Then, checks if that code is correct, and if it is, displays a prompt
 * for password resetting.
 */
var emailStored;
function resetPassword() {
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
    xhr.open('POST', 'login', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    emailStored = email.value;
    xhr.send('reset=true&email=' + email.value);
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
    xhr.open('POST', 'login', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('resend=true&email=' + emailStored);
    return false;
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
                    } else if(Object.keys(response).includes("takePassword")) {
                        takePassword();
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
        xhr.send('action=reset&code=' + code.value+'&email='+emailStored);
    }
}

/**
 * Displays two password input boxes to take the user's password
 */
function takePassword() {
    console.log("taking passwords");
    document.getElementById("codeTitle").innerHTML = "Enter a new password";
    document.getElementById("bottomText").innerHTML = "<button onclick='changePassword()' class='chngButton'>Change Password</button>";
    document.getElementById("errorBox").remove();
    errorBox = null;
    let passwords = document.createElement("div");
    passwords.innerHTML = "            <label for=\"pass\">Password</label>" +
                        "            <input type=\"password\" id=\"pass\" name=\"pass\" class=\"form-input\">" +
                        "            <label for=\"passAgain\">Re-Type Your Password</label>" +
                        "            <input type=\"password\" id=\"passAgain\" name=\"passAgain\" class=\"form-input\">"
    document.getElementById("code").replaceWith(passwords);
}

function changePassword() {
    let pass = document.getElementById("pass").value;
    let passAgain = document.getElementById("passAgain").value;

    // First, put a "verifying" box
    addSuccessBox("Changing...");

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
    xhr.send('action=reset&code='+code.value+'&email='+emailStored+'&pass='+pass+'&passAgain='+passAgain);
}