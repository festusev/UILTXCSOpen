///<reference path="../websocket.ts"/>
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
        clarificationColumn : "clarificationsColumn",
        // answers : "answersColumn",
        publicComps : "public_competitions",
        classComps : "class_competitions",
        myComps : "my_competitions",
        frqProblems : "frqProblems",
        mcSubmissions : "mcSubmissions",
        mcSubmissionsTr : "mcSubmissionsTr",
        frqSubmissionsTable : "frqSubmissionsTable",
        frqSubmissionsTr : "frqSubmissionsTr",
        teamMembers : "teamMembers",
        aboutNav : "aboutNav",
        scoreboardNav : "scoreboardNav",
        writtenNav : "writtenNav",
        handsOnNav : "handsOnNav",
        clarificationNav : "clarificationNav",
        signUpBox : "signUpBox",
        teamCode : "teamCode",
        toggleCreateTeam : "toggleCreateTeam",
        clarification_input : "clarification_input"
    },
    CLASSES: {
        columns : "column",
        secondNavItem : 'secondNavItem'
    },
    RESULTS: ["Incorrect", "Correct", "No Penalty"],
    SOCKET_FUNCTIONS: { // The functions that can be called when the server sends a message using the web socket.
        "addSmallMC" : function(response:{[k:string]:any}) {
            let html:string = response["html"];
            let template = document.createElement('template');
            template.innerHTML = html;

            dom.mcSubmissions.insertBefore(template.content.firstChild, dom.mcSubmissionsTr.nextSibling);
        }, "addSmallFRQ" : function(response:{[k:string]:any}) {
            let html:string = response["html"];
            let template = document.createElement('template');
            template.innerHTML = html;

            let row = dom.frqSubmissionsTable.insertRow(1);
            row.replaceWith(template.content.firstChild);
        }, "updateTeam" : function(response:{[k:string]:any}) {
            dom.teamMembers.innerHTML = response["html"];
        }, "competitionDeleted" : function(response:string[]) {   // The competition was deleted, so go to the uil list
            window.location.href = "http://" + window.location.host + "/console/competitions";
        }, "updateFRQProblems" : function(response:{[k:string]:any}) {
            let template = document.createElement('template');
            template.innerHTML = response["html"];
            dom.frqProblems.replaceWith(template.content.firstChild);
        }, "updateScoreboard" : function(response:{[k:string]:any}) {
            let template = document.createElement('template');
            template.innerHTML = response["html"];
            let display:string = dom.scoreboard.style.display;
            dom.scoreboard.replaceWith(template.content.firstChild);
            dom.cached[config.IDs.scoreboard] = null;
            showColumn();
        }, "reScoreMC" : function(response:{[k:string]:any}) {
            // TODO: Write this
        }, "nc" : function(response:{[k:string]:any}) { // Add in a new clarification with no response
            let clarification_list:HTMLElement = dom.clarificationColumn.querySelector(".clarification_group");
            if(clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }

            clarification_list.innerHTML = "<div class='clarification'><h3>Question - "+response["name"]+"</h3><span>"+response["question"]+"</span><h3>Answer</h3><span>" +
                "<textarea placeholder='Send a response.'></textarea><button class='chngButton' onclick='answerClarification(this, "+
                response["id"]+")'>Send</button></span></div>" + clarification_list.innerHTML;
        }, "ac" : function(response:{[k:string]:any}) { // Add in a new clarification with a response
            let clarification_list:HTMLElement = dom.clarificationColumn.querySelector(".clarification_group");
            if(clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }

            clarification_list.innerHTML = "<div class='clarification'><h3>Question</h3><span>"+response["question"]+"</span><h3>Answer</h3><span>" +
                response["answer"] + "</span></div>" + clarification_list.innerHTML;
        }
    }
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
    get mcSubmissions() {return this.getHelper(config.IDs.mcSubmissions)},
    get mcSubmissionsTr() {return this.getHelper(config.IDs.mcSubmissionsTr)},
    get frqSubmissionsTable() {return this.getHelper(config.IDs.frqSubmissionsTable)},
    get frqSubmissionsTr() {return this.getHelper(config.IDs.frqSubmissionsTr)},
    get teamMembers() {return this.getHelper(config.IDs.teamMembers)},
    get aboutNav() {return this.getHelper(config.IDs.aboutNav)},
    get scoreboardNav() {return this.getHelper(config.IDs.scoreboardNav)},
    get writtenNav() {return this.getHelper(config.IDs.writtenNav)},
    get handsOnNav() {return this.getHelper(config.IDs.handsOnNav)},
    get signUpBox() {return this.getHelper(config.IDs.signUpBox)},
    get teamCode() {return this.getHelper(config.IDs.teamCode)},
    get toggleCreateTeam() {return this.getHelper(config.IDs.toggleCreateTeam)},
    get clarificationColumn() {return this.getHelper(config.IDs.clarificationColumn)},
    get clarificationNav() {return this.getHelper(config.IDs.clarificationNav)},
    get clarification_input() {return this.getHelper(config.IDs.clarification_input)},

    classes : {
        cached: {},    // DOM objects that have already been accessed
        getHelper(className): NodeList {
            if (this.cached[className] == null) this.cached[className] = document.getElementsByClassName(className);
            return this.cached[className];
        },
        get columns():NodeList {return this.getHelper(config.CLASSES.columns)},
        get secondNavItems():NodeList {return this.getHelper(config.CLASSES.secondNavItem)}
    }
};

