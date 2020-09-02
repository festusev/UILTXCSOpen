const config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        about : "aboutColumn",
        scoreboard : "scoreboardColumn",
        team : "teamColumn",
        mc : "mcColumn",
        frq : "frqColumn",
        // answers : "answersColumn",
        publicComps : "public_competitions",
        classComps : "class_competitions",
        myComps : "my_competitions",
        frqProblems : "frqProblems"
    },
    CLASSES: {
        columns : "column"
    },
    RESULTS: ["Incorrect", "Correct", "No Penalty"]
};

let dom = {
    cached: {},    // DOM objects that have already been accessed
    getHelper(id): HTMLElement {
        if (this.cached[id] == null) this.cached[id] = document.getElementById(id);
        return this.cached[id];
    },
    get about() {return this.getHelper(config.IDs.about)},
    get scoreboard() {return this.getHelper(config.IDs.scoreboard)},
    get team() {return this.getHelper(config.IDs.team)},
    get mc() {return this.getHelper(config.IDs.mc)},
    get frq() {return this.getHelper(config.IDs.frq)},
    get publicComps() {return this.getHelper(config.IDs.publicComps)},
    get classComps() {return this.getHelper(config.IDs.classComps)},
    get myComps() {return this.getHelper(config.IDs.myComps)},
    get frqProblems() {return this.getHelper(config.IDs.frqProblems)},

    classes : {
        cached: {},    // DOM objects that have already been accessed
        getHelper(className): NodeList {
            if (this.cached[className] == null) this.cached[className] = document.getElementsByClassName(className);
            return this.cached[className];
        },
        get columns():NodeList {return this.getHelper(config.CLASSES.columns)}
    }
};

let cid = null;    // Undefined if we are looking at the UIL list

declare var $: any;
declare var xmcTestTimer: any;

$(document).ready(function(){
    let sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === "cid") {
            cid = sParameterName[1] === undefined ? null : decodeURIComponent(sParameterName[1]);
        }
    }

    showColumn();
});

function showColumn(){
    // Check if there is an anchor, and if there is show that section of the page
    if(window.location.hash==="#scoreboard") {
        showScoreboard();
    } else if(window.location.hash==="#mc") {
        showMC();
    } else if(window.location.hash==="#frq") {
        showFRQ();
    } /*else if(window.location.hash==="#answers") {
        showAnswers();
    } */else if(window.location.hash==="#team") {
        showTeam();
    } else if(cid != null) {
        showAbout();
    } else {
        showPublic();
    }
}

function showHelper(show, hash){
    let columns = dom.classes.columns;
    for(let i=0, j=columns.length;i<j;i++) {
        (<HTMLElement>columns.item(i)).style.display = "none";
    }

    show.style.display = "block";
    window.location.hash = hash;
}
function showAbout(){
    showHelper(dom.about, "#about");
}
function showScoreboard(){
    showHelper(dom.scoreboard, "#scoreboard");
}
function showTeam() {
    showHelper(dom.team, "#team");
}
function showMC(){
    showHelper(dom.mc, "#mc");
}
function showFRQ(){
    showHelper(dom.frq, "#frq");
}
/*function showAnswers(){
    showHelper(answers, "#answers");
}*/

function showPublic() { // Shows the public competitions
    showHelper(dom.publicComps, "");
}

function showClassComps() { // Shows the class competitions
    showHelper(dom.classComps, "");
}

function showMyComps() { // Shows the class competitions
    showHelper(dom.myComps, "");
}

// Uses ajax to update the navigation bar. Called whenever a countdown finished
function updateNav(){
    location.reload();
}

// Show the signup box
function showSignup() {
    $("#signUpBox").show();
}

// Switches between joining a team and creating a team
let jointeamShowing = true;
function toggleCreateTeam() {
    if(jointeamShowing) {   // Switch to creating a team
        $("#signUpBox h1").text("Create Team");
        $("#signUpBox .instruction").text("Team Name");
        $("#signUpBox input").val("");
        $("#teamCode").attr({"maxlength":"25", "oninput":"''"}).addClass("creatingTeam");
        $("#toggleCreateTeam").html("<button onclick='createTeam()' class='chngButton'>Create</button><span onclick='toggleCreateTeam()'>or join a team.</span>").click(function(){});
    } else {    // Switch to joining a team
        $("#signUpBox h1").text("Join Team");
        $("#signUpBox .instruction").text("Enter team join code:");
        $("#signUpBox input").val("");
        $("#teamCode").attr({"maxlength":"6", "oninput":"codeEntered(this)"}).removeClass("creatingTeam");
        $("#toggleCreateTeam").html("or create a team.").click(toggleCreateTeam);
    }
    jointeamShowing = !jointeamShowing;
}

