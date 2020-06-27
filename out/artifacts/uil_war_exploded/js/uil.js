console.log("checkin");
var columns;
var about;
var scoreboard;
var mc;
var frq;
$(document).ready(function(){
    columns = $(".column");
    about = $("#aboutColumn");
    scoreboard = $("#scoreboardColumn");
    mc = $("#mcColumn");
    frq = $("#frqColumn");

    showColumn();
})

function showColumn(){
    // Check if there is an anchor, and if there is show that section of the page
    if(window.location.hash=="#scoreboard") {
        showScoreboard();
    } else if(window.location.hash=="#mc") {
        showMC();
    } else if(window.location.hash=="#frq") {
        showFRQ();
    }
}

function showHelper(show, hash){
    columns.hide();
    show.show();
    window.location.hash = "#" + hash;
}
function showAbout(){
    showHelper(about, "about");
}
function showScoreboard(){
    showHelper(scoreboard, "scoreboard");
}
function showMC(){
    showHelper(mc, "mc");
}
function showFRQ(){
    showHelper(frq, "frq");
}

// Uses ajax to update the navigation bar. Called whenever a countdown finished
function updateNav(){
    $.ajax({
        url: window.location.href,
        method: "GET",
        cache: false,
        data: {"updatenav": "true"},
        success: function(result) {
            $("#upperHalf").replaceWith(result);
        }
    });
}

// Signup for this competition
function signUp(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "signup", "eligible": false},
        success: function(result) {
            if(result == null)
                $("#signUp").replaceWith("<p id=\"signUpMsg\" class=\"error_text\">An error occurred, try again later.</p>");
            if(result["status"] === "success") {
                $("#columns").replaceWith(result["updatedHTML"]);
                columns = $(".column");
                about = $("#aboutColumn");
                scoreboard = $("#scoreboardColumn");
                mc = $("#mcColumn");
                frq = $("#frqColumn");
            }
        }
    })
}

// Signup for a competition with an elible data point competition
function signUpEligible(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "signup", "eligible": $("#eligible").val()},
        success: function(result) {
            if(result == null)
                $("#signUp").replaceWith("<p id=\"signUpMsg\" class=\"error_text\">An error occurred, try again later.</p>");
            if(result["status"] === "success") {
                $("#columns").replaceWith(result["updatedHTML"]);
                columns = $(".column");
                about = $("#aboutColumn");
                scoreboard = $("#scoreboardColumn");
                mc = $("#mcColumn");
                frq = $("#frqColumn");
            }
        }
    })
}


// Begin the multiple choice
function beginMC(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "beginMC"},
        success: function(result) {
            if(result!=null){
                if(result["status"]==="success"){
                    mc.replaceWith(result["mcHTML"]);
                    mc = $("#mcColumn");
                    mc.show();
                    columns = $(".column");
                }
            }
        }
    })
}

// Begin the free response
function beginFRQ(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "beginFRQ"},
        success: function(result) {
            if(result!=null){
                if(result["status"]==="success"){
                    frq.replaceWith(result["frqHTML"]);
                    frq = $("#frqColumn");
                    frq.show();
                    columns = $(".column");
                    showColumn();
                }
            }
        }
    })
}

function submitMC() {
    var numQuestions = document.getElementsByClassName("mcQuestion").length;
    var answers = "[";
    for(var i=1; i<=numQuestions; i++) {
        var tmp = document.querySelector('input[name="'+i+'"]:checked');
        if(tmp != null) answers+=tmp.value;
        else answers+='SK';  // The character if they skipped it
        if(i<numQuestions) answers +=",";
    }
    answers+="]";

    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "submitMC", "answers": answers},
        success: function(result) {
            if(result!=null){
                mc.replaceWith(result["mcHTML"]);
                clearInterval(xmcTestTimer);
                mc = $("#mcColumn");
                mc.show();
                columns = $(".column");
            }
        }
    })
}

// Updates everything except for the nav bars
function updatePage(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "updatePage"},
        success: function(result) {
            if(result!=null){
                $("#columns").innerHTML(result["updatedHTML"]);
            }
        }
    })
}

/**
 * FRQ-Specific functions
 * @param box
 * @param success
 */

var grabFRQProblemsTimer;
var frqProblems;
function grabFRQProblems(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "grabFRQProblems"},
        success: function(result) {
            if(result!=null){
                $("#frqProblems").replaceWith(result["frqProblemsHTML"]);
            }
        }
    })
}

function beginFRQ() {
    // Tell the server that they've started
    $.ajax({
        url:window.location.href,
        method: "POST",
        data: {"action": "beginFRQ"},
        success: function(result) {
            if(result!=null && result["status"] == "success") {
                $("#frqColumn").replaceWith(result["frqHTML"]);
                frq = $("#frqCenterBox")
                columns = $(".column");
            }
        }
    })

    frqProblems = $("#frqProblems");
    return false;
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
var box=false;
function submitFRQ(){
    if(!box) box = document.getElementById("submit");
    addScoredBox(box, "Scoring...");

    var probSelector = document.getElementById("frqProblem");
    var probId = probSelector.options[probSelector.selectedIndex].value;

    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                if(response["status"]=="success") {
                    addScoredBox(box, "SCORED: " + response["scored"]);
                    $("frqProblem"+probId).hide();
                } else {
                    addErrorBox(box, response["error"]);
                }

                grabFRQProblems();
            } else {    // A server error occurred. Show an error message
                addErrorBox(box, "Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', window.location.href, true);
    // xhr.setRequestHeader('Content-type', 'multipart/form-data');
    var formData = new FormData();
    formData.append("textfile", document.getElementById("frqTextfile").files[0]);
    formData.append("probNum", probId);
    formData.append("action", "submitFRQ");
    xhr.send(formData);
    return false;
}
function finishFRQ(){
    $.ajax({
        url:window.location.href,
        method: "POST",
        data: {"action": "finishFRQ"},
        success: function(result) {
            clearInterval(grabFRQProblemsTimer);
            if(result!=null) {
                frq.replaceWith(result["frqHTML"]);
            }
        }
    })
}

function submitChallenge(){
    if(!box) box = document.getElementById("submit");
    addScoredBox(box, "Scoring...");

    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                if(response["status"]=="success") {
                    addScoredBox(box, "SCORED: " + response["scored"]);
                    $("frqProblem"+probId).hide();
                } else {
                    addErrorBox(box, response["error"]);
                }

                grabFRQProblems();
            } else {    // A server error occurred. Show an error message
                addErrorBox(box, "Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', window.location.href, true);
    var formData = new FormData();
    formData.append("textfile", document.getElementById("frqTextfile").files[0]);
    formData.append("action", "submitFRQ");
    xhr.send(formData);
    return false;
}