let cid = null;    // Undefined if we are looking at the UIL list

declare var $: any;
declare var xmcTestTimer: any;

$(document).ready(function(){
    showColumn();

    // Set the multiple choice answers that are saved
    let cookie = getCookie(cid+"MC");
    if(cookie != null) {
        let savedMC:{[key:string]:string} = null;
        try {
            savedMC = JSON.parse(cookie);
        } catch(e) {return;}

        let questionDOMs = document.getElementsByClassName("mcQuestion");
        if(questionDOMs.length <= 0) return;

        let aCharCode = "a".charCodeAt(0);

        for(let questionNumberString in savedMC) {
            let questionDOM:HTMLTableRowElement = <HTMLTableRowElement>questionDOMs.item(parseInt(questionNumberString)-1);

            let savedMCAnswer = savedMC[questionNumberString];
            if(savedMCAnswer == null) continue;
            let tableCell:HTMLTableCellElement = <HTMLTableCellElement>questionDOM.childNodes.item(savedMCAnswer.charCodeAt(0) - aCharCode + 1);
            if(tableCell == null) continue;

            let div:HTMLDivElement = <HTMLDivElement>tableCell.firstChild;
            div.classList.add("mcSelected");
        }
        choices = savedMC;
        /* let $dom = $(dom);
        let choice = dom.dataset.val;
        if(choices[question] === choice) {  // They are clicking a selected bubble
            choices[question] = null;
            $dom.removeClass("mcSelected");
        } else {
            choices[question] = choice;
            $dom.parent().parent().children().children().removeClass("mcSelected");
            $dom.addClass("mcSelected");
        }*/
    }
});

(function() {
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

    getWebSocket(window.location.host + "/console/sockets/c/" + cid, config.SOCKET_FUNCTIONS);
})();

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i < ca.length; i++) {
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

function showColumn(){
    // Check if there is an anchor, and if there is show that section of the page
    try{
        if(window.location.hash==="#scoreboard") {
            showScoreboard();
        } else if(window.location.hash==="#mc") {
            showMC();
        } else if(window.location.hash==="#frq") {
            showFRQ();
        } else {
            showAbout();
        }
    } catch(e) {
        showAbout();
    }
}

function showHelper(show, hash, navDom?:HTMLElement){
    let columns = dom.classes.columns;
    for(let i=0, j=columns.length;i<j;i++) {
        (<HTMLElement>columns.item(i)).style.display = "none";
    }

    if(navDom != null) {
        columns = dom.classes.secondNavItems;
        for(let i=0, j=columns.length;i<j;i++) {
            (<HTMLElement>columns.item(i)).classList.remove("navSelected");
        }
        navDom.classList.add("navSelected");
    }

    show.style.display = "";
    window.location.hash = hash;
}
function showAbout(){
    showHelper(dom.about, "#about", dom.aboutNav);
}
function showScoreboard(){
    showHelper(dom.scoreboard, "#scoreboard", dom.scoreboardNav);
}

function showMC(){
    showHelper(dom.mc, "#mc", dom.writtenNav);
}
function showFRQ(){
    showHelper(dom.frq, "#frq", dom.handsOnNav);
}
function showClarifications() {
    showHelper(dom.clarificationColumn, "#clarifications", dom.clarificationNav);
}
/*function showAnswers(){
    showHelper(answers, "#answers");
}*/

// Uses ajax to update the navigation bar. Called whenever a countdown finished
function updateNav(){
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
let jointeamShowing = true;
function toggleCreateTeam(event:Event) {
    if(jointeamShowing) {   // Switch to creating a team
        dom.signUpBox.querySelector("h1").innerText = "Create Team";
        dom.signUpBox.querySelector(".instruction").innerHTML = "Team Name";
        dom.signUpBox.querySelector("input").value = "";

        dom.teamCode.maxLength = "25";
        dom.teamCode.oninput = null;
        dom.teamCode.classList.add("creatingTeam");

        let button = document.createElement("button");
        button.onclick = createTeam;
        button.classList.add("chngButton");
        button.innerText = "Create";
        dom.toggleCreateTeam.innerHTML = "";
        dom.toggleCreateTeam.appendChild(button);

        let span = document.createElement("span");
        span.onclick = function(event) {
            toggleCreateTeam(event);
        };
        span.innerText = "or join a team.";
        dom.toggleCreateTeam.appendChild(span);
        dom.toggleCreateTeam.onclick = null;
    } else {    // Switch to joining a team
        dom.signUpBox.querySelector("h1").innerText = "Join Team";
        dom.signUpBox.querySelector(".instruction").innerHTML = "Enter team join code:";
        dom.signUpBox.querySelector("input").value = "";
        dom.teamCode.maxLength = "6";
        dom.teamCode.oninput = function() {
            codeEntered(dom.teamCode);
        };
        dom.teamCode.classList.remove("creatingTeam");
        dom.toggleCreateTeam.innerHTML = "or create a team.";
        dom.toggleCreateTeam.onclick = function(event) {
            toggleCreateTeam(event);
        }
    }
    jointeamShowing = !jointeamShowing;
    event.stopPropagation();
}

function createTeam(){
    addSignupSuccessBox("Creating team...");
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "createteam", "cid": cid, "tname": $("#teamCode").val()},
        success: function(result) {
            if(result == null || result["status"]==="error")
                addSignupErrorBox(result["error"]);
            if(result["status"] === "success") {
                location.reload();
            }
        }
    });
}