function createTeam(){
    addSignupSuccessBox("Creating team...");
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "createteam", "cid": cid, "tname": $("#teamCode").val()},
        success: function(result) {
            if(result == null || result["status"]==="error")
                $("#signUp").replaceWith("<p id='signUpMsg' class='error_text'>An error occurred, try again later.</p>");
            if(result["status"] === "success") {
                location.reload();
            }
        }
    });
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
                    dom.mc.replaceWith(result["mcHTML"]);
                    delete dom.cached[config.IDs.mc];
                    dom.mc.style.display = "block";
                    delete dom.classes.cached[config.CLASSES.columns];
                }
            }
        }
    })
}


let choices = {};   // MC choices. Not necessarily full
function setChoice(question, dom) {
    let $dom = $(dom);
    let choice = dom.dataset.val;
    if(choices[question] === choice) {  // They are clicking a selected bubble
        choices[question] = null;
        $dom.removeClass("mcSelected");
        return;
    }
    choices[question] = choice;
    $dom.parent().parent().children().children().removeClass("mcSelected");
    $dom.addClass("mcSelected");
}

function setSAQChoice(question,dom) {
    choices[question] = dom.value;
}

function submitMC() {
    var numQuestions = document.getElementsByClassName("mcQuestion").length;
    var answers = "[";
    for(var i=1; i<=numQuestions; i++) {
        if(choices[i] != null && choices[i].trim().length > 0) answers+='"'+choices[i]+'"';
        else answers+='"SK"';  // The character if they skipped it
        if(i<numQuestions) answers +=",";
    }
    answers+="]";

    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "submitMC", "answers": answers},
        success: function(result) {
            if(result!=null){
                let template = document.createElement('template');
                template.innerHTML = result["mcHTML"];
                dom.mc.replaceWith(template.content.firstChild);

                clearInterval(xmcTestTimer);
                delete dom.cached[config.IDs.mc];
                dom.mc.style.display = "block";
                delete dom.cached[config.CLASSES.columns];
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
function grabFRQProblems(){
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "grabFRQProblems"},
        success: function(result) {
            if(result!=null){
                let template = document.createElement('template');
                template.innerHTML = result["frqProblemsHTML"];
                dom.frqProblems.replaceWith(template.content.firstChild);
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
var box = null;
function submitFRQ(){
    if(!box) box = document.getElementById("submit");
    addScoredBox(box, "Scoring...");

    var probSelector = <HTMLSelectElement>document.getElementById("frqProblem");
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
    formData.append("textfile", (<HTMLInputElement>document.getElementById("frqTextfile")).files[0]);
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
                dom.frq.replaceWith(result["frqHTML"]);
                delete dom.cached[config.CLASSES.columns];
            }
        }
    })
}

