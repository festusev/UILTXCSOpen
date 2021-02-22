///<reference path="../websocket.ts"/>
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var Global = WebAssembly.Global;
var pageState = {
    isCreator: false,
    mcExists: false,
    frqExists: false,
    alternateExists: false,
    numNonAlts: 1,
    openTeam: null,
    editingTeam: false,
    addingAlt: false,
    saveTeamList: [],
    globalTeams: [],
    globalTeamsLoaded: false,
    addingExistingTeam: false,
    existingTeam: null,
    existingGlobalTeacher: null,
    isDeletingTeam: false,
    deletingObject: null // The object we are deleting
};
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
        clarificationColumn: "clarificationsColumn",
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
        clarificationNav: "clarificationNav",
        signUpBox: "signUpBox",
        teamCode: "teamCode",
        toggleCreateTeam: "toggleCreateTeam",
        clarification_input: "clarification_input",
        bottomRank: "bottomRank",
        bottomOutOf: "bottomOutOf",
        // Signup info
        signUpIsAlternateCnt: "signUpIsAlternateCnt",
        signUpIsAlternate: "signUpIsAlternate",
        // Scoreboard IDs
        teamListCnt: "teamListCnt",
        teamCnt: "teamCnt",
        teamList: "teamList",
        openTeamName: "openTeamName",
        openTeamWritten: "openTeamWritten",
        openTeamHandsOn: "openTeamHandsOn",
        openPrimariesList: "openPrimariesList",
        openAlternateList: "openAlternateList",
        editSaveTeam: "editSaveTeam",
        addPrimaryCompetitor: "addPrimaryCompetitor",
        addAlternateCompetitor: "addAlternateCompetitor",
        openTeamFeedbackCnt: "openTeamFeedbackCnt",
        openTeamCode: "openTeamCode",
        deleteMessage: "deleteMessage",
        deleteConfirmationCnt: "deleteConfirmationCnt",
        deleteSubtitle: "deleteSubtitle",
        deleteTeam: "deleteTeam",
        // Select global team IDs
        selectGlobalTeam: "selectGlobalTeam",
        selectGlobalTeamList: "selectGlobalTeamList",
        // Scoreboard select student IDs
        selectStudent: "selectStudent",
        selectStudentList: "selectStudentList",
    },
    CLASSES: {
        columns: "column",
        secondNavItem: 'secondNavItem'
    },
    RESULTS: ["Correct", "Incorrect", "Server Error", "Compile time Error", "Runtime Error", "Empty File", "Time Limit Exceeded", "Unclear File Type"],
    SOCKET_FUNCTIONS: {
        "addSmallMC": function (response) {
            var html = response["html"];
            var template = document.createElement('template');
            template.innerHTML = html;
            dom.mcSubmissions.insertBefore(template.content.firstChild, dom.mcSubmissionsTr.nextSibling);
        }, "addSmallFRQ": function (response) {
            var html = response["html"];
            var template = document.createElement('template');
            template.innerHTML = html;
            var row = dom.frqSubmissionsTable.insertRow(1);
            row.replaceWith(template.content.firstChild);
        }, "updateTeam": function (response) {
            dom.teamMembers.innerHTML = response["html"];
        }, "competitionDeleted": function (response) {
            window.location.href = "http://" + window.location.host + "/console/competitions";
        }, "updateFRQProblems": function (response) {
            var template = document.createElement('template');
            template.innerHTML = response["html"];
            dom.frqProblems.replaceWith(template.content.firstChild);
        }, "updateScoreboard": function (response) {
            var template = document.createElement('template');
            template.innerHTML = response["html"];
            var display = dom.scoreboard.style.display;
            dom.scoreboard.replaceWith(template.content.firstChild);
            dom.cached[config.IDs.scoreboard] = null;
            if (response["rank"]) {
                dom.bottomRank.innerText = response["rank"];
                dom.bottomOutOf.innerText = response["numTeams"];
            }
            showColumn();
        }, "reScoreMC": function (response) {
            // TODO: Write this
        }, "nc": function (response) {
            var clarification_list = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }
            clarification_list.innerHTML = "<div class='clarification'><h3>Question - " + response["name"] + "</h3><span>" + response["question"] + "</span><h3>Answer</h3><span>" +
                "<textarea placeholder='Send a response.'></textarea><button class='chngButton' onclick='answerClarification(this, " +
                response["id"] + ")'>Send</button></span></div>" + clarification_list.innerHTML;
        }, "ac": function (response) {
            var clarification_list = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }
            clarification_list.innerHTML = "<div class='clarification'><h3>Question</h3><span>" + response["question"] + "</span><h3>Answer</h3><span>" +
                response["answer"] + "</span></div>" + clarification_list.innerHTML;
        }, "loadScoreboard": function (response) {
            console.log(response);
            teams.length = 0;
            pageState.isCreator = response.isCreator;
            pageState.mcExists = response.mcExists;
            pageState.frqExists = response.frqExists;
            pageState.alternateExists = response.alternateExists;
            pageState.numNonAlts = response.numNonAlts;
            // If they are the creator, display the "#editSaveTeam" image
            if (pageState.isCreator) {
                document.body.classList.add("isCreator");
            }
            var fragment = document.createDocumentFragment(); // A collection of table row elements
            dom.teamList.innerHTML = "";
            // First, add in the table headers
            var headers = "<th>Name</th>";
            if (pageState.mcExists)
                headers += "<th class='right'>Written</th>";
            if (pageState.frqExists)
                headers += "<th class='right'>Hands-On</th>";
            headers += "<th class='right'>Total</th>";
            var headerDOM = document.createElement("tr");
            headerDOM.innerHTML = headers;
            fragment.appendChild(headerDOM);
            var selectStudentFragment = document.createDocumentFragment(); // The list of students that goes into the select student window
            var _loop_1 = function (i, j) {
                var teamData = response.teams[i];
                var team = new Team(teamData);
                if (pageState.isCreator)
                    team.code = response.teamCodes[i];
                fragment.appendChild(team.dom.tr);
                var _loop_2 = function (student) {
                    var stuTeam = { team: team, isAlt: false }; //  This has to be an object so that the selectStudent method can modify it
                    var tr = document.createElement("tr");
                    tr.classList.add("_" + student[1]);
                    tr.onclick = function () {
                        Team.selectStudent(student, stuTeam);
                    };
                    tr.innerHTML = "<td>" + student[0] + "</td>";
                    selectStudentFragment.appendChild(tr);
                };
                for (var _i = 0, _a = teamData.students.nonAlts; _i < _a.length; _i++) {
                    var student = _a[_i];
                    _loop_2(student);
                }
                if (pageState.alternateExists && teamData.students.alt) {
                    var stuTeam_1 = { team: team, isAlt: true }; //  This has to be an object so that the selectStudent method can modify it
                    var tr = document.createElement("tr");
                    tr.classList.add("_" + teamData.students.alt[1]);
                    tr.onclick = function () {
                        Team.selectStudent(teamData.students.alt, stuTeam_1);
                    };
                    tr.innerHTML = "<td>" + teamData.students.alt[0] + "</td>";
                    selectStudentFragment.appendChild(tr);
                }
            };
            for (var i = 0, j = response.teams.length; i < j; i++) {
                _loop_1(i, j);
            }
            dom.teamList.innerHTML = "";
            dom.selectStudentList.innerHTML = "";
            dom.teamList.appendChild(fragment);
            dom.selectStudentList.appendChild(selectStudentFragment);
        }, "scoreboardOpenTeamFeedback": function (response) {
            if (response.isError)
                addErrorBox(dom.openTeamFeedbackCnt, response.msg);
            else
                addSuccessBox(dom.openTeamFeedbackCnt, response.msg);
        }, "loadGlobalTeams": function (response) {
            if (pageState.globalTeamsLoaded)
                return;
            console.log(response);
            pageState.globalTeamsLoaded = true;
            var fragment = document.createDocumentFragment();
            for (var _i = 0, _a = response.teachers; _i < _a.length; _i++) {
                var data = _a[_i];
                var globalTeacher = new GlobalTeacher(data);
                fragment.appendChild(globalTeacher.getDOM());
            }
            dom.selectGlobalTeamList.appendChild(fragment);
        }, "addExistingTeam": function (response) {
            if (!pageState.isCreator)
                return;
            if (response.error) {
                addSignupErrorBox(response.error);
                return;
            }
            else if (response.reload) {
                requestLoadScoreboard();
                hideSignup();
                closeAddExistingTeam();
                return;
            }
            else {
                var team = new Team(response);
                if (pageState.isCreator)
                    team.code = response.code;
                dom.teamList.appendChild(team.dom.tr);
                var selectStudentFragment = document.createDocumentFragment();
                var _loop_3 = function (student) {
                    var stuTeam = { team: team, isAlt: false }; //  This has to be an object so that the selectStudent method can modify it
                    var tr = document.createElement("tr");
                    tr.onclick = function () {
                        Team.selectStudent(student, stuTeam);
                    };
                    tr.innerHTML = "<td>" + student[0] + "</td>";
                    selectStudentFragment.appendChild(tr);
                };
                for (var _i = 0, _a = response.students.nonAlts; _i < _a.length; _i++) {
                    var student = _a[_i];
                    _loop_3(student);
                }
                if (pageState.alternateExists && response.students.alt) {
                    var stuTeam_2 = { team: team, isAlt: true }; //  This has to be an object so that the selectStudent method can modify it
                    var tr = document.createElement("tr");
                    tr.onclick = function () {
                        Team.selectStudent(response.students.alt, stuTeam_2);
                    };
                    tr.innerHTML = "<td>" + response.students.alt[0] + "</td>";
                    selectStudentFragment.appendChild(tr);
                }
                dom.selectStudentList.appendChild(selectStudentFragment);
                hideSignup();
                closeAddExistingTeam();
                Team.toggleTeam(team);
            }
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
    get clarificationColumn() { return this.getHelper(config.IDs.clarificationColumn); },
    get clarificationNav() { return this.getHelper(config.IDs.clarificationNav); },
    get clarification_input() { return this.getHelper(config.IDs.clarification_input); },
    get bottomRank() { return this.getHelper(config.IDs.bottomRank); },
    get bottomOutOf() { return this.getHelper(config.IDs.bottomOutOf); },
    get teamListCnt() { return this.getHelper(config.IDs.teamListCnt); },
    get teamCnt() { return this.getHelper(config.IDs.teamCnt); },
    get teamList() { return this.getHelper(config.IDs.teamList); },
    get openTeamName() { return this.getHelper(config.IDs.openTeamName); },
    get openTeamWritten() { return this.getHelper(config.IDs.openTeamWritten); },
    get openTeamHandsOn() { return this.getHelper(config.IDs.openTeamHandsOn); },
    get openPrimariesList() { return this.getHelper(config.IDs.openPrimariesList); },
    get openAlternateList() { return this.getHelper(config.IDs.openAlternateList); },
    get editSaveTeam() { return this.getHelper(config.IDs.editSaveTeam); },
    get addPrimaryCompetitor() { return this.getHelper(config.IDs.addPrimaryCompetitor); },
    get addAlternateCompetitor() { return this.getHelper(config.IDs.addAlternateCompetitor); },
    get selectStudent() { return this.getHelper(config.IDs.selectStudent); },
    get selectStudentList() { return this.getHelper(config.IDs.selectStudentList); },
    get openTeamFeedbackCnt() { return this.getHelper(config.IDs.openTeamFeedbackCnt); },
    get openTeamCode() { return this.getHelper(config.IDs.openTeamCode); },
    get selectGlobalTeam() { return this.getHelper(config.IDs.selectGlobalTeam); },
    get selectGlobalTeamList() { return this.getHelper(config.IDs.selectGlobalTeamList); },
    get deleteMessage() { return this.getHelper(config.IDs.deleteMessage); },
    get deleteConfirmationCnt() { return this.getHelper(config.IDs.deleteConfirmationCnt); },
    get deleteSubtitle() { return this.getHelper(config.IDs.deleteSubtitle); },
    get deleteTeam() { return this.getHelper(config.IDs.deleteTeam); },
    get signUpIsAlternateCnt() { return this.getHelper(config.IDs.signUpIsAlternateCnt); },
    get signUpIsAlternate() { return this.getHelper(config.IDs.signUpIsAlternate); },
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
var globalTeachers = [];
// A global teacher that has created teams from their class. Students in these teams may not be signed up for this competition
var GlobalTeacher = /** @class */ (function () {
    function GlobalTeacher(data) {
        this.teacher = {
            uid: -1,
            uname: "",
            school: "",
        };
        this.teams = [];
        this.teacher.uid = data.uid;
        this.teacher.uname = data.uname;
        this.teacher.school = data.school;
        for (var _i = 0, _a = data.teams; _i < _a.length; _i++) {
            var team = _a[_i];
            this.teams.push(team);
        }
        globalTeachers.push(this);
    }
    // Gets the dom that is put into the global team list
    GlobalTeacher.prototype.getDOM = function () {
        var li = document.createElement("li");
        li.classList.add("teacher");
        var teacherName = document.createElement("h2");
        teacherName.innerText = this.teacher.uname;
        li.appendChild(teacherName);
        var school = document.createElement("p");
        school.innerText = this.teacher.school;
        li.appendChild(school);
        var teamsHeader = document.createElement("b");
        teamsHeader.innerText = "Teams:";
        li.appendChild(teamsHeader);
        var teamList = document.createElement("ul");
        li.appendChild(teamList);
        var thisGlobalTeacher = this;
        var _loop_4 = function (team) {
            var teamLI = document.createElement("li");
            teamLI.classList.add("team");
            teamLI.onclick = function () {
                // closeAddExistingTeam();
                dom.teamCode.value = team.tname;
                pageState.existingTeam = team;
                pageState.existingGlobalTeacher = thisGlobalTeacher;
                showSignup();
            };
            var teamHeader = document.createElement("h3");
            teamHeader.innerText = team.tname;
            teamLI.appendChild(teamHeader);
            var studentUL = document.createElement("ul");
            var primariesHeader = document.createElement("b");
            primariesHeader.innerText = "Primaries";
            studentUL.appendChild(primariesHeader);
            for (var _i = 0, _a = team.nonAlts; _i < _a.length; _i++) {
                var student = _a[_i];
                var studentLI = document.createElement("li");
                studentLI.classList.add("student");
                studentLI.innerText = student[0];
                studentUL.appendChild(studentLI);
            }
            var altHeader = document.createElement("b");
            altHeader.innerText = "Alternates";
            studentUL.appendChild(altHeader);
            if (team.alt) {
                var studentLI = document.createElement("li");
                studentLI.classList.add("student");
                studentLI.innerText = team.alt[0];
                studentUL.appendChild(studentLI);
            }
            teamLI.appendChild(studentUL);
            teamList.appendChild(teamLI);
        };
        for (var _i = 0, _a = this.teams; _i < _a.length; _i++) {
            var team = _a[_i];
            _loop_4(team);
        }
        return li;
    };
    return GlobalTeacher;
}());
var teams = [];
var Team = /** @class */ (function () {
    function Team(data) {
        this.dom = { tr: null, mcTD: null };
        this.tname = data.tname;
        this.school = data.school;
        this.tid = data.tid;
        this.students = data.students.nonAlts;
        this.alt = data.students.alt;
        if (pageState.frqExists)
            this.frqScore = data.frq;
        else
            this.frqScore = 0;
        this.mcScore = 0;
        if (pageState.mcExists) {
            for (var _i = 0, _a = this.students; _i < _a.length; _i++) {
                var student = _a[_i];
                if (student[2]) // If this student has competed
                    this.mcScore += student[2];
            }
        }
        teams.push(this);
        this.render();
    }
    // Creates the html objects
    Team.prototype.render = function () {
        this.dom.tr = document.createElement("tr");
        this.dom.tr.classList.add("team");
        var tnameTD = document.createElement("td");
        tnameTD.innerText = this.tname;
        this.dom.tr.appendChild(tnameTD);
        if (pageState.mcExists) {
            var mcTD = document.createElement("td");
            mcTD.classList.add("right");
            mcTD.innerText = "" + this.mcScore;
            this.dom.mcTD = mcTD;
            this.dom.tr.appendChild(mcTD);
        }
        if (pageState.frqExists) {
            var frqTD = document.createElement("td");
            frqTD.classList.add("right");
            frqTD.innerText = "" + this.frqScore;
            this.dom.tr.appendChild(frqTD);
        }
        var totalTD = document.createElement("td");
        totalTD.classList.add("right");
        totalTD.innerText = "" + (this.mcScore + this.frqScore);
        this.dom.tr.appendChild(totalTD);
        var thisTeam = this;
        this.dom.tr.onclick = function () { Team.toggleTeam(thisTeam); };
    };
    // Save the team and copy over the temporary information to the official information
    Team.prototype.save = function () {
        function getTeamData(team) {
            var nonAltUIDs = [];
            for (var _i = 0, _a = team.students; _i < _a.length; _i++) {
                var student = _a[_i];
                nonAltUIDs.push(student[1]);
            }
            var alt;
            if (team.alt)
                alt = team.alt[1];
            else
                alt = -1;
            return { tid: team.tid, nonAlts: nonAltUIDs, alt: alt };
        }
        console.log("save"); // Make sure that when this is implemented the  server checks that there are no duplicate students
        var data = ["saveTeam", []];
        data[1].push(getTeamData(this));
        for (var _i = 0, _a = pageState.saveTeamList; _i < _a.length; _i++) {
            var team = _a[_i];
            data[1].push(getTeamData(team));
        }
        pageState.saveTeamList.length = 0;
        ws.send(JSON.stringify(data));
        addSuccessBox(dom.openTeamFeedbackCnt, "Saving team...");
    };
    Team.prototype.delete = function () {
        for (var i = 0; i < teams.length; i++) {
            if (teams[i].tid == this.tid)
                teams.splice(i, 1);
        }
        for (var _i = 0, _a = this.students; _i < _a.length; _i++) { // Remove this from the selectStudents list
            var student = _a[_i];
            var studentDOMs = dom.selectStudentList.querySelector("._" + student[1]);
            for (var _b = 0, studentDOMs_1 = studentDOMs; _b < studentDOMs_1.length; _b++) {
                var studentDOM = studentDOMs_1[_b];
                dom.selectStudentList.removeChild(studentDOM);
            }
        }
        dom.teamList.removeChild(this.dom.tr);
        dom.teamCnt.style.display = "none";
        pageState.openTeam.dom.tr.classList.remove("selected");
        pageState.openTeam = null;
        ws.send("[\"deleteTeam\"," + this.tid + "]");
    };
    Team.prototype.deleteStudent = function (student) {
        console.log("Deleting student");
        var studentDOM = dom.selectStudentList.getElementsByClassName("_" + student[1])[0]; // Remove this from the selectStudents list
        if (studentDOM != null) {
            dom.selectStudentList.removeChild(studentDOM);
        }
        if (pageState.mcExists && student[2]) {
            this.mcScore -= student[2];
            this.dom.mcTD.innerText = "" + this.mcScore;
        }
        for (var i = 0; i < this.students.length; i++) {
            if (this.students[i][1] == student[1])
                this.students.splice(i, 1);
        }
        if (pageState.alternateExists && this.alt != null && this.alt[1] == student[1])
            this.alt = null;
        Team.renderOpenTeam(this);
        Team.editSaveTeam();
        ws.send("[\"deleteStudent\"," + this.tid + "," + student[1] + "]");
    };
    Team.showDeleteConfirmation = function () {
        pageState.isDeletingTeam = true;
        pageState.deletingObject = pageState.openTeam;
        dom.deleteMessage.innerText = "Are you sure?";
        dom.deleteSubtitle.innerText = "Deleting this team, '" + pageState.openTeam.tname + "', will also remove all of its students from the competition. This action cannot be undone.";
        dom.deleteConfirmationCnt.style.display = "block";
    };
    // Opens the menu for adding a student as a primary competitor
    Team.addPrimaryCompetitor = function () {
        if (!pageState.isCreator)
            return;
        pageState.addingAlt = false;
        dom.selectStudent.style.display = "block";
        console.log("Add primary competitor");
    };
    Team.addAlternateCompetitor = function () {
        if (!pageState.isCreator)
            return;
        pageState.addingAlt = true;
        dom.selectStudent.style.display = "block";
        console.log("Add alternate competitor");
    };
    Team.closeSelectStudent = function () {
        dom.selectStudent.style.display = "none";
    };
    // teamData is an object so that we can edit it so it will be passed to all future calls of this function.
    Team.selectStudent = function (student, teamData) {
        if (!pageState.isCreator)
            return;
        var openTeam = pageState.openTeam;
        var selectedSuccessfully = (pageState.alternateExists && pageState.addingAlt && !openTeam.alt) ||
            (!pageState.addingAlt && openTeam.students.length < pageState.numNonAlts); // If we are changing which team this student is on
        if (selectedSuccessfully) { // This must come first, so that if the student is already in this team, this works
            if (teamData.isAlt)
                teamData.team.alt = null;
            else {
                var newStudentsList = [];
                for (var _i = 0, _a = teamData.team.students; _i < _a.length; _i++) {
                    var s = _a[_i];
                    if (s[1] != student[1])
                        newStudentsList.push(s);
                }
                teamData.team.students = newStudentsList;
            }
            pageState.saveTeamList.push(teamData.team);
            teamData.isAlt = pageState.addingAlt;
            teamData.team = openTeam;
        }
        if (pageState.alternateExists && pageState.addingAlt && !openTeam.alt) {
            openTeam.alt = student;
        }
        else if (!pageState.addingAlt && openTeam.students.length < pageState.numNonAlts) {
            var alreadyInTeam = false;
            for (var _b = 0, _c = openTeam.students; _b < _c.length; _b++) {
                var s = _c[_b];
                if (student[1] == s[1]) {
                    alreadyInTeam = true;
                    break;
                }
            }
            if (!alreadyInTeam) {
                pageState.openTeam.students.push(student);
            }
        }
        if (selectedSuccessfully) {
            Team.renderOpenTeam(openTeam);
            Team.editSaveTeam();
            Team.closeSelectStudent();
        }
    };
    Team.renderOpenTeam = function (team) {
        // gets the table row for a student. [0] = uname, [1] = uid, [2] = mc score
        function getStudentTR(student) {
            var tr = document.createElement("tr");
            tr.classList.add("team");
            var unameTD = document.createElement("td");
            unameTD.innerText = student[0];
            tr.appendChild(unameTD);
            if (pageState.mcExists) {
                var mcTD = document.createElement("td");
                mcTD.classList.add("right");
                if (typeof student[2] != "undefined")
                    mcTD.innerText = student[2] + "pts";
                else
                    mcTD.innerText = "Not taken";
                tr.appendChild(mcTD);
            }
            var deleteTD = document.createElement("td");
            deleteTD.classList.add("editTeam");
            deleteTD.innerHTML = "<img src='/res/console/delete.svg' class='deleteStudent'/>";
            deleteTD.onclick = function () {
                pageState.isDeletingTeam = false;
                pageState.deletingObject = student;
                dom.deleteMessage.innerText = "Are you sure?";
                dom.deleteSubtitle.innerText = "Deleting this student, '" + student[0] + "', will also remove them from the competition. This action cannot be undone.";
                dom.deleteConfirmationCnt.style.display = "block";
            };
            tr.appendChild(deleteTD);
            return tr;
        }
        dom.editSaveTeam.src = "/res/console/edit.svg";
        dom.teamCnt.classList.remove("editing");
        dom.addPrimaryCompetitor.style.display = "none";
        if (pageState.alternateExists)
            dom.addAlternateCompetitor.style.display = "none";
        dom.teamCnt.style.display = "block";
        dom.openTeamName.innerText = team.tname;
        if (pageState.isCreator)
            dom.openTeamCode.innerText = team.code;
        if (pageState.mcExists)
            dom.openTeamWritten.innerText = team.mcScore + " pts";
        if (pageState.frqExists)
            dom.openTeamHandsOn.innerText = team.frqScore + " pts";
        // Add in the primaries
        var fragment = document.createDocumentFragment();
        for (var _i = 0, _a = team.students; _i < _a.length; _i++) {
            var student = _a[_i];
            fragment.appendChild(getStudentTR(student));
        }
        dom.openPrimariesList.innerHTML = "";
        dom.openPrimariesList.appendChild(fragment);
        // Add in the alternate
        if (pageState.alternateExists) {
            dom.openAlternateList.innerHTML = "";
            if (team.alt)
                dom.openAlternateList.appendChild(getStudentTR(team.alt));
        }
        // dom.openAlternateList.appendChild(getStudentTR(team.))
        pageState.openTeam = team;
        pageState.editingTeam = false;
    };
    /*
     Toggles between looking at a team and not. If the given team object is null or equal to the currently open team,
     then close the 'teamCnt'. Otherwise, open it and set its information properly.
     */
    Team.toggleTeam = function (team) {
        if (!team || team == pageState.openTeam) { // Close the "#teamCnt"
            dom.teamCnt.style.display = "none";
            pageState.openTeam.dom.tr.classList.remove("selected");
            pageState.openTeam = null;
            Team.editSaveTeam();
        }
        else {
            var errorBox_1 = document.getElementById(dom.openTeamFeedbackCnt.id + "ERROR");
            if (errorBox_1)
                dom.openTeamFeedbackCnt.removeChild(errorBox_1);
            if (pageState.openTeam)
                pageState.openTeam.dom.tr.classList.remove("selected");
            team.dom.tr.classList.add("selected");
            Team.renderOpenTeam(team);
        }
    };
    /*
    Toggles between editing and saving the open team.
     */
    Team.editSaveTeam = function () {
        if (!pageState.isCreator)
            return;
        if (pageState.openTeam) {
            if (pageState.editingTeam) { // Save the team but do not leave editing mode
                pageState.openTeam.save();
            }
            else { // Enter editing mode
                pageState.editingTeam = true;
                dom.editSaveTeam.src = "/res/console/save.svg";
                dom.teamCnt.classList.add("editing");
                if (pageState.openTeam.students.length < pageState.numNonAlts)
                    dom.addPrimaryCompetitor.style.display = "block";
                if (pageState.alternateExists && !pageState.openTeam.alt)
                    dom.addAlternateCompetitor.style.display = "block";
            }
        }
    };
    return Team;
}());
var cid = null; // Undefined if we are looking at the UIL list
$(document).ready(function () {
    showColumn();
    // Set the multiple choice answers that are saved
    var cookie = getCookie(cid + "MC");
    if (cookie != null) {
        var savedMC = null;
        try {
            savedMC = JSON.parse(cookie);
        }
        catch (e) {
            return;
        }
        var questionDOMs = document.getElementsByClassName("mcQuestion");
        if (questionDOMs.length <= 0)
            return;
        var aCharCode = "a".charCodeAt(0);
        for (var questionNumberString in savedMC) {
            var questionDOM = questionDOMs.item(parseInt(questionNumberString) - 1);
            var savedMCAnswer = savedMC[questionNumberString];
            if (savedMCAnswer == null)
                continue;
            var tableCell = questionDOM.childNodes.item(savedMCAnswer.charCodeAt(0) - aCharCode + 1);
            if (tableCell == null)
                continue;
            var div = tableCell.firstChild;
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
(function () {
    var sPageURL = window.location.search.substring(1), sURLVariables = sPageURL.split('&'), sParameterName, i;
    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === "cid") {
            cid = sParameterName[1] === undefined ? null : decodeURIComponent(sParameterName[1]);
        }
    }
    getWebSocket(window.location.host + "/console/sockets/c/" + cid, config.SOCKET_FUNCTIONS);
})();
function requestLoadScoreboard() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    if (!(ws == null || ws.readyState === 0)) return [3 /*break*/, 2];
                    return [4 /*yield*/, new Promise(function (r) { return setTimeout(r, 2000); })];
                case 1:
                    _a.sent();
                    return [3 /*break*/, 0];
                case 2:
                    console.log("sending");
                    ws.send("[\"loadScoreboard\"]");
                    return [2 /*return*/];
            }
        });
    });
}
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
function deleteTeamOrStudent() {
    if (pageState.isDeletingTeam) {
        var team = pageState.deletingObject;
        team.delete();
        closeDeleteConfirmation();
    }
    else { // Deleting a student
        var student = pageState.deletingObject;
        var team = pageState.openTeam;
        team.deleteStudent(student);
        closeDeleteConfirmation();
    }
}
function closeDeleteConfirmation() {
    dom.deleteConfirmationCnt.style.display = "none";
}
function showColumn() {
    // Check if there is an anchor, and if there is show that section of the page
    try {
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
    catch (e) {
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
function showClarifications() {
    showHelper(dom.clarificationColumn, "#clarifications", dom.clarificationNav);
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
    document.getElementById("errorBoxERROR");
    dom.signUpBox.style.display = "block";
}
function hideSignup() {
    dom.signUpBox.style.display = "none";
}
// Switches between joining a team and creating a team
var jointeamShowing = true;
function toggleCreateTeam(event) {
    if (jointeamShowing) { // Switch to creating a team
        dom.signUpBox.querySelector("h1").innerText = "Create Team";
        dom.signUpBox.querySelector(".instruction").innerHTML = "Team Name";
        dom.signUpBox.querySelector("input").value = "";
        dom.teamCode.maxLength = "25";
        dom.teamCode.oninput = null;
        dom.teamCode.classList.add("creatingTeam");
        if (pageState.alternateExists || pageState.alternateExists == null) { // If alternates exist or the scoreboard hasn't loaded
            dom.signUpIsAlternateCnt.style.display = "block";
        }
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
        dom.signUpIsAlternateCnt.style.display = "none";
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
    var data;
    if (pageState.addingExistingTeam) {
        var data_1 = ["addExistingTeam", dom.teamCode.value, pageState.existingGlobalTeacher.teacher.uid, pageState.existingTeam.tid];
        ws.send(JSON.stringify(data_1));
    }
    else {
        $.ajax({
            url: window.location.href,
            method: "POST",
            data: { "action": "createteam", "cid": cid, "tname": $("#teamCode").val() },
            success: function (result) {
                if (result == null || result["status"] === "error")
                    addSignupErrorBox(result["error"]);
                if (result["status"] === "success") {
                    if (pageState.isCreator) { // If they are the creator, add in the new team
                        var newTeam = new Team({
                            tname: result["tname"], school: "", tid: result["tid"],
                            students: { nonAlts: [], alt: null }, frq: 0
                        });
                        newTeam.code = result["code"];
                        dom.teamList.appendChild(newTeam.dom.tr);
                        Team.toggleTeam(newTeam);
                        hideSignup();
                    }
                    else
                        location.reload();
                }
            }
        });
    }
}
// Add a team that a teacher has created
function showAddExistingTeam() {
    if (!pageState.isCreator)
        return;
    if (!pageState.globalTeamsLoaded)
        ws.send("[\"fetchGlobalTeams\"]");
    var errorBox = document.getElementById(dom.selectGlobalTeam.id + "ERROR");
    if (errorBox)
        dom.selectGlobalTeam.removeChild(errorBox);
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
function toggle_clarify() {
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
    setCookie(cid + "MC", JSON.stringify(choices), 1);
}
function setSAQChoice(question, dom) {
    choices[question] = dom.value;
}
function submitMC(callback) {
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
                if (callback)
                    callback();
                else {
                    var template = document.createElement('template');
                    template.innerHTML = result["mcHTML"];
                    dom.mc.replaceWith(template.content.firstChild);
                    //  clearInterval(xmcTestTimer);
                    delete dom.cached[config.IDs.mc];
                    dom.mc.style.display = "block";
                    delete dom.cached[config.CLASSES.columns];
                }
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
        errorBox.classList.remove("success");
        errorBox.classList.add("error");
        errorBox.innerHTML = "" + error;
    }
}
function addSuccessBox(box, success) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='success' id='" + box.id + "ERROR'>" + success + "</div>");
    }
    else {
        errorBox.classList.remove("error");
        errorBox.classList.add("success");
        errorBox.innerHTML = "" + success;
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
                var template = document.createElement('template');
                template.innerHTML = result["frqHTML"];
                dom.frq.replaceWith(template.firstChild);
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
        xhr.send('cid=' + cid + '&action=jointeam&code=' + code.value + '&isAlternate=' + dom.signUpIsAlternate.checked);
    }
}
function changeMCJudgement(element, tid, uid, probNum) {
    var newJudgement = element.value; // "Correct" or "Incorrect"
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=changeMCJudgement&uid=" + uid + "&tid=" + tid + "&judgement=" + newJudgement + "&probNum=" + probNum);
    // document.getElementById("showFRQSubmission"+submissionId).innerText = result_cnt_changeJudgement.options[result_cnt_changeJudgement.selectedIndex].text;
}
/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
var submissionMap = {};
var showingFRQSubmission = null;
var showingFRQSubmissionTR = null; // The table row element they clicked on to show this frq submission
function showFRQSubmission(row, submissionId) {
    function add(element) {
        if (!showingFRQSubmission) {
            dom.frq.appendChild(element);
        }
        else {
            try {
                showingFRQSubmission.replaceWith(element);
            }
            catch (e) {
                dom.frq.appendChild(element);
            }
        }
        showingFRQSubmission = element;
        if (showingFRQSubmissionTR)
            showingFRQSubmissionTR.classList.remove("selected");
        showingFRQSubmissionTR = row;
        showingFRQSubmissionTR.classList.add("selected");
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
                var standardResult = false; // If the result is just "Correct" or "Incorrect"
                for (var i = 0, j = config.RESULTS.length; i < j; i++) {
                    var text = config.RESULTS[i];
                    var option = document.createElement("option");
                    option.value = "" + i;
                    option.innerText = text;
                    if (text == result) {
                        option.selected = true;
                        standardResult = true;
                    }
                    result_cnt_changeJudgement_1.appendChild(option);
                }
                /*if(!standardResult) {
                    let option = document.createElement("option");
                    option.value = "3";
                    option.innerText = result;
                    option.selected = true;
                    option.disabled = true;
                    result_cnt_changeJudgement.appendChild(option);
                }*/
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
/***
 * Lets a teacher answer a clarification.
 */
function answerClarification(button, id) {
    var span = button.parentNode; // The <span> that holds the textarea and the button
    var textarea = span.getElementsByTagName("textarea")[0];
    var msg = textarea.value;
    if (msg.trim().length != 0 && msg.length <= 255) {
        var response = ["rc", "" + id, msg];
        ws.send(JSON.stringify(response));
        span.innerHTML = msg;
    }
}
/***
 * Sends a clarification if the text box is not empty.
 */
function sendClarification() {
    var msg = dom.clarification_input.value;
    if (msg.trim().length != 0 && msg.length <= 255) {
        var response = ["nc", msg];
        ws.send(JSON.stringify(response));
    }
    dom.clarification_input.value = "";
}