function toggle_clarify() {

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


let choices:{[key:string]:string} = {};   // MC choices. Not necessarily full
function setChoice(question, dom) {
    let $dom = $(dom);
    let choice = dom.dataset.val;
    if(choices[question] === choice) {  // They are clicking a selected bubble
        choices[question] = null;
        $dom.removeClass("mcSelected");
    } else {
        choices[question] = choice;
        $dom.parent().parent().children().children().removeClass("mcSelected");
        $dom.addClass("mcSelected");
    }

    setCookie(cid+"MC", JSON.stringify(choices), 1);
}

function setSAQChoice(question,dom) {
    choices[question] = dom.value;
}

function submitMC(callback?:Function) {
    var numQuestions = document.getElementsByClassName("mcQuestion").length;
    // var answers = "[";
    let answers = [];
    for(var i=1; i<=numQuestions; i++) {
        if(choices[i] != null && choices[i].trim().length > 0) answers.push(choices[i]);
        else answers.push('jieKYL');  // The character if they skipped it
    }

    $.ajax({
        url: window.location.href,
        method: "POST",
        data: {"action": "submitMC", "answers": JSON.stringify(answers)},
        success: function(result) {
            if(result!=null){
                if(callback) callback();
                else {
                    let template = document.createElement('template');
                    template.innerHTML = result["mcHTML"];
                    dom.mc.replaceWith(template.content.firstChild);

                    //  clearInterval(xmcTestTimer);
                    delete dom.cached[config.IDs.mc];
                    dom.mc.style.display = "block";
                    delete dom.cached[config.CLASSES.columns];
                }
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
        box.insertAdjacentHTML('afterbegin', "<div class='error' id='" + box.id + "ERROR'>" + error + "</div>");
    }
    else {
        errorBox.innerHTML = "" + error;
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

                // grabFRQProblems();
            } else {    // A server error occurred. Show an error message
                addErrorBox(box, "Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    };
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
            if(result!=null) {
                let template = document.createElement('template');
                template.innerHTML = result["frqHTML"];
                dom.frq.replaceWith(template.firstChild);
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
        };
        xhr.open('POST', '/console/competitions', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send('cid='+cid+'&action=jointeam&code=' + code.value);
    }
}

function changeMCJudgement(element: HTMLSelectElement, tid: number, uid: number, probNum: number) {
    let xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid="+cid+"&action=changeMCJudgement&uid="+uid+"&tid="+tid+"&judgement="+element.value+"&probNum="+probNum);
    // document.getElementById("showFRQSubmission"+submissionId).innerText = result_cnt_changeJudgement.options[result_cnt_changeJudgement.selectedIndex].text;
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
            dom.frq.appendChild(element);
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
                    xhr.open('POST', "/console/competitions", true);
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
    xhr.open('POST', "/console/competitions", true);
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
            dom.mc.appendChild(element);
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
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid="+cid+"&action=showMCSubmission&tid="+tid+"&uid="+uid);
}


/***
 * Loads the clarifications that this user asked.
 */
function loadPersonalClarifications(): void {
    (<WebSocket>ws).send("Testing!");
}


/***
 * Lets a teacher answer a clarification.
 */
function answerClarification(button: HTMLButtonElement, id: number):void {
    let span:HTMLSpanElement = <HTMLSpanElement> button.parentNode;   // The <span> that holds the textarea and the button
    let textarea:HTMLTextAreaElement = span.getElementsByTagName("textarea")[0];

    let msg:string = textarea.value;

    if(msg.trim().length != 0 && msg.length <=255) {
        let response:string[] = ["rc", "" + id, msg];

        (<WebSocket>ws).send(JSON.stringify(response));

        span.innerHTML = msg;
    }
}

/***
 * Sends a clarification if the text box is not empty.
 */
function sendClarification(): void {
    let msg:string = dom.clarification_input.value;
    if(msg.trim().length != 0 && msg.length <=255) {
        let response:string[] = ["nc", msg];

        (<WebSocket>ws).send(JSON.stringify(response));
    }
    dom.clarification_input.value = "";
}

