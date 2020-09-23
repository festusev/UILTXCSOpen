var config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        about: "aboutColumn",
        scoreboard: "scoreboardColumn",
        team: "teamColumn",
        mc: "mcColumn",
        frq: "frqColumn",
        // answers : "answersColumn",
        publicComps: "public_competitions",
        classComps: "class_competitions",
        myComps: "my_competitions",
        frqProblems: "frqProblems",
        mcSubmissions: "mcSubmissions",
        mcSubmissionsTr: "mcSubmissionsTr",
        frqSubmissionsTable: "frqSubmissionsTable",
        frqSubmissionsTr: "frqSubmissionsTr",
        teamMembers: "teamMembers",
        aboutNav: "aboutNav",
        scoreboardNav: "scoreboardNav",
        writtenNav: "writtenNav",
        handsOnNav: "handsOnNav",
        signUpBox: "signUpBox",
        teamCode: "teamCode",
        toggleCreateTeam: "toggleCreateTeam"
    },
    CLASSES: {
        columns: "column",
        secondNavItem: 'secondNavItem'
    },
    RESULTS: ["Incorrect", "Correct", "No Penalty"],
    SOCKET_FUNCTIONS: {
        "addSmallMC": function (response) {
            var html = response[1];
            var template = document.createElement('template');
            template.innerHTML = html;
            dom.mcSubmissions.insertBefore(template.content.firstChild, dom.mcSubmissionsTr.nextSibling);
        }, "addSmallFRQ": function (response) {
            var html = response[1];
            var template = document.createElement('template');
            template.innerHTML = html;
            dom.frqSubmissionsTable.insertBefore(template.content.firstChild, dom.frqSubmissionsTr.nextSibling);
        }, "updateTeam": function (response) {
            var html = response[1];
            dom.teamMembers.innerHTML = html;
        }, "competitionDeleted": function (response) {
            window.location.replace(window.location.host + "/console/competitions");
        }, "updateFRQProblems": function (response) {
            var template = document.createElement('template');
            template.innerHTML = response[1];
            dom.frqProblems.replaceWith(template.content.firstChild);
        }, "updateScoreboard": function (response) {
            var template = document.createElement('template');
            template.innerHTML = response[1];
            var display = dom.scoreboard.style.display;
            dom.scoreboard.replaceWith(template.content.firstChild);
            dom.cached[config.IDs.scoreboard] = null;
            showColumn();
        }, "reScoreMC": function (response) {
            // TODO: Write this
        }
    }
};
var dom = {
    cached: {},
    getHelper: function (id) {
        if (this.cached[id] == null)
            this.cached[id] = document.getElementById(id);
        return this.cached[id];
    },
    get about() { return this.getHelper(config.IDs.about); },
    get scoreboard() { return this.getHelper(config.IDs.scoreboard); },
    get team() { return this.getHelper(config.IDs.team); },
    get mc() { return this.getHelper(config.IDs.mc); },
    get frq() { return this.getHelper(config.IDs.frq); },
    get publicComps() { return this.getHelper(config.IDs.publicComps); },
    get classComps() { return this.getHelper(config.IDs.classComps); },
    get myComps() { return this.getHelper(config.IDs.myComps); },
    get frqProblems() { return this.getHelper(config.IDs.frqProblems); },
    get mcSubmissions() { return this.getHelper(config.IDs.mcSubmissions); },
    get mcSubmissionsTr() { return this.getHelper(config.IDs.mcSubmissionsTr); },
    get frqSubmissionsTable() { return this.getHelper(config.IDs.frqSubmissionsTable); },
    get frqSubmissionsTr() { return this.getHelper(config.IDs.frqSubmissionsTr); },
    get teamMembers() { return this.getHelper(config.IDs.teamMembers); },
    get aboutNav() { return this.getHelper(config.IDs.aboutNav); },
    get scoreboardNav() { return this.getHelper(config.IDs.scoreboardNav); },
    get writtenNav() { return this.getHelper(config.IDs.writtenNav); },
    get handsOnNav() { return this.getHelper(config.IDs.handsOnNav); },
    get signUpBox() { return this.getHelper(config.IDs.signUpBox); },
    get teamCode() { return this.getHelper(config.IDs.teamCode); },
    get toggleCreateTeam() { return this.getHelper(config.IDs.toggleCreateTeam); },
    classes: {
        cached: {},
        getHelper: function (className) {
            if (this.cached[className] == null)
                this.cached[className] = document.getElementsByClassName(className);
            return this.cached[className];
        },
        get columns() { return this.getHelper(config.CLASSES.columns); },
        get secondNavItems() { return this.getHelper(config.CLASSES.secondNavItem); }
    }
};
var cid = null; // Undefined if we are looking at the UIL list
$(document).ready(function () {
    showColumn();
});
var ws;
(function () {
    var sPageURL = window.location.search.substring(1), sURLVariables = sPageURL.split('&'), sParameterName, i;
    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === "cid") {
            cid = sParameterName[1] === undefined ? null : decodeURIComponent(sParameterName[1]);
        }
    }
    ws = new WebSocket("wss://" + window.location.host + "/compsocket/" + cid);
    ws.onmessage = function (evt) {
        try {
            var msg = JSON.parse(evt.data);
            config.SOCKET_FUNCTIONS[msg[0]](msg);
        }
        catch (e) { }
    };
})();
function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}
function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}
function showColumn() {
    // Check if there is an anchor, and if there is show that section of the page
    if (window.location.hash === "#scoreboard") {
        showScoreboard();
    }
    else if (window.location.hash === "#mc") {
        showMC();
    }
    else if (window.location.hash === "#frq") {
        showFRQ();
    }
    else {
        showAbout();
    }
}
function showHelper(show, hash, navDom) {
    var columns = dom.classes.columns;
    for (var i = 0, j = columns.length; i < j; i++) {
        columns.item(i).style.display = "none";
    }
    if (navDom != null) {
        columns = dom.classes.secondNavItems;
        for (var i = 0, j = columns.length; i < j; i++) {
            columns.item(i).classList.remove("navSelected");
        }
        navDom.classList.add("navSelected");
    }
    show.style.display = "";
    window.location.hash = hash;
}
function showAbout() {
    showHelper(dom.about, "#about", dom.aboutNav);
}
function showScoreboard() {
    showHelper(dom.scoreboard, "#scoreboard", dom.scoreboardNav);
}
function showMC() {
    showHelper(dom.mc, "#mc", dom.writtenNav);
}
function showFRQ() {
    showHelper(dom.frq, "#frq", dom.handsOnNav);
}
/*function showAnswers(){
    showHelper(answers, "#answers");
}*/
// Uses ajax to update the navigation bar. Called whenever a countdown finished
function updateNav() {
    location.reload();
}
// Show the signup box
function showSignup() {
    dom.signUpBox.style.display = "block";
}
function hideSignup() {
    dom.signUpBox.style.display = "none";
}
// Switches between joining a team and creating a team
var jointeamShowing = true;
function toggleCreateTeam(event) {
    console.log("testing");
    if (jointeamShowing) { // Switch to creating a team
        dom.signUpBox.querySelector("h1").innerText = "Create Team";
        dom.signUpBox.querySelector(".instruction").innerHTML = "Team Name";
        dom.signUpBox.querySelector("input").value = "";
        dom.teamCode.maxLength = "25";
        dom.teamCode.oninput = null;
        dom.teamCode.classList.add("creatingTeam");
        var button = document.createElement("button");
        button.onclick = createTeam;
        button.classList.add("chngButton");
        button.innerText = "Create";
        dom.toggleCreateTeam.innerHTML = "";
        dom.toggleCreateTeam.appendChild(button);
        var span = document.createElement("span");
        span.onclick = function (event) {
            toggleCreateTeam(event);
        };
        span.innerText = "or join a team.";
        dom.toggleCreateTeam.appendChild(span);
        dom.toggleCreateTeam.onclick = null;
    }
    else { // Switch to joining a team
        dom.signUpBox.querySelector("h1").innerText = "Join Team";
        dom.signUpBox.querySelector(".instruction").innerHTML = "Enter team join code:";
        dom.signUpBox.querySelector("input").value = "";
        dom.teamCode.maxLength = "6";
        dom.teamCode.oninput = function () {
            codeEntered(dom.teamCode);
        };
        dom.teamCode.classList.remove("creatingTeam");
        dom.toggleCreateTeam.innerHTML = "or create a team.";
        dom.toggleCreateTeam.onclick = function (event) {
            toggleCreateTeam(event);
        };
    }
    jointeamShowing = !jointeamShowing;
    event.stopPropagation();
}
function createTeam() {
    addSignupSuccessBox("Creating team...");
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "createteam", "cid": cid, "tname": $("#teamCode").val() },
        success: function (result) {
            if (result == null || result["status"] === "error")
                $("#signUp").replaceWith("<p id='signUpMsg' class='error_text'>An error occurred, try again later.</p>");
            if (result["status"] === "success") {
                location.reload();
            }
        }
    });
}
// Begin the multiple choice
function beginMC() {
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "beginMC" },
        success: function (result) {
            if (result != null) {
                if (result["status"] === "success") {
                    dom.mc.replaceWith(result["mcHTML"]);
                    delete dom.cached[config.IDs.mc];
                    dom.mc.style.display = "block";
                    delete dom.classes.cached[config.CLASSES.columns];
                }
            }
        }
    });
}
var choices = {}; // MC choices. Not necessarily full
function setChoice(question, dom) {
    var $dom = $(dom);
    var choice = dom.dataset.val;
    if (choices[question] === choice) { // They are clicking a selected bubble
        choices[question] = null;
        $dom.removeClass("mcSelected");
    }
    else {
        choices[question] = choice;
        $dom.parent().parent().children().children().removeClass("mcSelected");
        $dom.addClass("mcSelected");
    }
}
function setSAQChoice(question, dom) {
    choices[question] = dom.value;
}
function submitMC() {
    var numQuestions = document.getElementsByClassName("mcQuestion").length;
    // var answers = "[";
    var answers = [];
    for (var i = 1; i <= numQuestions; i++) {
        if (choices[i] != null && choices[i].trim().length > 0)
            answers.push(choices[i]);
        else
            answers.push('jieKYL'); // The character if they skipped it
    }
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "submitMC", "answers": JSON.stringify(answers) },
        success: function (result) {
            if (result != null) {
                var template = document.createElement('template');
                template.innerHTML = result["mcHTML"];
                dom.mc.replaceWith(template.content.firstChild);
                clearInterval(xmcTestTimer);
                delete dom.cached[config.IDs.mc];
                dom.mc.style.display = "block";
                delete dom.cached[config.CLASSES.columns];
            }
        }
    });
}
// Updates everything except for the nav bars
function updatePage() {
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "updatePage" },
        success: function (result) {
            if (result != null) {
                $("#columns").innerHTML(result["updatedHTML"]);
            }
        }
    });
}
/**
 * FRQ-Specific functions
 * @param box
 * @param success
 */
