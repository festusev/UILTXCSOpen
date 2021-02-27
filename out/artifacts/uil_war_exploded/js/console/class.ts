///<reference path="../websocket.ts"/>
const config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        Class : "class",
        teamList : "teamList",
        studentList : "studentList",
        right : "right"
    },
    SOCKET_FUNCTIONS: {
        "updateStudentList" : function(response: Object) {
            let template = document.createElement('template');
            template.innerHTML = response["html"];
            let child = template.content.firstChild;
            dom.class.replaceWith(child);
            dom.cached.class = child;
        }, "showJoinClass" : function(response: Object) {
            showJoinClass();
        }, "setTID" : function(response: Object) {  // We just created a team, so set its tid
            let tid: number = response["tid"];
            let reference: number = response["reference"];  // The reference # to this team object
            teamList[reference].tid = tid;
        }
    }
};

(function() {
    getWebSocket(window.location.host + "/console/sockets/class", config.SOCKET_FUNCTIONS);
})();

/***
 * Helps interfacing with static elements (are not deleted *
 */
let dom = {
    cached : {},    // DOM objects that have already been accessed
    getHelper(id): HTMLElement {
        if(this.cached[id] == null) this.cached[id] = document.getElementById(id);
        return this.cached[id];
    },
    get class(){return this.getHelper(config.IDs.Class)},
    get teamList(){return this.getHelper(config.IDs.teamList)},
    get studentList(){return this.getHelper(config.IDs.studentList)},
    get right(){return this.getHelper(config.IDs.right)}
};

let pageState = {
    dragging : {
        uid : 0,    // uid of the student being dragged
        name : "",  // name of the student being dragged
        origin : null,
        isDragging : false
    }
};

function addErrorBox(box:HTMLElement, error:string): void{
    let errorBox:HTMLElement = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='error' id='" + box.id + "ERROR'>" + error + "</div>");
    }
    else {
        errorBox.innerHTML = "" + error;
        errorBox.className = "error";
    }
}
function addSuccessBox(box:HTMLElement, success:string):void {
    let errorBox:HTMLElement = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.innerHTML = success;
        errorBox.className = "success";
    }
}

