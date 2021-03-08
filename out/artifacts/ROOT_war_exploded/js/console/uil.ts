///<reference path="../websocket.ts"/>


import Global = WebAssembly.Global;

let pageState = {
    isCreator : false,
    mcExists : false,
    frqExists : false,
    alternateExists : false,
    numNonAlts : 1,
    openTeam : null, // The team object that is currently open in "#teamCnt"
    editingTeam : false,   // Whether or not we are editing the currently open team
    addingAlt: false,    // If the user has clicked the "+" button for alternates on a team. If false, they are adding a primary
    // saveTeamList: [],    // The list of teams that also need to be saved when the openTeam is saved
    globalTeams: [],     // A list of the global teams
    globalTeamsLoaded: false,    // If the global teams have been loaded
    addingExistingTeam : false,
    existingTeam : null, // the existing team that we are currently adding. needed so that the createTeam function can work properly
    existingGlobalTeacher : null,    // The teacher of 'existingTeam'
    isDeletingTeam : false, // If we are deleting a team. If false, we are deleting the student
    deletingObject : null   // The object we are deleting
};

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
        clarification_input : "clarification_input",
        bottomRank : "bottomRank",
        bottomOutOf : "bottomOutOf",
        errorBoxERROR : "errorBoxERROR",

        // Signup info
        signUpIsAlternateCnt : "signUpIsAlternateCnt",
        signUpIsAlternate : "signUpIsAlternate",

        // Scoreboard IDs
        teamListCnt : "teamListCnt",
        teamCnt : "teamCnt",
        teamList : "teamList",  // the table of teams
        openTeamName : "openTeamName",
        openTeamWritten : "openTeamWritten",
        openTeamHandsOn : "openTeamHandsOn",
        openPrimariesList : "openPrimariesList",
        openAlternateList : "openAlternateList",
        editSaveTeam : "editSaveTeam",
        addPrimaryCompetitor : "addPrimaryCompetitor",
        addAlternateCompetitor : "addAlternateCompetitor",
        openTeamFeedbackCnt : "openTeamFeedbackCnt",
        openTeamCode : "openTeamCode",
        deleteMessage : "deleteMessage",
        deleteConfirmationCnt : "deleteConfirmationCnt",
        deleteSubtitle : "deleteSubtitle",
        deleteTeam : "deleteTeam",  // The trashcan button
        studentSearchTable : "studentSearchTable",

        // Select global team IDs
        selectGlobalTeam : "selectGlobalTeam",
        selectGlobalTeamList : "selectGlobalTeamList",

        // Scoreboard select student IDs
        studentSearch : "studentSearch",
        selectStudent : "selectStudent",
        selectStudentList : "selectStudentList",
        selectStudentFromClass : "selectStudentFromClass",
        selectSignedUpStudent : "selectSignedUpStudent",
        fnameTemp : "fnameTemp",
        lnameTemp : "lnameTemp",
        inputSchool : "inputSchool",

        // Right info bar IDs, for admins only
        writtenSubmissionCount : "writtenSubmissionCount",
        writtenAverage : "writtenAverage",
        handsOnSubmissionCount : "handsOnSubmissionCount",
        handsOnSubmissionAverage : "handsOnSubmissionAverage",
        numTeams : "numTeams",
        numUsers : "numUsers",

        downloadScoreboard : "downloadScoreboard"
    },
    CLASSES: {
        columns : "column",
        secondNavItem : 'secondNavItem'
    },
    RESULTS: ["Correct", "Incorrect", "Server Error", "Compile time Error", "Runtime Error",  "Empty File",
        "Time Limit Exceeded", "Unclear File Type", "Package Error", "Wrong Output Format"],

    SOCKET_FUNCTIONS: { // The functions that can be called when the server sends a message using the web socket.
        "addSmallMC": function (response: { [k: string]: any }) {
            let html: string = response["html"];
            let template = document.createElement('template');
            template.innerHTML = html;

            dom.mcSubmissions.firstChild.insertBefore(template.content.firstChild, dom.mcSubmissionsTr.nextSibling);
        }, "addSmallFRQ": function (response: { [k: string]: any }) {
            let html: string = response["html"];
            let template = document.createElement('template');
            template.innerHTML = html;

            let row = dom.frqSubmissionsTable.insertRow(1);
            row.replaceWith(template.content.firstChild);
        }, "updateTeam": function (response: { [k: string]: any }) {
            dom.teamMembers.innerHTML = response["html"];
        }, "competitionDeleted": function (response: string[]) {   // The competition was deleted, so go to the uil list
            window.location.href = "http://" + window.location.host + "/console/competitions";
        }, "updateFRQProblems": function (response: { [k: string]: any }) {
            let template = document.createElement('template');
            template.innerHTML = response["html"];
            dom.frqProblems.replaceWith(template.content.firstChild);
        }, "updateScoreboard": function (response: { [k: string]: any }) {
            let template = document.createElement('template');
            template.innerHTML = response["html"];
            let display: string = dom.scoreboard.style.display;
            dom.scoreboard.replaceWith(template.content.firstChild);
            dom.cached[config.IDs.scoreboard] = null;

            if (response["rank"]) {
                dom.bottomRank.innerText = response["rank"];
                dom.bottomOutOf.innerText = response["numTeams"];
            }


            showColumn();
        }, "reScoreMC": function (response: { [k: string]: any }) {
            // TODO: Write this
        }, "nc": function (response: { [k: string]: any }) { // Add in a new clarification with no response
            let clarification_list: HTMLElement = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }

            clarification_list.innerHTML = "<div class='clarification' id='clarification_"+response["index"]+"'><h3>Question - " + response["name"] + "</h3><span>" + response["question"] + "</span><h3>Answer</h3><span>" +
                "<textarea placeholder='Send a response.' maxlength='255' oninput='inputMaxLength(this,255)'></textarea><button class='chngButton' onclick='answerClarification(this, " +
                response["id"] + ")'>Send</button></span></div>" + clarification_list.innerHTML;
        }, "ac": function (response: { [k: string]: any }) { // Add in a new clarification with a response
            let clarification_list: HTMLElement = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }

            if(pageState.isCreator) {   // They are a creator, so check if the clarification already has a dom
                let clarificationDOM = document.getElementById("clarification_"+response["index"]);
                if(clarificationDOM != null) clarification_list.removeChild(clarificationDOM);
            }

            clarification_list.innerHTML = "<div class='clarification' id='clarification_"+response["index"]+"'><h3>Question</h3><span>" + response["question"] + "</span><h3>Answer</h3><span>" +
                response["answer"] + "</span></div>" + clarification_list.innerHTML;
        }, "loadScoreboard": function (response: {
            isCreator: boolean, mcExists: boolean, frqExists: boolean, alternateExists: boolean, numNonAlts: number,
            teams: { tname: string, school: string, tid: number, students: { nonAlts: [string,number,number?][],
                    alt?: [string,number,number?]}, frq?: number }[],
            numHandsOnSubmitted?: number, teamCodes? : string[], studentsInClass? : [string,number][], tempUsers?: {[uid:string]:[string,string]}
        }) {
            let newToggleTeam: Team = null;  // The new team object that we are toggling open
            let oldOpenTeamTID = -1;    // The tid of the old open team
            if(pageState.openTeam) oldOpenTeamTID = (<Team>pageState.openTeam).tid; // The old team that was open. May be null

            teams = {};
            pageState.isCreator = response.isCreator;
            pageState.mcExists = response.mcExists;
            pageState.frqExists = response.frqExists;
            pageState.alternateExists = response.alternateExists;
            pageState.numNonAlts = response.numNonAlts;

            // If they are the creator, display the "#editSaveTeam" image
            if (pageState.isCreator) {
                document.body.classList.add("isCreator");
            }

            let fragment = document.createDocumentFragment();   // A collection of table row elements

            dom.teamList.innerHTML = "";

            // First, add in the table headers
            let headers = "<th>Name</th>";
            if (pageState.mcExists) headers += "<th class='right'>Written</th>";
            if (pageState.frqExists) headers += "<th class='right'>Hands-On</th>";
            headers += "<th class='right'>Total</th>";

            let headerDOM = document.createElement("tr");
            headerDOM.innerHTML = headers;
            fragment.appendChild(headerDOM);

            // These datapoints are displayed on the right bar if the user is an admin
            let numWrittenSubmitted: number = 0;
            let writtenSum: number = 0; // Sum of written scores

            let handsOnSum: number = 0; // Sum of hands on scores
            let numStudents: number = 0;

            let selectStudentFragment = document.createDocumentFragment();  // The list of students that goes into the select student window
            for (let i=0,j=response.teams.length;i<j;i++) {
                let teamData = response.teams[i];
                let team: Team = new Team(teamData, response.tempUsers);
                if(team.tid == oldOpenTeamTID) newToggleTeam = team;

                if(pageState.isCreator) team.code = response.teamCodes[i];
                fragment.appendChild(team.dom.tr);

                for (let student of team.students) {
                    let tr = document.createElement("tr");
                    tr.classList.add("_"+student.uid);
                    tr.onclick = function () {
                        Team.selectStudent(student);
                    };
                    tr.innerHTML = "<td>" + student.name + "</td>";
                    selectStudentFragment.appendChild(tr);

                    if(student.mcScore) {
                        numWrittenSubmitted++;
                        writtenSum += student.mcScore;
                    }
                    numStudents++;
                }

                if (pageState.alternateExists && team.alt) {
                    let tr = document.createElement("tr");
                    tr.classList.add("_"+team.alt.uid);
                    tr.onclick = function () {
                        Team.selectStudent(team.alt)
                    };
                    tr.innerHTML = "<td>" + team.alt.name + "</td>";
                    selectStudentFragment.appendChild(tr);

                    if(team.alt.mcScore) {
                        numWrittenSubmitted++;
                        writtenSum += team.alt.mcScore;
                    }
                    numStudents++;
                }

                handsOnSum += team.frqScore;
            }

            if(response.isCreator) {
                if(pageState.mcExists) {
                    dom.writtenSubmissionCount.innerText = numWrittenSubmitted + " submitted";
                    if(numWrittenSubmitted == 0)
                        dom.writtenAverage.innerText = "0 average";
                    else
                        dom.writtenAverage.innerText = Math.round(writtenSum / numWrittenSubmitted) + " average";
                }
                if(pageState.frqExists) {
                    dom.handsOnSubmissionCount.innerText = "" + response.numHandsOnSubmitted;
                    if(response.teams.length == 0)
                        dom.handsOnSubmissionAverage.innerText = "0";
                    else
                        dom.handsOnSubmissionAverage.innerText = "" + Math.round(handsOnSum/response.teams.length);
                }
                dom.numTeams.innerText = "" + response.teams.length;
                dom.numUsers.innerText = "" + numStudents;
            }

            dom.teamList.innerHTML = "";
            dom.teamList.appendChild(fragment);

            if(pageState.isCreator) {
                let selectStudentFromClassFragment = document.createDocumentFragment(); // Add in the students from their class

                for(let classStudent of response.studentsInClass) {
                    let student:Student = Student.students[""+classStudent[1]];
                    if(student == null) {
                        student = new Student(classStudent);
                    }

                    let tr = document.createElement("tr");
                    tr.classList.add("_"+student.uid);
                    tr.onclick = function () {
                        Team.selectStudent(student);
                    };
                    tr.innerHTML = "<td>" + student.name + "</td>";
                    selectStudentFromClassFragment.appendChild(tr);
                }

                dom.selectStudentFromClass.innerHTML = "";
                dom.selectStudentFromClass.appendChild(selectStudentFromClassFragment);

                dom.selectSignedUpStudent.innerHTML = "";
                dom.selectSignedUpStudent.appendChild(selectStudentFragment);
            }

            if(!newToggleTeam) Team.toggleTeam(newToggleTeam);  // New toggle team is null, so close the dom.
            else {
                Team.renderOpenTeam(newToggleTeam);
                newToggleTeam.dom.tr.classList.add("selected");
            }
        }, "scoreboardOpenTeamFeedback": function (response: { isError: boolean, msg: string }) { // When there is an error or a success that has to do with editing a team
            if (response.isError) addErrorBox(dom.openTeamFeedbackCnt, response.msg);
            else addSuccessBox(dom.openTeamFeedbackCnt, response.msg);
        },
        // Add in the results for the student search. The student objects are in the form [name, uid, mcScore]
        "ssearch" : function (response: {students: [string, number, number?][]}) {
            let fragment = document.createDocumentFragment();
            for(let student of response.students) {
                let data:Student = Student.students[""+student[1]];
                if(data == null) {
                    data = new Student(student);
                }
                let tr = document.createElement("tr");
                tr.classList.add("_"+data.uid);
                tr.onclick = function () {
                    Team.selectStudent(data);
                };
                tr.innerHTML = "<td>" + data.name + "</td>";
                fragment.appendChild(tr);
            }

            dom.studentSearchTable.innerHTML = "";
            dom.studentSearchTable.appendChild(fragment);
        }, "loadGlobalTeams" : function(response: {action: string,teachers:{uid: number,uname:string,school:string,
                teams:{tid: number,tname:string,nonAlts:[string, number][],alt:[string,number]}[]}[]}) {
            if(pageState.globalTeamsLoaded) return;

            pageState.globalTeamsLoaded = true;
            let fragment = document.createDocumentFragment();
            for(let data of response.teachers) {
                let globalTeacher = new GlobalTeacher(data);
                fragment.appendChild(globalTeacher.getDOM());
            }

            dom.selectGlobalTeamList.appendChild(fragment);
        }, "addExistingTeam" : function(response: {error?:string, reload?: string, tid?:number
            /*tname: string, school: string, code: string,tid: number, students: { nonAlts: [string, number, number?][],
                alt?: [string, number, number?] }, frq?: number */}) { // The response to adding an existing team
            if(!pageState.isCreator) return;

            if(response.error) {
                addErrorBox(dom.errorBoxERROR, response.error);
                return;
            } else if(response.reload) {
                // let team: Team = new Team(response);
                // Team.toggleTeam(team);
                pageState.openTeam = {tid: response.tid};   // So that the toggle team works correctly when we reload the scoreboard
                requestLoadScoreboard();
                hideSignup();
                closeAddExistingTeam();
                return;
            } /* else {
                let team: Team = new Team(response);
                if(pageState.isCreator) team.code = response.code;

                dom.teamList.appendChild(team.dom.tr);

                let selectStudentFragment = document.createDocumentFragment();
                for (let student of response.students.nonAlts) {
                    let stuTeam: { team: Team, isAlt: boolean } = {team: team, isAlt: false};   //  This has to be an object so that the selectStudent method can modify it
                    let tr = document.createElement("tr");
                    tr.onclick = function () {
                        Team.selectStudent(student, stuTeam)
                    };
                    tr.innerHTML = "<td>" + student[0] + "</td>";
                    selectStudentFragment.appendChild(tr);
                }

                if (pageState.alternateExists && response.students.alt) {
                    let stuTeam: { team: Team, isAlt: boolean } = {team: team, isAlt: true};   //  This has to be an object so that the selectStudent method can modify it
                    let tr = document.createElement("tr");
                    tr.onclick = function () {
                        Team.selectStudent(response.students.alt, stuTeam)
                    };
                    tr.innerHTML = "<td>" + response.students.alt[0] + "</td>";
                    selectStudentFragment.appendChild(tr);
                }
                dom.selectStudentList.appendChild(selectStudentFragment);

                hideSignup();
                closeAddExistingTeam();

                Team.toggleTeam(team);
            }*/
        },
        // Adds a new temporary student to the team
        "addTempStudent" : function(response: {name: string, uname: string, password: string, isAlt:boolean, uid: number, tid:number}) {
            addSuccessBox(dom.openTeamFeedbackCnt, "Team saved successfully.");
            let team: Team = teams["" + response.tid];  // The team they are in
            let student: Student = new Student([response.name,response.uid]);
            student.temp = true;
            student.uname = response.uname;
            student.password = response.password;
            if(response.isAlt) {
                team.alt = student;
            } else {
                team.students.push(student);
            }
            if(pageState.openTeam.tid == team.tid) {
                Team.renderOpenTeam(team);
            }
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
    get errorBoxERROR() {return this.getHelper(config.IDs.errorBoxERROR)},
    get toggleCreateTeam() {return this.getHelper(config.IDs.toggleCreateTeam)},
    get clarificationColumn() {return this.getHelper(config.IDs.clarificationColumn)},
    get clarificationNav() {return this.getHelper(config.IDs.clarificationNav)},
    get clarification_input() {return this.getHelper(config.IDs.clarification_input)},
    get bottomRank() {return this.getHelper(config.IDs.bottomRank)},
    get bottomOutOf() {return this.getHelper(config.IDs.bottomOutOf)},
    get teamListCnt() {return this.getHelper(config.IDs.teamListCnt)},
    get teamCnt() {return this.getHelper(config.IDs.teamCnt)},
    get teamList() {return this.getHelper(config.IDs.teamList)},
    get openTeamName() {return this.getHelper(config.IDs.openTeamName)},
    get openTeamWritten() {return this.getHelper(config.IDs.openTeamWritten)},
    get openTeamHandsOn() {return this.getHelper(config.IDs.openTeamHandsOn)},
    get openPrimariesList() {return this.getHelper(config.IDs.openPrimariesList)},
    get openAlternateList() {return this.getHelper(config.IDs.openAlternateList)},
    get editSaveTeam() {return this.getHelper(config.IDs.editSaveTeam)},
    get addPrimaryCompetitor() {return this.getHelper(config.IDs.addPrimaryCompetitor)},
    get addAlternateCompetitor() {return this.getHelper(config.IDs.addAlternateCompetitor)},
    get selectStudent() {return this.getHelper(config.IDs.selectStudent)},
    get selectStudentList() {return this.getHelper(config.IDs.selectStudentList)},
    get openTeamFeedbackCnt() {return this.getHelper(config.IDs.openTeamFeedbackCnt)},
    get openTeamCode(){return this.getHelper(config.IDs.openTeamCode)},
    get selectGlobalTeam(){return this.getHelper(config.IDs.selectGlobalTeam)},
    get selectGlobalTeamList(){return this.getHelper(config.IDs.selectGlobalTeamList)},
    get deleteMessage() {return this.getHelper(config.IDs.deleteMessage)},
    get deleteConfirmationCnt() {return this.getHelper(config.IDs.deleteConfirmationCnt)},
    get deleteSubtitle() {return this.getHelper(config.IDs.deleteSubtitle)},
    get deleteTeam() {return this.getHelper(config.IDs.deleteTeam)},
    get signUpIsAlternateCnt() {return this.getHelper(config.IDs.signUpIsAlternateCnt)},
    get signUpIsAlternate() {return this.getHelper(config.IDs.signUpIsAlternate)},
    get writtenSubmissionCount() {return this.getHelper(config.IDs.writtenSubmissionCount)},
    get writtenAverage(){return this.getHelper(config.IDs.writtenAverage)},
    get handsOnSubmissionCount() {return this.getHelper(config.IDs.handsOnSubmissionCount)},
    get handsOnSubmissionAverage() {return this.getHelper(config.IDs.handsOnSubmissionAverage)},
    get numTeams() {return this.getHelper(config.IDs.numTeams)},
    get numUsers() {return this.getHelper(config.IDs.numUsers)},
    get selectStudentFromClass() {return this.getHelper(config.IDs.selectStudentFromClass)},
    get selectSignedUpStudent() {return this.getHelper(config.IDs.selectSignedUpStudent)},
    get studentSearch() {return this.getHelper(config.IDs.studentSearch)},
    get studentSearchTable() {return this.getHelper(config.IDs.studentSearchTable)},
    get downloadScoreboard() {return this.getHelper(config.IDs.downloadScoreboard)},
    get fnameTemp() {return this.getHelper(config.IDs.fnameTemp)},
    get lnameTemp() {return this.getHelper(config.IDs.lnameTemp)},
    get inputSchool() {return this.getHelper(config.IDs.inputSchool)},


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


let globalTeachers: GlobalTeacher[] = [];

// A global teacher that has created teams from their class. Students in these teams may not be signed up for this competition
class GlobalTeacher {
    teacher = {     // Data about the teacher for this team
        uid: -1,
        uname: "",
        school: "",
    };

    teams:{tid: number,tname:string,nonAlts:[string, number][],alt?:[string,number]}[] = [];


    constructor(data:{uid: number,uname:string,school:string,teams:{tid: number,tname:string,nonAlts:[string, number][],alt:[string,number]}[]}) {
        this.teacher.uid = data.uid;
        this.teacher.uname = data.uname;
        this.teacher.school = data.school;

        for(let team of data.teams) {
            this.teams.push(team);
        }

        globalTeachers.push(this);
    }

    // Gets the dom that is put into the global team list
    getDOM():HTMLLIElement {
        let li = document.createElement("li");
        li.classList.add("teacher");

        let teacherName = document.createElement("h2");
        teacherName.innerText = this.teacher.uname;
        li.appendChild(teacherName);

        let school = document.createElement("p");
        school.innerText = this.teacher.school;
        li.appendChild(school);

        let teamsHeader = document.createElement("b");
        teamsHeader.innerText = "Teams:";
        li.appendChild(teamsHeader);

        let teamList = document.createElement("ul");
        li.appendChild(teamList);

        let thisGlobalTeacher: GlobalTeacher = this;
        for(let team of this.teams) {   // {tid: number,nonAlts:[string, number][],alt:[string,number]}
            let teamLI = document.createElement("li");
            teamLI.classList.add("team");
            teamLI.onclick = function() {
                // closeAddExistingTeam();
                dom.teamCode.value = team.tname;
                pageState.existingTeam = team;
                pageState.existingGlobalTeacher = thisGlobalTeacher;
                showSignup();
            };

            let teamHeader = document.createElement("h3");
            teamHeader.innerText = team.tname;
            teamLI.appendChild(teamHeader);

            let studentUL = document.createElement("ul");
            studentUL.innerHTML = "<b>Primaries</b><br>";

            for(let student of team.nonAlts) {
                let studentLI = document.createElement("li");
                studentLI.classList.add("student");
                studentLI.innerText = student[0];
                studentUL.appendChild(studentLI);
            }

            let altHeader = document.createElement("b");
            altHeader.innerText = "Written Specialist";
            studentUL.appendChild(altHeader);

            if(team.alt) {
                let studentLI = document.createElement("li");
                studentLI.classList.add("student");
                studentLI.innerText = team.alt[0];
                studentUL.appendChild(studentLI);
            }

            teamLI.appendChild(studentUL);
            teamList.appendChild(teamLI);
        }

        return li;
    }
}


class Student {
    temp: boolean = false;  // If they are a temporary user
    name: string;
    uid: number;
    uname: string;  // If they are a temp
    password: string;   // If they are a temp
    mcScore:number;
    team: Team;
    static students: {[key:string]:Student} = {};
    constructor(data: [string,number,number?]) {
        this.name = data[0];
        this.uid = data[1];

        this.mcScore = <number>data[2];
    }
}

let teams: {[key:string]:Team} = {};    // Maps tids to their team
class Team {
    tname: string;
    school: string;
    tid: number;
    // First element is if they are a temporary user, second is their name, third is their uid, last is their mcScore.
    // If they are a temporary user, the fourth is their username, and the fifth is their password
    students: Student[];
    alt : Student;
    mcScore : number;
    frqScore: number;
    code : string;

    dom : {
        tr : HTMLTableRowElement,  // The table row in the scoreboard
        mcTD : HTMLTableCellElement    // The table cell that holds this team's written score
    } = {tr: null, mcTD: null};


    constructor(data: { tname: string, school: string, tid: number, students: { nonAlts: [string,number,number?][],
            alt?: [string,number,number?]}, frq?: number }, tempData?:{[uid:string]:[string,string]}) {
        function createStudent(data:[string,number,number?]):Student {
            let student: Student = new Student(data);
            if(pageState.isCreator) {   // If they are the creator, check if this student is a temporary user
                let temp = tempData[""+student.uid];    // First element is their username, second is their password
                if(temp) {
                    student.temp = true;
                    student.uname = temp[0];
                    student.password = temp[1];
                }
            }
            student.team = thisTeam;
            return student;
        }
        let thisTeam:Team = this;

        this.tname = data.tname;
        this.school = data.school;
        this.tid = data.tid;
        this.students = [];
        for(let studentData of data.students.nonAlts) {
            this.students.push(createStudent(studentData));
        }
        this.alt = null;
        if(data.students.alt) {
            this.alt = createStudent(data.students.alt);
        }
        if(pageState.frqExists) this.frqScore = data.frq;
        else this.frqScore = 0;
        this.mcScore = 0;

        if(pageState.mcExists) {
            let indexOfLowest = 0;
            let numSubmittedMCs = 0;    // The number of mc tests that this team has submitted
            let lowest = Number.MAX_VALUE;
            for(let i=0;i<this.students.length;i++) {
                let thisScore = this.students[i].mcScore;
                if(thisScore) {
                    if (thisScore < lowest) {
                        indexOfLowest = i;
                        lowest = thisScore;
                    }
                    numSubmittedMCs++;
                    this.mcScore += thisScore;
                }
            }

            if(pageState.alternateExists && this.alt && this.alt.mcScore) {  // This team has an alt that has submitted an mc test
                let thisScore = this.alt.mcScore;
                numSubmittedMCs++;

                if(thisScore >= lowest || numSubmittedMCs <= pageState.numNonAlts) {    // The alternate is not the lowest score or we are adding all of the scores
                    this.mcScore += thisScore;
                } else
                    numSubmittedMCs --;
            }

            if(numSubmittedMCs > pageState.numNonAlts) {   // In this case, remove the lowest score
                this.mcScore -= this.students[indexOfLowest].mcScore;
            }
        }

        teams[""+this.tid] = this;
        this.render();
    }


    // Creates the html objects
    render() {
        this.dom.tr = document.createElement("tr");
        this.dom.tr.classList.add("team");

        let tnameTD = document.createElement("td");
        tnameTD.innerText = this.tname;
        this.dom.tr.appendChild(tnameTD);

        if(pageState.mcExists) {
            let mcTD = document.createElement("td");
            mcTD.classList.add("right");
            mcTD.innerText = "" + this.mcScore;
            this.dom.mcTD = mcTD;
            this.dom.tr.appendChild(mcTD);
        }

        if(pageState.frqExists) {
            let frqTD = document.createElement("td");
            frqTD.classList.add("right");
            frqTD.innerText = "" + this.frqScore;
            this.dom.tr.appendChild(frqTD);
        }

        let totalTD = document.createElement("td");
        totalTD.classList.add("right");
        totalTD.innerText = "" + (this.mcScore + this.frqScore);
        this.dom.tr.appendChild(totalTD);

        let thisTeam: Team = this;
        this.dom.tr.onclick = function(){Team.toggleTeam(thisTeam);};
    }

    // Save the team and copy over the temporary information to the official information
    save() {
        function getTeamData(team: Team):{tid:number, nonAlts:number[], alt:number} {
            let nonAltUIDs = [];
            for(let student of team.students) {
                nonAltUIDs.push(student.uid);
            }
            let alt;
            if(team.alt) alt = team.alt.uid;
            else alt = -1;
            return {tid: team.tid, nonAlts: nonAltUIDs, alt: alt};
        }

        let data: [string, {tid:number, nonAlts:number[], alt:number}] = ["saveTeam", getTeamData(this)];

        /*for(let team of pageState.saveTeamList) {
            data[1].push(getTeamData(team));
        }*/

        // pageState.saveTeamList.length = 0;
        ws.send(JSON.stringify(data));

        addSuccessBox(dom.openTeamFeedbackCnt, "Saving team...");
    }

    delete() {
        teams[""+this.tid] = undefined;

        for(let student of this.students) { // Remove this from the selectStudents list
            let studentDOMs = dom.selectSignedUpStudent.querySelector("._"+student.uid);
            for(let studentDOM of studentDOMs) {
                dom.selectSignedUpStudent.removeChild(studentDOM);
            }
        }

        dom.teamList.removeChild(this.dom.tr);

        dom.teamCnt.style.display = "none";
        pageState.openTeam.dom.tr.classList.remove("selected");
        pageState.openTeam = null;

        ws.send("[\"deleteTeam\","+this.tid+"]");
    }

    deleteStudent(student: Student) {
        let studentDOM = dom.selectSignedUpStudent.getElementsByClassName("_"+student.uid)[0]; // Remove this from the selectStudents list
        if(studentDOM != null) {
            dom.selectSignedUpStudent.removeChild(studentDOM);
        }

        if(pageState.mcExists && student.mcScore) {
            this.mcScore -= student.mcScore;
            this.dom.mcTD.innerText = ""+this.mcScore;
        }

        for(let i=0;i<this.students.length;i++) {
            if(this.students[i].uid == student.uid) this.students.splice(i,1);
        }

        if(pageState.alternateExists && this.alt != null && this.alt.uid == student.uid) this.alt = null;

        Team.renderOpenTeam(this);
        this.save();

        // ws.send("[\"deleteStudent\","+this.tid+","+student.uid+"]");
    }

    static showDeleteConfirmation() {
        pageState.isDeletingTeam = true;
        pageState.deletingObject = pageState.openTeam;

        dom.deleteMessage.innerText = "Are you sure?";
        dom.deleteSubtitle.innerText = "Deleting this team, '" + pageState.openTeam.tname + "', will also remove all of its students from the competition. This action cannot be undone.";
        dom.deleteConfirmationCnt.style.display = "block";
    }

    // Opens the menu for adding a student as a primary competitor
    static addPrimaryCompetitor() {
        if(!pageState.isCreator) return;
        pageState.addingAlt = false;
        dom.selectStudent.style.display = "block";
    }

    static addAlternateCompetitor() {
        if(!pageState.isCreator) return;
        pageState.addingAlt = true;
        dom.selectStudent.style.display = "block";
    }

    static closeSelectStudent() {
        dom.selectStudent.style.display = "none";
    }

    static selectStudent(student: Student) {
        if(!pageState.isCreator) return;

        let openTeam: Team = pageState.openTeam;
        let selectedSuccessfully: boolean = (pageState.alternateExists && pageState.addingAlt && !openTeam.alt) ||
            (!pageState.addingAlt && openTeam.students.length < pageState.numNonAlts);  // If we are changing which team this student is on
        // This must come first, so that if the student is already in this team, this works
        // If team is null, they are adding a student from their class.
        if(selectedSuccessfully && student.team != null) {
            if(student.team.alt && student.uid==student.team.alt.uid) student.team.alt = null;
            else {
                let newStudentsList:Student[] = [];
                for(let s of student.team.students) {
                    if(s.uid != student.uid) newStudentsList.push(s);
                }
                student.team.students = newStudentsList;
            }
            // pageState.saveTeamList.push(teamData.team);
            student.team = openTeam;
        }
        if(pageState.alternateExists && pageState.addingAlt && !openTeam.alt) {
            openTeam.alt = student;
        } else if(!pageState.addingAlt && openTeam.students.length < pageState.numNonAlts) {
            let alreadyInTeam: boolean = false;
            for(let s of openTeam.students) {
                if(student.uid == s.uid) {
                    alreadyInTeam = true;
                    break;
                }
            }
            if(!alreadyInTeam) {
                pageState.openTeam.students.push(student);
            }
        }
        if(selectedSuccessfully) {
            Team.renderOpenTeam(openTeam);
            Team.editSaveTeam();
            Team.closeSelectStudent();
        }
    }

    static renderOpenTeam(team: Team) {
        // gets the table row for a student. [0] = uname, [1] = uid, [2] = mc score
        function getStudentTR(student: Student): HTMLTableRowElement {
            let tr = document.createElement("tr");
            tr.classList.add("team");

            let nameTD = document.createElement("td");

            let nameP:HTMLParagraphElement = document.createElement("p");
            nameP.innerText = student.name;
            nameTD.appendChild(nameP);

            if(student.temp) {
                let unameP = document.createElement("p");
                unameP.innerHTML = "<b>Username:</b>";

                let unameNode = document.createTextNode(student.uname);
                unameP.appendChild(unameNode);
                nameTD.appendChild(unameP);

                let passwordP = document.createElement("p");
                passwordP.innerHTML = "<b>Password:</b>";

                let passwordNode = document.createTextNode(student.password);
                passwordP.appendChild(passwordNode);
                nameTD.appendChild(passwordP);

            }
            tr.appendChild(nameTD);

            if(pageState.mcExists) {
                let mcTD = document.createElement("td");
                mcTD.classList.add("right");
                if(typeof student.mcScore != "undefined") mcTD.innerText = student.mcScore + "pts";
                else mcTD.innerText = "Not taken";
                tr.appendChild(mcTD);
            }

            let deleteTD = document.createElement("td");
            deleteTD.classList.add("editTeam");
            deleteTD.innerHTML = "<img src='/res/console/delete.svg' class='deleteStudent'/>";
            deleteTD.onclick = function() {
                pageState.isDeletingTeam = false;
                pageState.deletingObject = student;

                dom.deleteMessage.innerText = "Are you sure?";
                dom.deleteSubtitle.innerText = "Deleting this student, '" + student.name + "', will also remove them from the competition. This action cannot be undone.";
                dom.deleteConfirmationCnt.style.display = "block";
            };
            tr.appendChild(deleteTD);
            return tr;
        }

        dom.editSaveTeam.src = "/res/console/edit.svg";
        dom.teamCnt.classList.remove("editing");
        dom.addPrimaryCompetitor.style.display = "none";

        if(pageState.alternateExists) dom.addAlternateCompetitor.style.display = "none";

        dom.teamCnt.style.display = "block";
        dom.openTeamName.innerText = team.tname;
        if(pageState.isCreator) dom.openTeamCode.innerText = team.code;
        if(pageState.mcExists) dom.openTeamWritten.innerText = team.mcScore + " pts";
        if(pageState.frqExists) dom.openTeamHandsOn.innerText = team.frqScore + " pts";

        // Add in the primaries
        let fragment = document.createDocumentFragment();
        for(let student of team.students) {
            fragment.appendChild(getStudentTR(student));
        }

        dom.openPrimariesList.innerHTML = "";
        dom.openPrimariesList.appendChild(fragment);

        // Add in the alternate
        if(pageState.alternateExists) {
            dom.openAlternateList.innerHTML = "";
            if(team.alt) dom.openAlternateList.appendChild(getStudentTR(team.alt));
        }
        // dom.openAlternateList.appendChild(getStudentTR(team.))

        pageState.openTeam = team;
        pageState.editingTeam = false;
    }

    /*
     Toggles between looking at a team and not. If the given team object is null or equal to the currently open team,
     then close the 'teamCnt'. Otherwise, open it and set its information properly.
     */
    static toggleTeam(team: Team) {
        if(!team || team == pageState.openTeam) {   // Close the "#teamCnt"
            dom.teamCnt.style.display = "none";
            pageState.openTeam.dom.tr.classList.remove("selected");
            pageState.openTeam = null;

            requestLoadScoreboard();
        } else {
            deleteErrorSuccessBox(dom.openTeamFeedbackCnt);

            if(pageState.openTeam) {
                pageState.openTeam.dom.tr.classList.remove("selected");
                requestLoadScoreboard();
            }
            team.dom.tr.classList.add("selected");
            Team.renderOpenTeam(team);
        }
    }

    /*
    Toggles between editing and saving the open team.
     */
    static editSaveTeam() {
        if(!pageState.isCreator) return;

        if(pageState.openTeam) {
            if (pageState.editingTeam) {    // Save the team but do not leave editing mode
                pageState.openTeam.save();
            } else {    // Enter editing mode
                pageState.editingTeam = true;

                dom.editSaveTeam.src = "/res/console/save.svg";
                dom.teamCnt.classList.add("editing");

                if (pageState.openTeam.students.length < pageState.numNonAlts) dom.addPrimaryCompetitor.style.display = "block";
                if (pageState.alternateExists && !pageState.openTeam.alt) dom.addAlternateCompetitor.style.display = "block";
            }
        }
    }
}

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

// Starts the load scoreboard

// @ts-ignore
async function requestLoadScoreboard() {
    while (ws == null || ws.readyState === 0) {
        // @ts-ignore
        await new Promise(r => setTimeout(r, 100));
    }

    ws.send("[\"loadScoreboard\"]");
}

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

function deleteTeamOrStudent() {
    if(pageState.isDeletingTeam) {
        let team:Team = <Team>pageState.deletingObject;

        team.delete();
        closeDeleteConfirmation();
    } else {    // Deleting a student
        let student:Student = <Student> pageState.deletingObject;
        let team:Team = <Team>pageState.openTeam;

        team.deleteStudent(student);
        closeDeleteConfirmation();
    }
}

function closeDeleteConfirmation() {
    dom.deleteConfirmationCnt.style.display = "none";
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
    deleteErrorSuccessBox(dom.errorBoxERROR);
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


        dom.signUpIsAlternateCnt.style.display = "none";

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

        if(pageState.alternateExists || pageState.alternateExists == null) {    // If alternates exist or the scoreboard hasn't loaded
            dom.signUpIsAlternateCnt.style.display = "block";
        }

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
    addSuccessBox(dom.errorBoxERROR, "Creating team...");
    let data;
    if(pageState.addingExistingTeam) {
        let data = ["addExistingTeam", dom.teamCode.value, pageState.existingGlobalTeacher.teacher.uid, pageState.existingTeam.tid];
        ws.send(JSON.stringify(data));
    } else {
        $.ajax({
            url: window.location.href,
            method: "POST",
            data: {"action": "createteam", "cid": cid, "tname": $("#teamCode").val()},
            success: function (result) {
                if (result == null || result["status"] === "error")
                    addErrorBox(dom.errorBoxERROR, result["error"]);
                if (result["status"] === "success") {
                    if (pageState.isCreator) {   // If they are the creator, add in the new team
                        let newTeam: Team = new Team({
                            tname: result["tname"], school: "", tid: result["tid"],
                            students: {nonAlts: [], alt: null}, frq: 0
                        });
                        newTeam.code = result["code"];
                        dom.teamList.appendChild(newTeam.dom.tr);

                        Team.toggleTeam(newTeam);
                        hideSignup();
                    } else location.reload();
                }
            }
        });
    }
}

// Add a team that a teacher has created
function showAddExistingTeam() {
    if(!pageState.isCreator) return;
    if(!pageState.globalTeamsLoaded) ws.send("[\"fetchGlobalTeams\"]");

    deleteErrorSuccessBox(dom.errorBoxERROR);

    pageState.addingExistingTeam = true;
    pageState.existingTeam = null;
    pageState.existingGlobalTeacher = null;
    dom.selectGlobalTeam.style.display = "block";
}

// Close the add existing team dialogue
function closeAddExistingTeam() {
    pageState.addingExistingTeam = false;
    pageState.existingTeam = null;
    pageState.existingGlobalTeacher = null;
    dom.selectGlobalTeam.style.display = "none";
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
function deleteErrorSuccessBox(box) {   // Deletes an error or success box
    let errorBox = document.getElementById(box.id + "ERROR");
    if(errorBox) box.removeChild(errorBox);
}
function addErrorBox(box, error){
    let errorBox = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='error' id='" + box.id + "ERROR'>" + error + "</div>");
    }
    else {
        errorBox.classList.remove("success");
        errorBox.classList.add("error");
        errorBox.innerHTML = "" + error;
    }
}
function addSuccessBox(box, success){
    let errorBox = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.classList.remove("error");
        errorBox.classList.add("success");
        errorBox.innerHTML = "" + success;
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


function codeEntered(code) {
    if(code.value.length == 6) {   // If the code is fully entered
        // First, put a "verifying" box
        addSuccessBox(dom.errorBoxERROR, "Joining...");

        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(){
            if (xhr.readyState === 4) {
                if (xhr.status == 200) { // If an error occurred
                    var response = JSON.parse(xhr.responseText);
                    // @ts-ignore
                    if(Object.keys(response).includes("reload")) {
                        window.location.reload();
                    } else {
                        addErrorBox(dom.errorBoxERROR, response["error"]);
                    }
                } else {    // A server error occurred. Show an error message
                    addErrorBox(dom.errorBoxERROR, "Whoops! A server error occurred. Contact an admin if the problem continues.");
                }
            }
        };
        xhr.open('POST', '/console/competitions', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send('cid='+cid+'&action=jointeam&code=' + code.value + '&isAlternate=' + dom.signUpIsAlternate.checked);
    }
}

function changeMCJudgement(element: HTMLSelectElement, uid: number, probNum: number) {
    let newJudgement = element.value;   // "Correct" or "Incorrect"

    let xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid="+cid+"&action=changeMCJudgement&uid="+uid+"&judgement="+newJudgement+"&probNum="+probNum);
    // document.getElementById("showFRQSubmission"+submissionId).innerText = result_cnt_changeJudgement.options[result_cnt_changeJudgement.selectedIndex].text;
}

/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
let submissionMap : object = {};
let showingFRQSubmission:HTMLElement = null;
let showingFRQSubmissionTR:HTMLTableRowElement = null;  // The table row element they clicked on to show this frq submission
function showFRQSubmission(row:HTMLTableRowElement, submissionId: number) {
    function add(element:HTMLElement) {
        if(!showingFRQSubmission) {
            dom.frq.appendChild(element);
        } else {
            try {
                showingFRQSubmission.replaceWith(element);
            } catch(e) {
                dom.frq.appendChild(element);
            }
        }
        showingFRQSubmission = element;

        if(showingFRQSubmissionTR) showingFRQSubmissionTR.classList.remove("selected");
        showingFRQSubmissionTR = row;
        showingFRQSubmissionTR.classList.add("selected");
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

                let standardResult:boolean = false; // If the result is just "Correct" or "Incorrect"
                for(let i=0,j = config.RESULTS.length; i<j;i++) {
                    let text = config.RESULTS[i];
                    let option = document.createElement("option");
                    option.value = ""+i;
                    option.innerText = text;
                    if(text == result) {
                        option.selected = true;
                        standardResult = true;
                    }

                    result_cnt_changeJudgement.appendChild(option);
                }
                /*if(!standardResult) {
                    let option = document.createElement("option");
                    option.value = "3";
                    option.innerText = result;
                    option.selected = true;
                    option.disabled = true;
                    result_cnt_changeJudgement.appendChild(option);
                }*/
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
function showMCSubmission(uid: number) {
    function add(element:HTMLElement) {
        if(!showingMCSubmission) {
            dom.mc.appendChild(element);
        } else {
            showingMCSubmission.replaceWith(element);
        }
        showingMCSubmission = element;
    }

    if(mcSubmissionMap[uid] != null) {
        add(mcSubmissionMap[uid]);
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
                submissionMap[uid] = div;
            }
        }
    };
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid="+cid+"&action=showMCSubmission&uid="+uid);
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

function inputMaxLength(element: {value: string, maxLength: number}) {
    if(element.value.length > element.maxLength) {
        element.value = element.value.slice(0, element.maxLength);
    }
}

// Send the student name search to the socket
function searchForStudent(input: HTMLInputElement) {
    let data:string[] = ["ssearch", input.value];
    ws.send(JSON.stringify(data));
}

/* Makes a download url for the scoreboard */
let url:string = null;
function makeDownloadURL(csv:string):void {
    if (url != null) window.URL.revokeObjectURL(url);
    let data: Blob = new Blob([csv], {type: 'text/plain'});   /* Makes a blob out of the editor's value. This will be encoded into the url. */

    dom.downloadScoreboard.href = window.URL.createObjectURL(data);
    dom.downloadScoreboard.download = "scoreboard.csv";
}


// Downloads the scoreboard
/*function downloadScoreboard() {
    function processRow(row) {
        let finalVal = '';
        for (let j = 0; j < row.length; j++) {
            let innerValue = row[j] === null ? '' : row[j].toString();
            if (row[j] instanceof Date) {
                innerValue = row[j].toLocaleString();
            }
            let result = innerValue.replace(/"/g, '""');
            if (result.search(/("|,|\n)/g) >= 0)
                result = '"' + result + '"';
            if (j > 0)
                finalVal += ',';
            finalVal += result;
        }
        return finalVal + '\n';
    }

    let csvFile = '';
    for (var i = 0; i < rows.length; i++) {
        csvFile += processRow(rows[i]);
    }

    let blob = new Blob([csvFile], { type: 'text/csv;charset=utf-8;' });
    if (navigator.msSaveBlob) { // IE 10+
        navigator.msSaveBlob(blob, "scoreboard");
    } else {
        if (dom.downloadScoreboard.download !== undefined) { // feature detection
            // Browsers that support HTML5 download attribute
            let url = URL.createObjectURL(blob);
            dom.downloadScoreboard.setAttribute("href", url);
            dom.downloadScoreboard.setAttribute("download", filename);
            dom.downloadScoreboard.style.visibility = 'hidden';
            dom.downloadScoreboard.click();
        }
    }
}*/

// Sends the temporary student data to the server
function createTempStudent() {
    addSuccessBox(dom.openTeamFeedbackCnt, "Saving team...");
    let data = ["createTempStudent", dom.fnameTemp.value, dom.lnameTemp.value, dom.inputSchool.value, (<Team>pageState.openTeam).tid, pageState.addingAlt];
    ws.send(JSON.stringify(data));
    Team.closeSelectStudent();
}