function leaveTeam() {
    $.ajax({
        url:window.location.href,
        method: "POST",
        data: {"action": "leaveTeam"},
        success: function(result) {
            if(result!=null && result["status"]==="success") {
                window.location.reload();
            }
        }
    })
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

let errorBox;
function addSignupErrorBox(error){
    if(!errorBox) {
        document.getElementById("errorBoxERROR").insertAdjacentHTML('afterbegin', "<div class='error' id='errorBox'>ERROR: " + error + "</div>");
        errorBox = document.getElementsByClassName("error")[0];
    }
    else {
        errorBox.innerHTML = "ERROR: " + error;
        errorBox.className = "error";
    }
}
function addSignupSuccessBox(success) {
    if(!errorBox) {
        document.getElementById("errorBoxERROR").insertAdjacentHTML('afterbegin', "<div class='success' id='errorBox'>" + success + "</div>");
        errorBox = document.getElementsByClassName("success")[0];
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}

function codeEntered(code) {
    if(code.value.length == 6) {   // If the code is fully entered
        // First, put a "verifying" box
        addSignupSuccessBox("Joining...");

        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(){
            if (xhr.readyState === 4) {
                if (xhr.status == 200) { // If an error occurred
                    var response = JSON.parse(xhr.responseText);
                    // @ts-ignore
                    if(Object.keys(response).includes("reload")) {
                        window.location.reload();
                    } else {
                        addSignupErrorBox(response["error"]);
                    }
                } else {    // A server error occurred. Show an error message
                    addSignupErrorBox("Whoops! A server error occurred. Contact an admin if the problem continues.");
                }
            }
        }
        xhr.open('POST', 'uil', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send('cid='+cid+'&action=jointeam&code=' + code.value);
    }
}

/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
let submissionMap : object = {};
let showingFRQSubmission:HTMLElement = null;
function showFRQSubmission(submissionId: number) {
    function add(element:HTMLElement) {
        if(!showingFRQSubmission) {
            dom.frq.insertAdjacentElement('afterbegin', element);
        } else {
            showingFRQSubmission.replaceWith(element);
        }
        showingFRQSubmission = element;
    }

    if(submissionMap[submissionId] != null) {
        add(submissionMap[submissionId]);
        return;
    }

    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response:object = JSON.parse(xhr.responseText);
                let name:string = response["name"];
                let team:string = response["team"];
                let result:string = response["result"];

                let div = document.createElement("div");
                div.classList.add("frqSubmissionEditor");

                let probName_cnt = document.createElement("div");
                probName_cnt.innerHTML = "<b>Problem</b><h2>"+name+"</h2>";
                probName_cnt.classList.add("half");
                div.appendChild(probName_cnt);

                let teamName_cnt = document.createElement("div");
                teamName_cnt.innerHTML = "<b>Team</b><h2>"+team+"</h2>";
                teamName_cnt.classList.add("half");
                div.appendChild(teamName_cnt);

                let result_cnt = document.createElement("p");
                result_cnt.classList.add("resultCnt");
                result_cnt.innerHTML = "<b>Judgement:</b>";
                div.appendChild(result_cnt);

                let result_cnt_changeJudgement = document.createElement("select");
                result_cnt_changeJudgement.onchange = function() {
                    let xhr:XMLHttpRequest = new XMLHttpRequest();
                    xhr.open('POST', "/uil", true);
                    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                    xhr.send("cid="+cid+"&action=changeFRQJudgement&id="+submissionId+"&judgeId="+result_cnt_changeJudgement.value);
                    document.getElementById("showFRQSubmission"+submissionId).innerText = result_cnt_changeJudgement.options[result_cnt_changeJudgement.selectedIndex].text;
                };

                for(let i=0,j = config.RESULTS.length; i<j;i++) {
                    let text = config.RESULTS[i];
                    let option = document.createElement("option");
                    option.value = ""+i;
                    option.innerText = text;
                    if(text == result) option.selected = true;

                    result_cnt_changeJudgement.appendChild(option);
                }
                result_cnt.appendChild(result_cnt_changeJudgement);

                let input = response["input"];
                if(input) {
                    let input_cnt = document.createElement("div");
                    input_cnt.classList.add("inputCnt");
                    input_cnt.innerHTML = "<b>Input</b><span>"+input.replace(/\r\n/g, "<br>")
                        .replace(/\n/g, "<br>").replace(/\t/g, "<div class='tab'></div>")+"</span>";
                    div.appendChild(input_cnt);

                    let output = response["output"];
                    if(output) {
                        let output_cnt = document.createElement("div");
                        output_cnt.classList.add("outputCnt");
                        output_cnt.innerHTML = "<b>Output</b><span>"+output.replace(/\r\n/g, "<br>")
                            .replace(/\n/g, "<br>").replace(/\t/g, "<div class='tab'></div>")+"</span>";
                        div.appendChild(output_cnt);
                    }
                }
                add(div);
                submissionMap[submissionId] = div;
            }
        }
    };
    xhr.open('POST', "/uil", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid="+cid+"&action=showFRQSubmission&id="+submissionId);
}


/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
let mcSubmissionMap : object = {};
let showingMCSubmission:HTMLElement = null;
function showMCSubmission(tid: number, uid: number) {
    function add(element:HTMLElement) {
        if(!showingMCSubmission) {
            dom.mc.insertAdjacentElement('afterbegin', element);
        } else {
            showingMCSubmission.replaceWith(element);
        }
        showingMCSubmission = element;
    }

    if(mcSubmissionMap[tid] != null && mcSubmissionMap[tid][uid] != null) {
        add(mcSubmissionMap[tid][uid]);
        return;
    }

    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response:object = JSON.parse(xhr.responseText);
                let name:string = response["user"];
                let team:string = response["team"];
                let scoringReport:[number, number, number, number] = JSON.parse(response["scoringReport"]);

                let div = document.createElement("div");
                div.classList.add("frqSubmissionEditor");
                div.classList.add("mcSubmissionEditor");

                let probName_cnt = document.createElement("div");
                probName_cnt.innerHTML = "<b>User</b><h2>"+name+"</h2>";
                probName_cnt.classList.add("half");
                div.appendChild(probName_cnt);

                let teamName_cnt = document.createElement("div");
                teamName_cnt.innerHTML = "<b>Team</b><h2>"+team+"</h2>";
                teamName_cnt.classList.add("half");
                div.appendChild(teamName_cnt);

                let scoring_cnt = document.createElement("div");
                scoring_cnt.classList.add("mcScoring_cnt");
                scoring_cnt.innerHTML = "<b>Score:</b>"+scoringReport[0]+"<br><b>Correct:</b>"+scoringReport[1]+"" +
                    "<br><b>Incorrect:</b>"+scoringReport[3]+"<br><b>Skipped:</b>"+scoringReport[2];
                div.appendChild(scoring_cnt);

                let result_cnt = document.createElement("p");
                result_cnt.classList.add("resultCnt");
                result_cnt.innerHTML = "<b>Judgement:</b>";
                div.appendChild(result_cnt);

                let test = response["answers"];
                let submission_cnt = document.createElement("div");
                submission_cnt.classList.add("mcSubmissionCnt_teacher");
                submission_cnt.innerHTML = test;
                div.appendChild(submission_cnt);

                add(div);
                if(submissionMap[tid] == null) submissionMap[tid] = {};
                submissionMap[tid][uid] = div;
            }
        }
    };
    xhr.open('POST', "/uil", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid="+cid+"&action=showMCSubmission&tid="+tid+"&uid="+uid);
}