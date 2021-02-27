///<reference path="../websocket.ts"/>
var config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        Class: "class",
        teamList: "teamList",
        studentList: "studentList",
        right: "right"
    },
    SOCKET_FUNCTIONS: {
        "updateStudentList": function (response) {
            var template = document.createElement('template');
            template.innerHTML = response["html"];
            var child = template.content.firstChild;
            dom.class.replaceWith(child);
            dom.cached.class = child;
        }, "showJoinClass": function (response) {
            showJoinClass();
        }, "setTID": function (response) {
            var tid = response["tid"];
            var reference = response["reference"]; // The reference # to this team object
            teamList[reference].tid = tid;
        }
    }
};
(function () {
    getWebSocket(window.location.host + "/console/sockets/class", config.SOCKET_FUNCTIONS);
})();
/***
 * Helps interfacing with static elements (are not deleted *
 */
var dom = {
    cached: {},
    getHelper: function (id) {
        if (this.cached[id] == null)
            this.cached[id] = document.getElementById(id);
        return this.cached[id];
    },
    get class() { return this.getHelper(config.IDs.Class); },
    get teamList() { return this.getHelper(config.IDs.teamList); },
    get studentList() { return this.getHelper(config.IDs.studentList); },
    get right() { return this.getHelper(config.IDs.right); }
};
var pageState = {
    dragging: {
        uid: 0,
        name: "",
        origin: null,
        isDragging: false
    }
};
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
function addSuccessBox(box, success) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}
function joinClass(classCode_input) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                if (response["error"] != null) {
                    window.location.reload();
                }
                else {
                    dom.right.innerHTML = "";
                    var template = document.createElement('template');
                    template.innerHTML = response["html"];
                    var child = template.content.firstChild;
                    dom.right.appendChild(child);
                    dom.cached = {};
                    dom.cached.class = child;
                    loadClass();
                }
            }
        }
    };
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=joinClass&code=" + classCode_input.value);
}
/***
 * Element is the actual "<span>Kick</span>" element.
 * @param event
 * @param element
 * @param uid
 */
