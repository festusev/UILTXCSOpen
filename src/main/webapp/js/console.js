var joinTeamName;
var joinTeamPass;
var joinTeamBox;

var newTeamName;
var affiliation;
var newTeamPass;
var confNewTeamPass;
var newTeamBox;

var curTeamPass;
var newTeamPassBox;

var curUserPass;
var newUserPass;
var confNewUserPass;
var newUserPassBox;
const SERVER_ERROR = "Whoops! A server error occurred. Contact an admin if the problem continues.";

var delUserPass;
var delUserBox;

let leaveTeamBox;
document.addEventListener("DOMContentLoaded", function(event) {
    joinTeamName = document.getElementById('joinTeamName');
    joinTeamPass = document.getElementById("joinTeamPass");
    joinTeamBox = document.getElementById("joinTeamBox");
    if(joinTeamBox != null) joinTeamBox.onsubmit = joinTeam;

    newTeamName = document.getElementById('newTeamName');
    affiliation = document.getElementById('affiliation');
    newTeamPass = document.getElementById('newTeamPass');
    confNewTeamPass = document.getElementById('confNewTeamPass');
    newTeamBox = document.getElementById("createTeamBox");
    if(newTeamBox != null) newTeamBox.onsubmit = createTeam;

    curTeamPass = document.getElementById("curTeamPass");
    newTeamPassBox = document.getElementById("newTeamPassBox");
    if(newTeamPassBox != null) newTeamPassBox.onsubmit = updateTeamPass;

    curUserPass = document.getElementById("curUserPass");
    newUserPass = document.getElementById("newUserPass");
    confNewUserPass = document.getElementById("confNewUserPass");
    newUserPassBox = document.getElementById("newUserPassBox");
    if(newUserPassBox!=null) newUserPassBox.onsubmit = updateUserPass;

    delUserPass = document.getElementById("delUserPass");
    delUserBox = document.getElementById("delUserBox");
    if(delUserBox!=null) delUserBox.onsubmit = delUser;

    leaveTeamBox = document.getElementById("leaveTeamBox");
})
function asyncConnectHelper(url, params, teamBox) {
    addSuccessBox(teamBox, "Running...");
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("reload")) {
                    window.location.href=response["reload"];
                } else if(Object.keys(response).includes("success")) {
                    addSuccessBox(teamBox, "SUCCESS: " + response["success"]);
                } else {
                    addErrorBox(teamBox, response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox(teamBox, SERVER_ERROR);
            }
        }
    }
    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send(params);
    return false;
}
function joinTeam() {
    asyncConnectHelper("join-team", "joinTeamName=" + joinTeamName.value + "&joinTeamPass=" + joinTeamPass.value, joinTeamBox);

    // Unset the values
    joinTeamName.value = "";
    joinTeamPass.value = "";
    return false;
}
function createTeam(){
    asyncConnectHelper("create-team", "newTeamName=" + newTeamName.value + "&affiliation=" + affiliation.value + "&newTeamPass=" + newTeamPass.value + "&confNewTeamPass=" + confNewTeamPass.value, newTeamBox);
    newTeamName.value = "";
    affiliation.value = "";
    newTeamPass.value = "";
    confNewTeamPass.value = "";
    return false;
}
function updateTeamPass() {
    asyncConnectHelper("update-team-pass", "curTeamPass=" + curTeamPass.value + "&newTeamPass=" + newTeamPass.value + "&confNewTeamPass=" + confNewTeamPass.value, newTeamPassBox);
    curTeamPass.value = "";
    newTeamPass.value = "";
    confNewTeamPass.value = "";
    return false;
}
function updateUserPass(){
    asyncConnectHelper("update-user-pass", "curUserPass=" + curUserPass.value + "&newUserPass=" + newUserPass.value + "&confNewUserPass=" + confNewUserPass.value, newUserPassBox);
    curUserPass.value = "";
    newUserPass.value = "";
    confNewUserPass.value = "";
    return false;
}
function delUser(){
    asyncConnectHelper("delete-user", "delUserPass="+delUserPass.value, delUserBox);
    delUserPass.value = "";
    return false;
}

function leaveTeam() {
    asyncConnectHelper("leave-team","leaveTeam=true",leaveTeamBox);
    return false;
}

function addErrorBox(box, error){
    let errorBox = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='error' id='" + box.id + "ERROR'>ERROR: " + error + "</div>");
    }
    else {
        errorBox.innerHTML = "ERROR: " + error;
        errorBox.className = "error";
    }
}
function addSuccessBox(box, success) {
    let errorBox = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}