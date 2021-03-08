var box;
var problemCnt;
document.addEventListener("DOMContentLoaded", function(event) {
    box = document.getElementById("submit");
    box.onsubmit = submit;
    problemCnt = document.getElementById("submissionRight");

    // Every 10 seconds, grab the updated Problems list
    var x = setInterval(function() {
        grabProblems()
    }, 1000*10);
});
function begin() {
    startTimer();
    beginWarning.style.display = "none";

    // Tell the server that they've started
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'programming', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('started=' + window.cntdwnLoaded);
    return false;
}
function submit(){
    addScoredBox(box, "Scoring...");

    var probSelector = document.getElementById("problem");
    var probId = probSelector.options[probSelector.selectedIndex].value;

    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("reload")) {
                    window.location.href=response["reload"];
                } else if(Object.keys(response).includes("success")) {
                    addScoredBox(box, "SCORED: " + response["success"]);
                } else {
                    addErrorBox(box, response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox(box, "Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', "programming", true);
    // xhr.setRequestHeader('Content-type', 'multipart/form-data');
    var formData = new FormData();
    var file = document.getElementById("textfile");
    formData.append("textfile", file.files[0]);
    formData.append("probNum", probId)
    xhr.send(formData);

    file.value = "";

    return false;
}
function grabProblems(){
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("problems")) {
                    problemCnt.innerHTML = response["problems"];
                }
            }
        }
    }
    xhr.open('POST', "programming", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("grabproblems=true");
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
    grabProblems(); // Update the problems list
}
function addScoredBox(box, success) {
    let errorBox = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
    grabProblems(); // Update the problems list
}