function kickStudent(element, uid) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=kickStudent&uid=" + uid);
    dom.studentList.removeChild(element.parentNode);
    var _loop_1 = function (team) {
        if (uid == team.altUid) {
            team.altUid = -1;
            team.dom.alts.removeChild(team.dom.alts.lastChild);
        }
        else {
            var nonAltChildNodes = team.dom.nonAlts.childNodes;
            var i_1 = 0;
            nonAltChildNodes.forEach(function (item) {
                if (item.dataset.uid == "" + pageState.dragging.uid) {
                    team.nonAltUIDs.slice(i_1, 1);
                    team.dom.nonAlts.removeChild(item);
                }
                i_1++;
            });
        }
    };
    for (var _i = 0, teamList_1 = teamList; _i < teamList_1.length; _i++) {
        var team = teamList_1[_i];
        _loop_1(team);
    }
    return false;
}
function leaveClass() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=leaveClass");
    showJoinClass();
}
function showJoinClass() {
    dom.right.innerHTML = "";
    var center = document.createElement("div");
    center.classList.add("center");
    dom.right.appendChild(center);
    var classDOM = document.createElement("class");
    classDOM.id = "class";
    center.appendChild(classDOM);
    dom.cached.class = classDOM;
    dom.class.innerHTML = "<h2>Join a Class</h2>";
    dom.class.classList.add("join");
    var classCode = document.createElement("div");
    classCode.classList.add("profile_cmpnt");
    classCode.classList.add("half");
    dom.class.appendChild(classCode);
    var classCode_input = document.createElement("input");
    classCode_input.classList.add("classCodeInput");
    classCode_input.placeholder = "Code";
    classCode_input.maxLength = 6;
    classCode.appendChild(classCode_input);
    var joinClass_div = document.createElement("div");
    joinClass_div.classList.add("profile_cmpnt");
    joinClass_div.classList.add("full");
    dom.class.appendChild(joinClass_div);
    var joinClass_button = document.createElement("button");
    joinClass_button.onclick = function () {
        joinClass(classCode_input);
    };
    joinClass_button.innerText = "Join";
    joinClass_div.appendChild(joinClass_button);
}
// The list of the teams in this class. The index is this team's reference, which is used so that the server can identify
// a team without a tid. If they delete a team, it isn't popped, it's set to null so that the indices don't break.
var teamList = [];
var Team = /** @class */ (function () {
    function Team(tid, primaryUids, altUid) {
        this.dom = {
            name: null,
            li: null,
            nonAlts: null,
            alts: null
        };
        this.tid = tid;
        this.nonAltUIDs = primaryUids;
        this.altUid = altUid;
        this.reference = teamList.length;
        teamList.push(this);
    }
    /*
    Saves a team that has already been created.
     */
    Team.prototype.save = function () {
        var data = { action: "save", tid: this.tid,
            name: this.dom.name.value, "nonAlts": this.nonAltUIDs, "alt": this.altUid };
        ws.send(JSON.stringify(data));
    };
    Team.prototype.create = function () {
        var data = { action: "create",
            reference: this.reference, name: this.dom.name.value, "nonAlts": this.nonAltUIDs, "alt": this.altUid };
        ws.send(JSON.stringify(data));
    };
    // Deletes the team
    Team.prototype.delete = function () {
        dom.teamList.removeChild(this.dom.li);
        teamList[this.reference] = null; // We don't pop it so that the references don't mess up
        if (this.tid >= 0) { // If this team has been stored in the database
            var data = { action: "delete", tid: this.tid };
            ws.send(JSON.stringify(data));
        }
        var empty = true;
        for (var _i = 0, teamList_2 = teamList; _i < teamList_2.length; _i++) { // Check if there are no teams left. If there aren't, add in the empty message
            var team = teamList_2[_i];
            if (team)
                empty = false;
        }
        if (empty) {
            dom.teamList.innerHTML = emptyMessage;
            teamList.length = 0;
        }
    };
    Team.initialize = function (teamData) {
        var teamLi = document.createElement("li");
        teamLi.classList.add("team");
        var teamNameInput = document.createElement("input");
        teamNameInput.placeholder = "Team Name";
        teamNameInput.value = teamData.name;
        teamNameInput.classList.add("teamName");
        teamLi.appendChild(teamNameInput);
        var deleteTeam;
        if (isTeacher) {
            deleteTeam = document.createElement("button");
            deleteTeam.innerText = "Delete";
            deleteTeam.classList.add("kick");
            teamLi.appendChild(deleteTeam);
        }
        else {
            teamNameInput.disabled = true;
        }
        var ul = document.createElement("ul");
        /* Add in the non-alt students */
        var nonAltSpan = document.createElement("span");
        nonAltSpan.classList.add("drop_area");
        ul.appendChild(nonAltSpan);
        var nonAltHeader = document.createElement("h3");
        nonAltHeader.innerText = "Primaries";
        nonAltSpan.appendChild(nonAltHeader);
        var nonAltFragment = document.createDocumentFragment();
        var nonAltUIDs = []; // basically nonAlt but without "name" in the objects
        var nonAltLIs = []; // This is used to later add in the onclick elements
        for (var _i = 0, _a = teamData.nonAlt; _i < _a.length; _i++) {
            var student = _a[_i];
            var li = document.createElement("li");
            li.classList.add("student");
            li.innerHTML = student.name;
            li.dataset.uid = "" + student.uid;
            nonAltFragment.appendChild(li);
            nonAltUIDs.push(student.uid);
            nonAltLIs.push({ uid: student.uid, element: li });
        }
        nonAltSpan.appendChild(nonAltFragment);
        var addNonAltButton = document.createElement("button");
        addNonAltButton.classList.add("addButton");
        addNonAltButton.innerText = "+";
        addNonAltButton.onclick = function (event) { dropStudent(nonAltSpan); };
        ul.appendChild(addNonAltButton);
        /* Add in the alt student */
        var altSpan = document.createElement("span");
        altSpan.classList.add("drop_area");
        ul.appendChild(altSpan);
        var altHeader = document.createElement("h3");
        altHeader.innerText = "Written Specialist";
        altSpan.appendChild(altHeader);
        var altUID = -1;
        var altLI;
        if (teamData.alt) {
            altLI = document.createElement("li");
            altLI.classList.add("student");
            altLI.dataset.uid = "" + teamData.alt.uid;
            altLI.innerHTML = teamData.alt.name;
            altUID = teamData.alt.uid;
            altSpan.appendChild(altLI);
        }
        else {
            var temp = document.createElement("p"); // Used temporarily
            altSpan.append(temp);
        }
        var addAltButton = document.createElement("button");
        addAltButton.classList.add("addButton");
        addAltButton.innerText = "+";
        addAltButton.onclick = function (event) {
            dropStudent(altSpan);
        };
        ul.appendChild(addAltButton);
        teamLi.appendChild(ul);
        var team = new Team(teamData.tid, nonAltUIDs, altUID);
        team.dom.name = teamNameInput;
        team.dom.li = teamLi;
        team.dom.nonAlts = nonAltSpan;
        team.dom.alts = altSpan;
        nonAltSpan.dataset.teamReference = "" + team.reference;
        nonAltSpan.dataset.type = "nonAlt"; // if it is nonAlt
        altSpan.dataset.teamReference = "" + team.reference;
        altSpan.dataset.type = "alt";
        if (isTeacher) {
            var _loop_2 = function (item) {
                var deleteImg = document.createElement("img"); // This is the image of a trashcan on the student's name
                deleteImg.src = "/res/recycle-bin-line.svg";
                deleteImg.classList.add("trashcan");
                deleteImg.onclick = function () {
                    var newNonAltUIDs = [];
                    for (var _i = 0, _a = team.nonAltUIDs; _i < _a.length; _i++) {
                        var uid = _a[_i];
                        if (uid != item.uid)
                            newNonAltUIDs.push(uid);
                    }
                    team.nonAltUIDs = newNonAltUIDs;
                    team.dom.nonAlts.removeChild(item.element);
                    team.save();
                };
                item.element.appendChild(deleteImg);
            };
            for (var _b = 0, nonAltLIs_1 = nonAltLIs; _b < nonAltLIs_1.length; _b++) {
                var item = nonAltLIs_1[_b];
                _loop_2(item);
            }
            if (teamData.alt) {
                var deleteImg = document.createElement("img"); // This is the image of a trashcan on the student's name
                deleteImg.src = "/res/recycle-bin-line.svg";
                deleteImg.classList.add("trashcan");
                deleteImg.onclick = function () {
                    team.altUid = -1;
                    team.dom.alts.removeChild(altLI);
                    team.save();
                };
                altLI.appendChild(deleteImg);
            }
        }
        if (isTeacher) {
            deleteTeam.onclick = function () {
                team.delete();
            };
        }
        var scheduledSave = false; // Whether or not we have set a timeout to schedule a save for 10 seconds in the future
        teamNameInput.onchange = function () {
            if (scheduledSave)
                return;
            scheduledSave = true;
            setTimeout(function () {
                team.save();
                scheduledSave = false;
            }, 10000);
        };
        return team;
    };
    return Team;
}());
/***
 * Dragging functions.
 */