function grabFRQProblems() {
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "grabFRQProblems" },
        success: function (result) {
            if (result != null) {
                var template = document.createElement('template');
                template.innerHTML = result["frqProblemsHTML"];
                dom.frqProblems.replaceWith(template.content.firstChild);
            }
        }
    });
}
function beginFRQ() {
    // Tell the server that they've started
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "beginFRQ" },
        success: function (result) {
            if (result != null && result["status"] == "success") {
                dom.frq.replaceWith(result["frqHTML"]);
                delete dom.cached[config.IDs.frq];
                delete dom.classes.cached[config.CLASSES.columns];
                showColumn();
            }
        }
    });
    delete dom.cached[config.IDs.frqProblems];
    return false;
}
function addScoredBox(box, success) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}
function addErrorBox(box, error) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='error' id='" + box.id + "ERROR'>" + error + "</div>");
    }
    else {
        errorBox.innerHTML = "" + error;
        errorBox.className = "error";
    }
}
var box = null;
function submitFRQ() {
    if (!box)
        box = document.getElementById("submit");
    addScoredBox(box, "Scoring...");
    var probSelector = document.getElementById("frqProblem");
    var probId = probSelector.options[probSelector.selectedIndex].value;
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                var response = JSON.parse(xhr.responseText);
                if (response["status"] == "success") {
                    addScoredBox(box, "SCORED: " + response["scored"]);
                    $("frqProblem" + probId).hide();
                }
                else {
                    addErrorBox(box, response["error"]);
                }
                // grabFRQProblems();
            }
            else { // A server error occurred. Show an error message
                addErrorBox(box, "Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    };
    xhr.open('POST', window.location.href, true);
    // xhr.setRequestHeader('Content-type', 'multipart/form-data');
    var formData = new FormData();
    formData.append("textfile", document.getElementById("frqTextfile").files[0]);
    formData.append("probNum", probId);
    formData.append("action", "submitFRQ");
    xhr.send(formData);
    return false;
}
function finishFRQ() {
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "finishFRQ" },
        success: function (result) {
            if (result != null) {
                dom.frq.replaceWith(result["frqHTML"]);
                delete dom.cached[config.CLASSES.columns];
            }
        }
    });
}
function leaveTeam() {
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "leaveTeam" },
        success: function (result) {
            if (result != null && result["status"] === "success") {
                window.location.reload();
            }
        }
    });
}
/*function submitChallenge(){
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
}*/
var errorBox;
function addSignupErrorBox(error) {
    if (!errorBox) {
        document.getElementById("errorBoxERROR").insertAdjacentHTML('afterbegin', "<div class='error' id='errorBox'>ERROR: " + error + "</div>");
        errorBox = document.getElementsByClassName("error")[0];
    }
    else {
        errorBox.innerHTML = "ERROR: " + error;
        errorBox.className = "error";
    }
}
function addSignupSuccessBox(success) {
    if (!errorBox) {
        document.getElementById("errorBoxERROR").insertAdjacentHTML('afterbegin', "<div class='success' id='errorBox'>" + success + "</div>");
        errorBox = document.getElementsByClassName("success")[0];
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}
function codeEntered(code) {
    if (code.value.length == 6) { // If the code is fully entered
        // First, put a "verifying" box
        addSignupSuccessBox("Joining...");
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status == 200) { // If an error occurred
                    var response = JSON.parse(xhr.responseText);
                    // @ts-ignore
                    if (Object.keys(response).includes("reload")) {
                        window.location.reload();
                    }
                    else {
                        addSignupErrorBox(response["error"]);
                    }
                }
                else { // A server error occurred. Show an error message
                    addSignupErrorBox("Whoops! A server error occurred. Contact an admin if the problem continues.");
                }
            }
        };
        xhr.open('POST', '/console/competitions', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send('cid=' + cid + '&action=jointeam&code=' + code.value);
    }
}
function changeMCJudgement(element, tid, uid, probNum) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=changeMCJudgement&uid=" + uid + "&tid=" + tid + "&judgement=" + element.value + "&probNum=" + probNum);
    // document.getElementById("showFRQSubmission"+submissionId).innerText = result_cnt_changeJudgement.options[result_cnt_changeJudgement.selectedIndex].text;
}
/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
var submissionMap = {};
var showingFRQSubmission = null;
function showFRQSubmission(submissionId) {
    function add(element) {
        if (!showingFRQSubmission) {
            dom.frq.appendChild(element);
        }
        else {
            showingFRQSubmission.replaceWith(element);
        }
        showingFRQSubmission = element;
    }
    if (submissionMap[submissionId] != null) {
        add(submissionMap[submissionId]);
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                var name_1 = response["name"];
                var team = response["team"];
                var result = response["result"];
                var div = document.createElement("div");
                div.classList.add("frqSubmissionEditor");
                var probName_cnt = document.createElement("div");
                probName_cnt.innerHTML = "<b>Problem</b><h2>" + name_1 + "</h2>";
                probName_cnt.classList.add("half");
                div.appendChild(probName_cnt);
                var teamName_cnt = document.createElement("div");
                teamName_cnt.innerHTML = "<b>Team</b><h2>" + team + "</h2>";
                teamName_cnt.classList.add("half");
                div.appendChild(teamName_cnt);
                var result_cnt = document.createElement("p");
                result_cnt.classList.add("resultCnt");
                result_cnt.innerHTML = "<b>Judgement:</b>";
                div.appendChild(result_cnt);
                var result_cnt_changeJudgement_1 = document.createElement("select");
                result_cnt_changeJudgement_1.onchange = function () {
                    var xhr = new XMLHttpRequest();
                    xhr.open('POST', "/console/competitions", true);
                    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                    xhr.send("cid=" + cid + "&action=changeFRQJudgement&id=" + submissionId + "&judgeId=" + result_cnt_changeJudgement_1.value);
                    document.getElementById("showFRQSubmission" + submissionId).innerText = result_cnt_changeJudgement_1.options[result_cnt_changeJudgement_1.selectedIndex].text;
                };
                for (var i = 0, j = config.RESULTS.length; i < j; i++) {
                    var text = config.RESULTS[i];
                    var option = document.createElement("option");
                    option.value = "" + i;
                    option.innerText = text;
                    if (text == result)
                        option.selected = true;
                    result_cnt_changeJudgement_1.appendChild(option);
                }
                result_cnt.appendChild(result_cnt_changeJudgement_1);
                var input = response["input"];
                if (input) {
                    var input_cnt = document.createElement("div");
                    input_cnt.classList.add("inputCnt");
                    input_cnt.innerHTML = "<b>Input</b><span>" + input.replace(/\r\n/g, "<br>")
                        .replace(/\n/g, "<br>").replace(/\t/g, "<div class='tab'></div>") + "</span>";
                    div.appendChild(input_cnt);
                    var output = response["output"];
                    if (output) {
                        var output_cnt = document.createElement("div");
                        output_cnt.classList.add("outputCnt");
                        output_cnt.innerHTML = "<b>Output</b><span>" + output.replace(/\r\n/g, "<br>")
                            .replace(/\n/g, "<br>").replace(/\t/g, "<div class='tab'></div>") + "</span>";
                        div.appendChild(output_cnt);
                    }
                }
                add(div);
                submissionMap[submissionId] = div;
            }
        }
    };
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=showFRQSubmission&id=" + submissionId);
}
/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
var mcSubmissionMap = {};
var showingMCSubmission = null;
function showMCSubmission(tid, uid) {
    function add(element) {
        if (!showingMCSubmission) {
            dom.mc.appendChild(element);
        }
        else {
            showingMCSubmission.replaceWith(element);
        }
        showingMCSubmission = element;
    }
    if (mcSubmissionMap[tid] != null && mcSubmissionMap[tid][uid] != null) {
        add(mcSubmissionMap[tid][uid]);
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                var name_2 = response["user"];
                var team = response["team"];
                var scoringReport = JSON.parse(response["scoringReport"]);
                var div = document.createElement("div");
                div.classList.add("frqSubmissionEditor");
                div.classList.add("mcSubmissionEditor");
                var probName_cnt = document.createElement("div");
                probName_cnt.innerHTML = "<b>User</b><h2>" + name_2 + "</h2>";
                probName_cnt.classList.add("half");
                div.appendChild(probName_cnt);
                var teamName_cnt = document.createElement("div");
                teamName_cnt.innerHTML = "<b>Team</b><h2>" + team + "</h2>";
                teamName_cnt.classList.add("half");
                div.appendChild(teamName_cnt);
                var scoring_cnt = document.createElement("div");
                scoring_cnt.classList.add("mcScoring_cnt");
                scoring_cnt.innerHTML = "<b>Score:</b>" + scoringReport[0] + "<br><b>Correct:</b>" + scoringReport[1] + "" +
                    "<br><b>Incorrect:</b>" + scoringReport[3] + "<br><b>Skipped:</b>" + scoringReport[2];
                div.appendChild(scoring_cnt);
                var result_cnt = document.createElement("p");
                result_cnt.classList.add("resultCnt");
                result_cnt.innerHTML = "<b>Judgement:</b>";
                div.appendChild(result_cnt);
                var test = response["answers"];
                var submission_cnt = document.createElement("div");
                submission_cnt.classList.add("mcSubmissionCnt_teacher");
                submission_cnt.innerHTML = test;
                div.appendChild(submission_cnt);
                add(div);
                if (submissionMap[tid] == null)
                    submissionMap[tid] = {};
                submissionMap[tid][uid] = div;
            }
        }
    };
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=showMCSubmission&tid=" + tid + "&uid=" + uid);
}