function joinClass(classCode_input: HTMLInputElement) {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                let response = JSON.parse(xhr.responseText);
                if(response["error"] != null) {
                    window.location.reload();
                } else {
                    dom.right.innerHTML = "";
                    let template = document.createElement('template');
                    template.innerHTML = response["html"];
                    let child = template.content.firstChild;
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
function kickStudent(element:HTMLSpanElement, uid: number) {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=kickStudent&uid=" + uid);

    dom.studentList.removeChild(element.parentNode);

    for(let team of teamList) {
        if(uid == team.altUid) {
            team.altUid = -1;
            team.dom.alts.removeChild(team.dom.alts.lastChild);
        } else {
            let nonAltChildNodes = team.dom.nonAlts.childNodes;
            let i=0;
            nonAltChildNodes.forEach(function(item) {
                if((<HTMLElement>item).dataset.uid == "" + pageState.dragging.uid) {
                    team.nonAltUIDs.slice(i,1);
                    team.dom.nonAlts.removeChild(item);
                }
                i++;
            });
        }
    }

    return false;
}

function leaveClass() {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=leaveClass");
    showJoinClass();
}


function showJoinClass() {
    dom.right.innerHTML = "";

    let center = document.createElement("div");
    center.classList.add("center");
    dom.right.appendChild(center);

    let classDOM = document.createElement("class");
    classDOM.id = "class";
    center.appendChild(classDOM);
    dom.cached.class = classDOM;

    dom.class.innerHTML = "<h2>Join a Class</h2>";
    dom.class.classList.add("join");
    let classCode = document.createElement("div");
    classCode.classList.add("profile_cmpnt");
    classCode.classList.add("half");
    dom.class.appendChild(classCode);

    let classCode_input = document.createElement("input");
    classCode_input.classList.add("classCodeInput");
    classCode_input.placeholder = "Code";
    classCode_input.maxLength = 6;
    classCode.appendChild(classCode_input);

    let joinClass_div = document.createElement("div");
    joinClass_div.classList.add("profile_cmpnt");
    joinClass_div.classList.add("full");
    dom.class.appendChild(joinClass_div);

    let joinClass_button = document.createElement("button");
    joinClass_button.onclick = function() {
        joinClass(classCode_input);
    };
    joinClass_button.innerText = "Join";
    joinClass_div.appendChild(joinClass_button);
}

// The list of the teams in this class. The index is this team's reference, which is used so that the server can identify
// a team without a tid. If they delete a team, it isn't popped, it's set to null so that the indices don't break.
let teamList: Team[] = [];
class Team {
    reference: number;  // The index of this in the teamList variable.
    tid: number;

    nonAltUIDs: number[];
    altUid: number;

    dom: {
        name : HTMLInputElement,
        li: HTMLLIElement
        nonAlts : HTMLSpanElement,
        alts : HTMLSpanElement
    } = {
        name : null,
        li : null,
        nonAlts : null,
        alts : null
    };

    constructor(tid: number, primaryUids: number[], altUid: number) {
        this.tid = tid;
        this.nonAltUIDs = primaryUids;
        this.altUid = altUid;
        this.reference = teamList.length;
        teamList.push(this);
    }

    /*
    Saves a team that has already been created.
     */
    save(): void {
        let data:{action: string, tid: number, name: string, nonAlts:number[], alt: number} = {action: "save", tid:this.tid,
            name : this.dom.name.value, "nonAlts": this.nonAltUIDs, "alt": this.altUid};
        ws.send(JSON.stringify(data));
    }

    create(): void {
        let data:{action: string,reference: number, name: string, nonAlts:number[], alt: number} = {action: "create",
             reference: this.reference, name : this.dom.name.value, "nonAlts": this.nonAltUIDs, "alt": this.altUid};
        ws.send(JSON.stringify(data));
    }

    // Deletes the team
    delete(): void {
        dom.teamList.removeChild(this.dom.li);
        teamList[this.reference] = null;    // We don't pop it so that the references don't mess up
        if(this.tid >= 0) {  // If this team has been stored in the database
            let data:{action: string, tid: number} = {action: "delete", tid: this.tid};
            ws.send(JSON.stringify(data));
        }

        let empty: boolean = true;
        for(let team of teamList) { // Check if there are no teams left. If there aren't, add in the empty message
            if(team) empty = false;
        }

        if(empty) {
            dom.teamList.innerHTML = emptyMessage;
            teamList.length = 0 ;
        }
    }

    static initialize(teamData: {tid: number, name: string, nonAlt: {name: string, uid:number}[], alt: {name: string, uid:number}}): Team {
        let teamLi = document.createElement("li");
        teamLi.classList.add("team");

        let teamNameInput = document.createElement("input");
        teamNameInput.placeholder = "Team Name";
        teamNameInput.value = teamData.name;
        teamNameInput.classList.add("teamName");
        teamLi.appendChild(teamNameInput);

        let deleteTeam;
        if(isTeacher) {
            deleteTeam = document.createElement("button");
            deleteTeam.innerText = "Delete";
            deleteTeam.classList.add("kick");

            teamLi.appendChild(deleteTeam);
        } else {
            teamNameInput.disabled = true;
        }

        let ul = document.createElement("ul");

        /* Add in the non-alt students */
        let nonAltSpan = document.createElement("span");
        nonAltSpan.classList.add("drop_area");
        ul.appendChild(nonAltSpan);

        let nonAltHeader = document.createElement("h3");
        nonAltHeader.innerText = "Primaries";
        nonAltSpan.appendChild(nonAltHeader);

        let nonAltFragment = document.createDocumentFragment();

        let nonAltUIDs: number[] = [];  // basically nonAlt but without "name" in the objects
        let nonAltLIs: {uid: number, element:HTMLLIElement}[] = []; // This is used to later add in the onclick elements
        for(let student of teamData.nonAlt) {
            let li = document.createElement("li");
            li.classList.add("student");
            li.innerHTML = student.name;
            li.dataset.uid = ""+student.uid;
            nonAltFragment.appendChild(li);

            nonAltUIDs.push(student.uid);
            nonAltLIs.push({uid: student.uid, element: li});
        }

        nonAltSpan.appendChild(nonAltFragment);

        let addNonAltButton = document.createElement("button");
        addNonAltButton.classList.add("addButton");
        addNonAltButton.innerText = "+";
        addNonAltButton.onclick = event => {dropStudent(nonAltSpan)};
        ul.appendChild(addNonAltButton);

        /* Add in the alt student */
        let altSpan = document.createElement("span");
        altSpan.classList.add("drop_area");
        ul.appendChild(altSpan);

        let altHeader = document.createElement("h3");
        altHeader.innerText = "Written Specialist";
        altSpan.appendChild(altHeader);

        let altUID = -1;

        let altLI;
        if(teamData.alt) {
            altLI = document.createElement("li");
            altLI.classList.add("student");
            altLI.dataset.uid = ""+teamData.alt.uid;
            altLI.innerHTML = teamData.alt.name;
            altUID = teamData.alt.uid;
            altSpan.appendChild(altLI);
        } else {
            let temp = document.createElement("p");     // Used temporarily
            altSpan.append(temp);
        }

        let addAltButton = document.createElement("button");
        addAltButton.classList.add("addButton");
        addAltButton.innerText = "+";
        addAltButton.onclick = event => {
            dropStudent(altSpan);
        };
        ul.appendChild(addAltButton);

        teamLi.appendChild(ul);

        let team: Team = new Team(teamData.tid, nonAltUIDs, altUID);
        team.dom.name = teamNameInput;
        team.dom.li = teamLi;
        team.dom.nonAlts = nonAltSpan;
        team.dom.alts = altSpan;

        nonAltSpan.dataset.teamReference = ""+team.reference;
        nonAltSpan.dataset.type = "nonAlt"; // if it is nonAlt

        altSpan.dataset.teamReference = "" + team.reference;
        altSpan.dataset.type = "alt";

        if(isTeacher) {
            for (let item of nonAltLIs) {
                let deleteImg = document.createElement("img");  // This is the image of a trashcan on the student's name
                deleteImg.src = "/res/recycle-bin-line.svg";
                deleteImg.classList.add("trashcan");
                deleteImg.onclick = function() {
                    let newNonAltUIDs = [];
                    for(let uid of team.nonAltUIDs) {
                        if(uid != item.uid) newNonAltUIDs.push(uid);
                    }
                    team.nonAltUIDs = newNonAltUIDs;

                    team.dom.nonAlts.removeChild(item.element);
                    team.save();
                };
                item.element.appendChild(deleteImg);
            }

            if(teamData.alt) {
                let deleteImg = document.createElement("img");  // This is the image of a trashcan on the student's name
                deleteImg.src = "/res/recycle-bin-line.svg";
                deleteImg.classList.add("trashcan");
                deleteImg.onclick = function() {
                    team.altUid = -1;
                    team.dom.alts.removeChild(altLI);
                    team.save();
                };
                altLI.appendChild(deleteImg);
            }
        }

        if(isTeacher) {
            deleteTeam.onclick = function () {
                team.delete();
            };
        }

        let scheduledSave: boolean = false; // Whether or not we have set a timeout to schedule a save for 10 seconds in the future
        teamNameInput.onchange = function () {
            if(scheduledSave) return;
            scheduledSave = true;

            setTimeout(function() {
                team.save();
                scheduledSave = false;
            }, 10000);
        };

        return team;
    }
}

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
function beginDragStudent(event: MouseEvent, element: HTMLElement) {
    if(pageState.dragging.origin) pageState.dragging.origin.classList.remove("selected");

    element.classList.add("selected");

    dom.right.classList.add("dragging");
    pageState.dragging.isDragging = true;
    pageState.dragging.origin = element;
    pageState.dragging.name = element.innerText.slice(0,-5);
    pageState.dragging.uid = parseInt(element.dataset.uid);

    event.preventDefault();
}

// Drops a student onto the target
function dropStudent(target: HTMLElement): void {
    if(pageState.dragging.isDragging) {
        dom.right.classList.remove("dragging");

        pageState.dragging.isDragging = false;
        pageState.dragging.origin.classList.remove("selected");

        let team: Team = teamList[parseInt(target.dataset.teamReference)];

        let targetIsAlt: boolean = target.dataset.type == "alt";
        let targetIsNonAlt: boolean = target.dataset.type == "nonAlt";

        let alreadyAlt = false; // If the user we are dropping is already an alt
        let alreadyNonAlt = false;  // If they are already a non-alt
        if(team.altUid == pageState.dragging.uid) alreadyAlt = true;
        else {
            for(let uid of team.nonAltUIDs) {
                if(uid == pageState.dragging.uid) alreadyNonAlt = true;
            }
        }

        if(targetIsAlt && alreadyAlt || targetIsNonAlt && alreadyNonAlt) return;    // Don't do anything, this student is already here

        let li = document.createElement("li");
        li.classList.add("student");
        li.dataset.uid = ""+pageState.dragging.uid;
        li.innerHTML = pageState.dragging.name;

        let oldAlt: number = team.altUid;
        if(alreadyAlt) {   // This is already an alt, but now it should be a nonAlt
            team.altUid = -1;
            team.dom.alts.removeChild(team.dom.alts.lastChild);
        } else if(alreadyNonAlt) {  // This is already not an alt, but now it should be an alt
            let newNonAltUIDs: number[] = [];
            for(let uid of team.nonAltUIDs) {
                if(uid != pageState.dragging.uid) newNonAltUIDs.push(uid);
            }
            team.nonAltUIDs = newNonAltUIDs;

            let nonAltChildNodes = team.dom.nonAlts.childNodes;
            nonAltChildNodes.forEach(function(item) {
                if((<HTMLElement>item).dataset.uid == "" + pageState.dragging.uid) team.dom.nonAlts.removeChild(item);
            });
        }

        let deleteImg = document.createElement("img");  // This is the image of a trashcan on the student's name
        deleteImg.src = "/res/recycle-bin-line.svg";
        deleteImg.classList.add("trashcan");
        if(targetIsAlt) {
            if(oldAlt >= 0) target.removeChild(target.lastChild);
            team.altUid = pageState.dragging.uid;

            deleteImg.onclick = function() {
                team.altUid = -1;
                team.dom.alts.removeChild(li);
                team.save();
            };
        } else {
            team.nonAltUIDs.push(pageState.dragging.uid);

            deleteImg.onclick = function() {
                let newNonAltUIDs = [];
                for(let uid of team.nonAltUIDs) {
                    if(uid != pageState.dragging.uid) newNonAltUIDs.push(uid);
                }
                team.nonAltUIDs = newNonAltUIDs;

                team.dom.nonAlts.removeChild(li);
                team.save();
            };
        }
        li.appendChild(deleteImg);

        target.appendChild(li);
        team.save();
    }
}


/*
Adds an empty team.
 */
function newTeam() {
    if(teamList.length == 0) {   // There are no other teams, so get rid of the new team message
        dom.teamList.innerHTML = "";
    }

    let team = Team.initialize({"tid": -1, "name": "", nonAlt: [], alt: undefined});
    team.create();
    dom.teamList.appendChild(team.dom.li);
}


let emptyMessage:string = "No teams.";
/*
Loads all of the teams and students.
 */
let isTeacher: boolean = false;
function loadClass() {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                let response = JSON.parse(xhr.responseText);
                isTeacher  = response["teacher"];

                /*
                Add in the student list.
                 */
                let studentList:[] = response["studentList"];
                if(studentList.length > 0) {
                    let studentListFragment = document.createDocumentFragment();
                    for (let student of studentList) {   // student: {name: string, uid: short}
                        let li = document.createElement("li");
                        li.classList.add("student");
                        li.innerHTML = student["name"];
                        li.dataset.uid = student["uid"];

                        if (isTeacher) {
                            li.onclick = event => {
                                beginDragStudent(<MouseEvent>event, li);
                            };

                            let kick = document.createElement("span");
                            kick.classList.add("kick");
                            kick.innerText = "Kick";
                            kick.onclick = event => {
                                event.stopPropagation();
                                kickStudent(kick, student["uid"]);
                            };
                            li.appendChild(kick);
                        }

                        studentListFragment.appendChild(li);
                    }
                    dom.studentList.appendChild(studentListFragment);
                }

                /*
                Add in the team list
                 */
                if(isTeacher) emptyMessage = "Click 'New' to add a team.";

                let teamList:[] = response["teamList"];
                if(teamList.length == 0) {
                    dom.teamList.innerHTML = emptyMessage;
                } else {
                    let teamListFragment = document.createDocumentFragment();
                    for(let teamData of teamList) {
                        let team = Team.initialize(teamData);
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