/*function dragStudent(event: MouseEvent) {
    if(pageState.dragging.isDragging) {
        pageState.dragging.element.left = (event.clientX - 24) + "px";
        pageState.dragging.element.top = (event.clientY - 24) + "px";
    }
}*/
// element is the student <li> element that is being dragged
function beginDragStudent(event, element) {
    if (pageState.dragging.origin)
        pageState.dragging.origin.classList.remove("selected");
    element.classList.add("selected");
    dom.right.classList.add("dragging");
    pageState.dragging.isDragging = true;
    pageState.dragging.origin = element;
    pageState.dragging.name = element.innerText.slice(0, -5);
    pageState.dragging.uid = parseInt(element.dataset.uid);
    event.preventDefault();
}
// Drops a student onto the target
function dropStudent(target) {
    if (pageState.dragging.isDragging) {
        dom.right.classList.remove("dragging");
        pageState.dragging.isDragging = false;
        pageState.dragging.origin.classList.remove("selected");
        var team_1 = teamList[parseInt(target.dataset.teamReference)];
        var targetIsAlt = target.dataset.type == "alt";
        var targetIsNonAlt = target.dataset.type == "nonAlt";
        var alreadyAlt = false; // If the user we are dropping is already an alt
        var alreadyNonAlt = false; // If they are already a non-alt
        if (team_1.altUid == pageState.dragging.uid)
            alreadyAlt = true;
        else {
            for (var _i = 0, _a = team_1.nonAltUIDs; _i < _a.length; _i++) {
                var uid = _a[_i];
                if (uid == pageState.dragging.uid)
                    alreadyNonAlt = true;
            }
        }
        if (targetIsAlt && alreadyAlt || targetIsNonAlt && alreadyNonAlt)
            return; // Don't do anything, this student is already here
        var li_1 = document.createElement("li");
        li_1.classList.add("student");
        li_1.dataset.uid = "" + pageState.dragging.uid;
        li_1.innerHTML = pageState.dragging.name;
        var oldAlt = team_1.altUid;
        if (alreadyAlt) { // This is already an alt, but now it should be a nonAlt
            team_1.altUid = -1;
            team_1.dom.alts.removeChild(team_1.dom.alts.lastChild);
        }
        else if (alreadyNonAlt) { // This is already not an alt, but now it should be an alt
            var newNonAltUIDs = [];
            for (var _b = 0, _c = team_1.nonAltUIDs; _b < _c.length; _b++) {
                var uid = _c[_b];
                if (uid != pageState.dragging.uid)
                    newNonAltUIDs.push(uid);
            }
            team_1.nonAltUIDs = newNonAltUIDs;
            var nonAltChildNodes = team_1.dom.nonAlts.childNodes;
            nonAltChildNodes.forEach(function (item) {
                if (item.dataset.uid == "" + pageState.dragging.uid)
                    team_1.dom.nonAlts.removeChild(item);
            });
        }
        var deleteImg = document.createElement("img"); // This is the image of a trashcan on the student's name
        deleteImg.src = "/res/recycle-bin-line.svg";
        deleteImg.classList.add("trashcan");
        if (targetIsAlt) {
            if (oldAlt >= 0)
                target.removeChild(target.lastChild);
            team_1.altUid = pageState.dragging.uid;
            deleteImg.onclick = function () {
                team_1.altUid = -1;
                team_1.dom.alts.removeChild(li_1);
                team_1.save();
            };
        }
        else {
            team_1.nonAltUIDs.push(pageState.dragging.uid);
            deleteImg.onclick = function () {
                var newNonAltUIDs = [];
                for (var _i = 0, _a = team_1.nonAltUIDs; _i < _a.length; _i++) {
                    var uid = _a[_i];
                    if (uid != pageState.dragging.uid)
                        newNonAltUIDs.push(uid);
                }
                team_1.nonAltUIDs = newNonAltUIDs;
                team_1.dom.nonAlts.removeChild(li_1);
                team_1.save();
            };
        }
        li_1.appendChild(deleteImg);
        target.appendChild(li_1);
        team_1.save();
    }
}
/*
Adds an empty team.
 */
function newTeam() {
    if (teamList.length == 0) { // There are no other teams, so get rid of the new team message
        dom.teamList.innerHTML = "";
    }
    var team = Team.initialize({ "tid": -1, "name": "", nonAlt: [], alt: undefined });
    team.create();
    dom.teamList.appendChild(team.dom.li);
}
var emptyMessage = "No teams.";
/*
Loads all of the teams and students.
 */
var isTeacher = false;
function loadClass() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.responseText);
                isTeacher = response["teacher"];
                /*
                Add in the student list.
                 */
                var studentList = response["studentList"];
                if (studentList.length > 0) {
                    var studentListFragment = document.createDocumentFragment();
                    var _loop_3 = function (student) {
                        var li = document.createElement("li");
                        li.classList.add("student");
                        li.innerHTML = student["name"];
                        li.dataset.uid = student["uid"];
                        if (isTeacher) {
                            li.onclick = function (event) {
                                beginDragStudent(event, li);
                            };
                            var kick_1 = document.createElement("span");
                            kick_1.classList.add("kick");
                            kick_1.innerText = "Kick";
                            kick_1.onclick = function (event) {
                                event.stopPropagation();
                                kickStudent(kick_1, student["uid"]);
                            };
                            li.appendChild(kick_1);
                        }
                        studentListFragment.appendChild(li);
                    };
                    for (var _i = 0, studentList_1 = studentList; _i < studentList_1.length; _i++) {
                        var student = studentList_1[_i];
                        _loop_3(student);
                    }
                    dom.studentList.appendChild(studentListFragment);
                }
                /*
                Add in the team list
                 */
                if (isTeacher)
                    emptyMessage = "Click 'New' to add a team.";
                var teamList_4 = response["teamList"];
                if (teamList_4.length == 0) {
                    dom.teamList.innerHTML = emptyMessage;
                }
                else {
                    var teamListFragment = document.createDocumentFragment();
                    for (var _a = 0, teamList_3 = teamList_4; _a < teamList_3.length; _a++) {
                        var teamData = teamList_3[_a];
                        var team = Team.initialize(teamData);
                        teamListFragment.appendChild(team.dom.li);
                        /*let teamLi = document.createElement("li");
                        teamLi.innerHTML = "<input class='teamName' placeholder='teamName' value='"+team["name"]+"'>";

                        let ul = document.createElement("ul");
                        let nonAlt:[] = response["nonAlt"];  // The list of non-alternates
                        let nonAltFragment = document.createDocumentFragment();
                        for(let student of nonAlt) {
                            let li = document.createElement("li");
                            li.classList.add("student");
                            li.innerHTML = student["name"];
                            nonAltFragment.appendChild(li);
                        }
                        ul.appendChild(nonAltFragment);

                        let altHeader = document.createElement("h2");
                        altHeader.innerText = "Alternate";
                        ul.appendChild(altHeader);
                        if(team["alt"]) {
                            let li = document.createElement("li");
                            li.classList.add("student");
                            li.innerHTML = team["alt"]["name"];
                            ul.appendChild(li);
                        }
                        teamLi.appendChild(ul);

                        teamListFragment.appendChild(teamLi);*/
                    }
                    dom.teamList.append(teamListFragment);
                }
            }
        }
    };
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=loadClass");
}
