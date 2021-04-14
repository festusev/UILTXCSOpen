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
    mcCorrectPoints: 0,
    mcIncorrectPoints: 0,
    frqExists: false,
    frqMaxPoints: 0,
    frqIncorrectPenalty: 0,
    frqProblemMap: [],
    writtenSpecialistExists: false,
    teamSize: 1,
    mcNumScoresToKeep: 0,
    openTeam: null,
    editingTeam: false,
    addingAlt: false,
    // saveTeamList: [],    // The list of teams that also need to be saved when the openTeam is saved
    globalTeams: [],
    globalTeamsLoaded: false,
    addingExistingTeam: false,
    existingTeam: null,
    existingGlobalTeacher: null,
    isDeletingTeam: false,
    deletingObject: null,
    saveTeamCallbacks: [],
    frqSortMethod: null,
    viewingDivision: null,
    showAlternates: false // If the written tab should show alternates
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
        errorBoxERROR: "errorBoxERROR",
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
        editSaveTeam: "editSaveTeam",
        addPrimaryCompetitor: "addPrimaryCompetitor",
        openTeamFeedbackCnt: "openTeamFeedbackCnt",
        openTeamCode: "openTeamCode",
        openTeamIsIndividual: "openTeamIsIndividual",
        deleteMessage: "deleteMessage",
        deleteConfirmationCnt: "deleteConfirmationCnt",
        deleteSubtitle: "deleteSubtitle",
        deleteTeam: "deleteTeam",
        studentSearchTable: "studentSearchTable",
        teamDivision: "teamDivision",
        generalScoreboard: "generalScoreboard",
        writtenScoreboard: "writtenScoreboard",
        writtenScoreboardTable: "writtenScoreboardTable",
        handsOnScoreboard: "handsOnScoreboard",
        handsOnScoreboardTable: "handsOnScoreboardTable",
        // Select global team IDs
        selectGlobalTeam: "selectGlobalTeam",
        selectGlobalTeamList: "selectGlobalTeamList",
        // Scoreboard select student IDs
        studentSearch: "studentSearch",
        selectStudent: "selectStudent",
        selectStudentList: "selectStudentList",
        selectStudentFromClass: "selectStudentFromClass",
        selectSignedUpStudent: "selectSignedUpStudent",
        fnameTemp: "fnameTemp",
        lnameTemp: "lnameTemp",
        inputSchool: "inputSchool",
        // Right info bar IDs, for admins only
        writtenSubmissionCount: "writtenSubmissionCount",
        writtenAverage: "writtenAverage",
        handsOnSubmissionCount: "handsOnSubmissionCount",
        handsOnSubmissionAverage: "handsOnSubmissionAverage",
        numTeams: "numTeams",
        numUsers: "numUsers",
        playBell: "playBell",
        downloadScoreboard: "downloadScoreboard",
        frqSubmissions: "frqSubmissions",
        frqTextfile: "frqTextfile",
        uploadFileProxy: "uploadFileProxy",
        uploadRosterProxy: "uploadRosterProxy",
        uploadRosterBox: "uploadRosterBox",
        resetConfirmationCnt: "resetConfirmationCnt",
        divisionSelector: "divisionSelector",
        deleteTeamsConfirmationCnt: "deleteTeamsConfirmationCnt"
    },
    CLASSES: {
        columns: "column",
        secondNavItem: 'secondNavItem'
    },
    RESULTS: ["Correct", "Incorrect", "Server Error", "Compile-time Error", "Runtime Error", "Empty File",
        "Time Limit Exceeded", "File Type Not Allowed", "Package Error", "Wrong Output Format"],
    SOCKET_FUNCTIONS: {
        "reload": function (response) {
            window.location.reload();
        },
        "addSmallMC": function (response) {
            var html = response["html"];
            var template = document.createElement('template');
            template.innerHTML = html;
            dom.mcSubmissions.firstChild.insertBefore(template.content.firstChild, dom.mcSubmissionsTr.nextSibling);
        }, "addSmallFRQ": function (response) {
            dom.playBell.play();
            new FRQSubmission(response.submission);
            pageState.frqSortMethod();
        }, "updateSmallFRQ": function (response) {
            var submission = frqSubmissionMap[response.submission[5]];
            submission.update(response.submission);
            pageState.frqSortMethod();
            if (showingFRQSubmission.submissionId == submission.submissionId) {
                showFRQSubmission(submission);
            }
        }, "updateTeam": function (response) {
            dom.teamMembers.innerHTML = response["html"];
        }, "competitionDeleted": function (response) {
            window.location.href = "http://" + window.location.host + "/console/competitions";
        }, "updateFRQProblems": function (response) {
            var template = document.createElement('template');
            template.innerHTML = response["html"];
            dom.frqProblems.replaceWith(template.content.firstChild);
            dom.cached.frqProblems = template.content.firstChild;
        }, "reScoreMC": function (response) {
            // TODO: Write this
        }, "nc": function (response) {
            var clarification_list = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }
            var clarification = document.createElement("div");
            clarification.id = "clarification_" + response["index"];
            clarification.classList.add("clarification");
            var clarificationQuestionH3 = document.createElement("h3");
            clarificationQuestionH3.innerText = "Question" + response["name"];
            clarification.appendChild(clarificationQuestionH3);
            var clarificationQuestion = document.createElement("span");
            clarificationQuestion.innerText = response["question"];
            clarification.appendChild(clarificationQuestion);
            var clarificationAnswerH3 = document.createElement("h3");
            clarificationAnswerH3.innerText = "Answer";
            clarification.appendChild(clarificationAnswerH3);
            var clarificationAnswer = document.createElement("span");
            clarificationAnswer.innerHTML = "<textarea placeholder='Send a response.' maxlength='255' " +
                "oninput='inputMaxLength(this,255)'></textarea><button class='chngButton' onclick='answerClarification(this, " +
                response["id"] + ")'>Send</button>";
            clarification.appendChild(clarificationAnswer);
            clarification_list.insertBefore(clarification, clarification_list.firstChild);
            dom.playBell.play();
        }, "judgeClarification": function (response) {
            var clarification_list = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }
            var clarification = document.createElement("div");
            clarification.id = "clarification_" + response["index"];
            clarification.classList.add("clarification");
            var clarificationQuestionH3 = document.createElement("h3");
            clarificationQuestionH3.innerText = "Judge Clarification";
            clarification.appendChild(clarificationQuestionH3);
            var clarificationQuestion = document.createElement("span");
            clarificationQuestion.innerText = response["question"];
            clarification.appendChild(clarificationQuestion);
            clarification_list.insertBefore(clarification, clarification_list.firstChild);
        }, "ac": function (response) {
            var clarification_list = dom.clarificationColumn.querySelector(".clarification_group");
            if (clarification_list.innerHTML == "There are no clarifications.") {
                clarification_list.innerHTML = "";
            }
            if (pageState.isCreator) { // They are a creator, so check if the clarification already has a dom
                var clarificationDOM = document.getElementById("clarification_" + response["index"]);
                if (clarificationDOM != null)
                    clarification_list.removeChild(clarificationDOM);
            }
            var clarification = document.createElement("div");
            clarification.id = "clarification_" + response["index"];
            clarification.classList.add("clarification");
            var clarificationQuestionH3 = document.createElement("h3");
            clarificationQuestionH3.innerText = "Question" + response["name"];
            clarification.appendChild(clarificationQuestionH3);
            var clarificationQuestion = document.createElement("span");
            clarificationQuestion.innerText = response["question"];
            clarification.appendChild(clarificationQuestion);
            var clarificationAnswerH3 = document.createElement("h3");
            clarificationAnswerH3.innerText = "Answer";
            clarification.appendChild(clarificationAnswerH3);
            var clarificationAnswer = document.createElement("span");
            clarificationAnswer.innerText = response["answer"];
            clarification.appendChild(clarificationAnswer);
            clarification_list.insertBefore(clarification, clarification_list.firstChild);
        }, "loadFRQSubmissions": function (response) {
            function resetSort(type) {
                if (type != 0) {
                    problemSortMode = 0;
                    problemImg.src = "/res/unsorted.svg";
                }
                if (type != 1) {
                    teamSortMode = 0;
                    teamImg.src = "/res/unsorted.svg";
                }
                if (type != 2) {
                    gradedSortMode = 0;
                    gradedImg.src = "/res/unsorted.svg";
                }
                if (type != 3) {
                    resultSortMode = 0;
                    resultImg.src = "/res/unsorted.svg";
                }
                if (type != 4) {
                    timestampSortMode = 0;
                    timestampImg.src = "/res/unsorted.svg";
                }
            }
            // returns the new sortMode based on the old sortMode, and sets the image appropriately
            function manageClick(sortMode, img) {
                if (sortMode == 1) { // Switch to ascending
                    img.src = "/res/ascending.svg";
                    return 2;
                }
                else { // Switch to descending
                    img.src = "/res/descending.svg";
                    return 1;
                }
            }
            frqSubmissions = [];
            frqSubmissionMap = {};
            dom.frqSubmissionsTable.innerHTML = "";
            var tr = document.createElement("tr");
            tr.id = "frqSubmissionsTr";
            var problemTH = document.createElement("th");
            problemTH.innerText = "Problem";
            var problemSortMode = 0; // 0 is unsorted, 1 is descending, 2 is ascending
            var problemImg = document.createElement("img");
            problemImg.src = "/res/unsorted.svg";
            problemTH.appendChild(problemImg);
            var problemSortFunction = function () {
                sortFRQSubmissions(function (f1, f2) {
                    return f1.problemName.localeCompare(f2.problemName);
                }, problemSortMode == 1);
            };
            problemTH.onclick = function () {
                resetSort(0);
                problemSortMode = manageClick(problemSortMode, problemImg);
                problemSortFunction();
                pageState.frqSortMethod = problemSortFunction;
            };
            tr.appendChild(problemTH);
            var teamTH = document.createElement("th");
            teamTH.innerText = "Team";
            var teamSortMode = 0; // 0 is unsorted, 1 is descending, 2 is ascending
            var teamImg = document.createElement("img");
            teamImg.src = "/res/unsorted.svg";
            teamTH.appendChild(teamImg);
            var teamSortFunction = function () {
                sortFRQSubmissions(function (f1, f2) {
                    return f1.tname.localeCompare(f2.tname);
                }, teamSortMode == 1);
            };
            teamTH.onclick = function () {
                resetSort(1);
                teamSortMode = manageClick(teamSortMode, teamImg);
                teamSortFunction();
                pageState.frqSortMethod = teamSortFunction;
            };
            tr.appendChild(teamTH);
            var gradedTH = document.createElement("th");
            gradedTH.innerText = "Graded";
            var gradedSortMode = 0; // 0 is unsorted, 1 is descending, 2 is ascending
            var gradedImg = document.createElement("img");
            gradedImg.src = "/res/unsorted.svg";
            gradedTH.appendChild(gradedImg);
            var gradedSortFunction = function () {
                sortFRQSubmissions(function (f1, f2) {
                    if (f1.graded && !f2.graded)
                        return 1;
                    return -1;
                }, gradedSortMode == 1);
            };
            gradedTH.onclick = function () {
                resetSort(2);
                gradedSortMode = manageClick(gradedSortMode, gradedImg);
                gradedSortFunction();
                pageState.frqSortMethod = gradedSortFunction;
            };
            tr.appendChild(gradedTH);
            var resultTH = document.createElement("th");
            resultTH.innerText = "Result";
            var resultSortMode = 0; // 0 is unsorted, 1 is descending, 2 is ascending
            var resultImg = document.createElement("img");
            resultImg.src = "/res/unsorted.svg";
            resultTH.appendChild(resultImg);
            var resultSortFunction = function () {
                sortFRQSubmissions(function (f1, f2) {
                    return f1.result.localeCompare(f2.result);
                }, resultSortMode == 1);
            };
            resultTH.onclick = function () {
                resetSort(3);
                resultSortMode = manageClick(resultSortMode, resultImg);
                resultSortFunction();
                pageState.frqSortMethod = resultSortFunction;
            };
            tr.appendChild(resultTH);
            var timestampTH = document.createElement("th");
            timestampTH.innerText = "Timestamp";
            var timestampSortMode = 1; // 0 is unsorted, 1 is descending, 2 is ascending
            var timestampImg = document.createElement("img");
            timestampImg.src = "/res/descending.svg";
            timestampTH.appendChild(timestampImg);
            var timestampSortFunction = function () {
                sortFRQSubmissions(function (f1, f2) {
                    return f2.timestamp - f1.timestamp;
                }, timestampSortMode == 1);
            };
            pageState.frqSortMethod = timestampSortMode;
            timestampTH.onclick = function () {
                resetSort(4);
                timestampSortMode = manageClick(timestampSortMode, timestampImg);
                timestampSortFunction();
                pageState.frqSortMethod = timestampSortFunction;
            };
            tr.appendChild(timestampTH);
            dom.frqSubmissionsTable.appendChild(tr);
            for (var _i = 0, _a = response.frqSubmissions; _i < _a.length; _i++) {
                var frqSubmission = _a[_i];
                new FRQSubmission(frqSubmission);
            }
        }, "loadScoreboard": function (response) {
            var newToggleTeam = null; // The new team object that we are toggling open
            var oldOpenTeamTID = -1; // The tid of the old open team
            if (pageState.openTeam)
                oldOpenTeamTID = pageState.openTeam.tid; // The old team that was open. May be null
            teams = {};
            divisions = {};
            pageState.isCreator = response.isCreator;
            pageState.mcExists = response.mcExists;
            pageState.mcCorrectPoints = response.mcCorrectPoints;
            pageState.mcIncorrectPoints = response.mcIncorrectPoints;
            pageState.frqExists = response.frqExists;
            pageState.frqIncorrectPenalty = response.frqIncorrectPenalty;
            pageState.frqMaxPoints = response.frqMaxPoints;
            pageState.frqProblemMap = response.frqProblemMap;
            pageState.writtenSpecialistExists = response.alternateExists;
            pageState.teamSize = response.teamSize;
            pageState.mcNumScoresToKeep = response.mcNumScoresToKeep;
            // If they are the creator, display the "#editSaveTeam" image
            if (pageState.isCreator) {
                document.body.classList.add("isCreator");
            }
            // These datapoints are displayed on the right bar if the user is an admin
            var numWrittenSubmitted = 0;
            var writtenSum = 0; // Sum of written scores
            var handsOnSum = 0; // Sum of hands on scores
            var numStudents = 0;
            var bottomRank = 0; // If they are a signed-up student, response.tid is specified and we calculate their bottom rank
            var bottomOutOf = response.teams.length;
            dom.divisionSelector.innerHTML = "";
            dom.divisionSelector.onchange = function () {
                selectDivision(dom.divisionSelector.value);
            };
            var divisionsSorted = [];
            var selectStudentFragment = document.createDocumentFragment(); // The list of students that goes into the select student window
            for (var i = 0, j = response.teams.length; i < j; i++) {
                var teamData = response.teams[i];
                if (!divisions[teamData.division]) {
                    divisionsSorted.push(addDivision(teamData.division));
                }
                var team = new Team(teamData, response.tempUsers);
                if (team.tid == oldOpenTeamTID)
                    newToggleTeam = team;
                if (response.tid && team.tid == response.tid)
                    bottomRank = i + 1;
                if (pageState.isCreator) {
                    team.code = response.teamCodes[i];
                }
                if (!team.individual)
                    divisions[team.division].teamDOM.appendChild(team.dom.tr);
                var _loop_1 = function (student) {
                    var tr = document.createElement("tr");
                    tr.classList.add("_" + student.uid);
                    tr.onclick = function () {
                        Team.selectStudent(student);
                    };
                    tr.innerHTML = "<td>" + student.name + "</td>";
                    selectStudentFragment.appendChild(tr);
                    if (student.type != StudentType.ALTERNATE) {
                        if (student.mcScore) {
                            numWrittenSubmitted++;
                            writtenSum += student.mcScore;
                        }
                        numStudents++;
                    }
                };
                for (var _i = 0, _a = team.students; _i < _a.length; _i++) {
                    var student = _a[_i];
                    _loop_1(student);
                }
                handsOnSum += team.frqScore;
            }
            divisionsSorted.sort(function (d1, d2) {
                return d1.value.localeCompare(d2.value);
            });
            var firstDivision = divisionsSorted[0] ? divisionsSorted[0].value : ""; // The first division to show
            for (var _b = 0, divisionsSorted_1 = divisionsSorted; _b < divisionsSorted_1.length; _b++) {
                var divisionOption = divisionsSorted_1[_b];
                dom.divisionSelector.appendChild(divisionOption);
            }
            for (var division in divisions) {
                if (pageState.mcExists) {
                    divisions[division].writtenScoreboard.sort(function (s1, s2) {
                        var s1IsUndefined = s1.mcScore == null; //typeof s1.mcScore == "undefined";
                        var s2IsUndefined = s2.mcScore == null; //typeof s2.mcScore == "undefined";
                        if (s1IsUndefined && s2IsUndefined)
                            return 0;
                        else if (s1IsUndefined && !s2IsUndefined)
                            return 1;
                        else if (!s1IsUndefined && s2IsUndefined)
                            return -1;
                        if (s1.mcScore == s2.mcScore) { // Tiebreak
                            return s2.mcNumCorrect / (s2.mcNumIncorrect + s2.mcNumCorrect) - s1.mcNumCorrect / (s1.mcNumIncorrect + s1.mcNumCorrect);
                        }
                        else if (s1.mcScore < s2.mcScore)
                            return 1;
                        else
                            return -1;
                    });
                    var writtenTestFragment = document.createDocumentFragment();
                    for (var _c = 0, _d = divisions[division].writtenScoreboard; _c < _d.length; _c++) {
                        var student = _d[_c];
                        // if (!pageState.showAlternates && student.type == StudentType.ALTERNATE) continue;
                        writtenTestFragment.appendChild(student.dom.tr);
                    }
                    divisions[division].writtenDOM.appendChild(writtenTestFragment);
                }
                if (pageState.frqExists) {
                    divisions[division].handsOnScoreboard.sort(function (t1, t2) {
                        return t2.frqScore - t1.frqScore;
                    });
                    var handsOnFragment = document.createDocumentFragment();
                    for (var _e = 0, _f = divisions[division].handsOnScoreboard; _e < _f.length; _e++) {
                        var team = _f[_e];
                        handsOnFragment.appendChild(team.dom.frqTR);
                    }
                    divisions[division].handsOnDOM.appendChild(handsOnFragment);
                }
            }
            if (response.isCreator) {
                if (pageState.mcExists) {
                    dom.writtenSubmissionCount.innerText = numWrittenSubmitted + " submitted";
                    if (numWrittenSubmitted == 0)
                        dom.writtenAverage.innerText = "0 average";
                    else
                        dom.writtenAverage.innerText = Math.round(writtenSum / numWrittenSubmitted) + " average";
                }
                if (pageState.frqExists) {
                    dom.handsOnSubmissionCount.innerText = "" + response.numHandsOnSubmitted;
                    if (response.teams.length == 0)
                        dom.handsOnSubmissionAverage.innerText = "0";
                    else
                        dom.handsOnSubmissionAverage.innerText = "" + Math.round(handsOnSum / response.teams.length);
                }
                dom.numTeams.innerText = "" + response.teams.length;
                dom.numUsers.innerText = "" + numStudents;
            }
            else if (response.tid) {
                dom.bottomRank.innerText = ordinal(bottomRank);
                dom.bottomOutOf.innerText = bottomOutOf;
            }
            dom.teamList.innerHTML = "";
            if (pageState.mcExists)
                dom.writtenScoreboardTable.innerHTML = "";
            if (pageState.frqExists)
                dom.handsOnScoreboardTable.innerHTML = "";
            if (pageState.isCreator) {
                var selectStudentFromClassFragment = document.createDocumentFragment(); // Add in the students from their class
                var _loop_2 = function (classStudent) {
                    var student = Student.students["" + classStudent[1]];
                    if (student == null) {
                        classStudent.push(StudentType[StudentType.PRIMARY]);
                        student = new Student(classStudent);
                    }
                    var tr = document.createElement("tr");
                    tr.classList.add("_" + student.uid);
                    tr.onclick = function () {
                        Team.selectStudent(student);
                    };
                    tr.innerHTML = "<td>" + student.name + "</td>";
                    selectStudentFromClassFragment.appendChild(tr);
                };
                for (var _g = 0, _h = response.studentsInClass; _g < _h.length; _g++) {
                    var classStudent = _h[_g];
                    _loop_2(classStudent);
                }
                dom.selectStudentFromClass.innerHTML = "";
                dom.selectStudentFromClass.appendChild(selectStudentFromClassFragment);
                dom.selectSignedUpStudent.innerHTML = "";
                dom.selectSignedUpStudent.appendChild(selectStudentFragment);
            }
            if (pageState.viewingDivision != null && divisions[pageState.viewingDivision]) {
                for (var _j = 0, divisionsSorted_2 = divisionsSorted; _j < divisionsSorted_2.length; _j++) {
                    var divisionOption = divisionsSorted_2[_j];
                    if (divisionOption.value == pageState.viewingDivision) {
                        divisionOption.selected = true;
                        break;
                    }
                }
                selectDivision(pageState.viewingDivision);
            }
            else
                selectDivision(firstDivision);
            var oldEditingTeam = pageState.editingTeam;
            Team.toggleTeam(newToggleTeam, false);
            if (oldEditingTeam)
                Team.editSaveTeam();
        }, "scoreboardOpenTeamFeedback": function (response) {
            var callback = pageState.saveTeamCallbacks.pop();
            if (response.isError)
                addErrorBox(dom.openTeamFeedbackCnt, response.msg, true);
            else {
                if (callback)
                    callback();
                else
                    addSuccessBox(dom.openTeamFeedbackCnt, response.msg, true);
            }
        },
        // Add in the results for the student search. The student objects are in the form [name, uid, student type, mcScore]
        "ssearch": function (response) {
            var fragment = document.createDocumentFragment();
            var _loop_3 = function (student) {
                var data = Student.students["" + student[1]];
                if (data == null) {
                    data = new Student(student);
                }
                var tr = document.createElement("tr");
                tr.classList.add("_" + data.uid);
                tr.onclick = function () {
                    Team.selectStudent(data);
                };
                tr.innerHTML = "<td>" + data.name + "</td>";
                fragment.appendChild(tr);
            };
            for (var _i = 0, _a = response.students; _i < _a.length; _i++) {
                var student = _a[_i];
                _loop_3(student);
            }
            dom.studentSearchTable.innerHTML = "";
            dom.studentSearchTable.appendChild(fragment);
        }, "loadGlobalTeams": function (response) {
            if (pageState.globalTeamsLoaded)
                return;
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
                addErrorBox(dom.errorBoxERROR, response.error, true);
                return;
            }
            else if (response.reload) {
                // let team: Team = new Team(response);
                // Team.toggleTeam(team);
                pageState.openTeam = { tid: response.tid }; // So that the toggle team works correctly when we reload the scoreboard
                requestLoadScoreboard();
                hideSignup();
                closeAddExistingTeam();
                return;
            }
        },
        // Adds a new temporary student to the team
        "addTempStudent": function (response) {
            addSuccessBox(dom.openTeamFeedbackCnt, "Team saved successfully.", true);
            var team = teams["" + response.tid]; // The team they are in
            var student = new Student([response.name, response.uid, StudentType[StudentType.PRIMARY]], team);
            student.render();
            if (pageState.mcExists)
                dom.writtenScoreboardTable.appendChild(student.dom.tr);
            student.temp = true;
            student.uname = response.uname;
            student.password = response.password;
            team.students.push(student);
            if (pageState.openTeam.tid == team.tid) {
                Team.renderOpenTeam(team);
                Team.editSaveTeam();
            }
        },
        "updateFRQSubmission": function (response) {
            if (frqSubmissionMap[response.submissionID]) {
                var div = frqSubmissionMap[response.submissionID].dom.div;
                try {
                    div.removeChild(div.querySelector(".frqUpdateWrapper"));
                }
                catch (e) { }
                addFRQOutputHTML(frqSubmissionMap[response.submissionID].input, response.newOutput, response.outputFile, frqSubmissionMap[response.submissionID].result, div);
                // let element:HTMLElement = div.querySelector(".outputCnt");
                addSuccessBox(document.getElementById("frqSubmissionEditorResponse"), "Regraded submission.", true);
            }
        },
        "rosterUploaded": function (response) {
            dom.uploadRosterBox.style.display = "none";
        },
        "blockFRQ": function (response) {
            var submission = frqSubmissionMap[response.submissionID];
            if (submission) {
                submission.dom.tr.classList.add("blocked");
                submission.blocked = true;
            }
        },
        "unblockFRQ": function (response) {
            var submission = frqSubmissionMap[response.submissionID];
            if (submission) {
                submission.dom.tr.classList.remove("blocked");
                submission.blocked = false;
            }
        },
        "fetchWrittenStatistics": function (response) {
            var data = [["Written Statistics"], [], ["#", "% Correct", "# Correct", "# Attempted"]];
            for (var i = 0; i < response.stats.length; i++) {
                var question = response.stats[i];
                var percentCorrect = 0;
                if (question[1] > 0)
                    percentCorrect = question[0] / question[1];
                data.push(["" + (i + 1), (percentCorrect * 100).toFixed(2), "" + question[0], "" + question[1]]);
            }
            exportCSV(data, ",", "scoreboard");
        }
    }
};
function addDivision(division) {
    // First, add in the table headers
    var headers = "<th>Team</th>";
    if (pageState.mcExists)
        headers += "<th class='right'>Written</th>";
    if (pageState.frqExists)
        headers += "<th class='right'>Hands-On</th>";
    headers += "<th class='right'>Total</th>";
    var generalFragment = document.createElement("span");
    // let generalFragment = document.createDocumentFragment();   // A collection of table row elements for the general table
    var headerDOM = document.createElement("tr");
    headerDOM.innerHTML = headers;
    generalFragment.appendChild(headerDOM);
    divisions[division] = {
        writtenScoreboard: [],
        handsOnScoreboard: [],
        teamScoreboard: [],
        handsOnDOM: null,
        writtenDOM: null,
        teamDOM: generalFragment
    };
    var divisionOption = document.createElement("option");
    divisionOption.innerText = division;
    divisionOption.value = division;
    if (pageState.mcExists) {
        var writtenTestFragment = document.createElement("span"); // document.createDocumentFragment();
        var writtenTestTR = document.createElement("tr");
        writtenTestTR.innerHTML = "<th>Name</th><th>Team</th><th>Correct</th><th>Incorrect</th><th>% Correct</th><th>Total</th>";
        writtenTestFragment.appendChild(writtenTestTR);
        divisions[division].writtenDOM = writtenTestFragment;
    }
    if (pageState.frqExists) {
        var handsOnFragment = document.createElement("span"); // document.createDocumentFragment();    // A collection of table rows for the hands-on table
        var handsOnHeader = document.createElement("tr");
        handsOnHeader.innerHTML = "<th></th><th>Total</th>";
        for (var _i = 0, _a = pageState.frqProblemMap; _i < _a.length; _i++) {
            var problem = _a[_i];
            var problemTH = document.createElement("th");
            problemTH.innerText = problem;
            handsOnHeader.appendChild(problemTH);
        }
        handsOnFragment.appendChild(handsOnHeader);
        divisions[division].handsOnDOM = handsOnFragment;
    }
    return divisionOption;
}
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
    get errorBoxERROR() { return this.getHelper(config.IDs.errorBoxERROR); },
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
    get editSaveTeam() { return this.getHelper(config.IDs.editSaveTeam); },
    get addPrimaryCompetitor() { return this.getHelper(config.IDs.addPrimaryCompetitor); },
    get selectStudent() { return this.getHelper(config.IDs.selectStudent); },
    get selectStudentList() { return this.getHelper(config.IDs.selectStudentList); },
    get openTeamFeedbackCnt() { return this.getHelper(config.IDs.openTeamFeedbackCnt); },
    get openTeamCode() { return this.getHelper(config.IDs.openTeamCode); },
    get openTeamIsIndividual() { return this.getHelper(config.IDs.openTeamIsIndividual); },
    get selectGlobalTeam() { return this.getHelper(config.IDs.selectGlobalTeam); },
    get selectGlobalTeamList() { return this.getHelper(config.IDs.selectGlobalTeamList); },
    get deleteMessage() { return this.getHelper(config.IDs.deleteMessage); },
    get deleteConfirmationCnt() { return this.getHelper(config.IDs.deleteConfirmationCnt); },
    get deleteSubtitle() { return this.getHelper(config.IDs.deleteSubtitle); },
    get deleteTeam() { return this.getHelper(config.IDs.deleteTeam); },
    get signUpIsAlternateCnt() { return this.getHelper(config.IDs.signUpIsAlternateCnt); },
    get signUpIsAlternate() { return this.getHelper(config.IDs.signUpIsAlternate); },
    get writtenSubmissionCount() { return this.getHelper(config.IDs.writtenSubmissionCount); },
    get writtenAverage() { return this.getHelper(config.IDs.writtenAverage); },
    get handsOnSubmissionCount() { return this.getHelper(config.IDs.handsOnSubmissionCount); },
    get handsOnSubmissionAverage() { return this.getHelper(config.IDs.handsOnSubmissionAverage); },
    get numTeams() { return this.getHelper(config.IDs.numTeams); },
    get numUsers() { return this.getHelper(config.IDs.numUsers); },
    get selectStudentFromClass() { return this.getHelper(config.IDs.selectStudentFromClass); },
    get selectSignedUpStudent() { return this.getHelper(config.IDs.selectSignedUpStudent); },
    get studentSearch() { return this.getHelper(config.IDs.studentSearch); },
    get studentSearchTable() { return this.getHelper(config.IDs.studentSearchTable); },
    get downloadScoreboard() { return this.getHelper(config.IDs.downloadScoreboard); },
    get fnameTemp() { return this.getHelper(config.IDs.fnameTemp); },
    get lnameTemp() { return this.getHelper(config.IDs.lnameTemp); },
    get inputSchool() { return this.getHelper(config.IDs.inputSchool); },
    get generalScoreboard() { return this.getHelper(config.IDs.generalScoreboard); },
    get writtenScoreboard() { return this.getHelper(config.IDs.writtenScoreboard); },
    get writtenScoreboardTable() { return this.getHelper(config.IDs.writtenScoreboardTable); },
    get handsOnScoreboard() { return this.getHelper(config.IDs.handsOnScoreboard); },
    get handsOnScoreboardTable() { return this.getHelper(config.IDs.handsOnScoreboardTable); },
    get playBell() { return this.getHelper(config.IDs.playBell); },
    get frqSubmissions() { return this.getHelper(config.IDs.frqSubmissions); },
    get frqTextfile() { return this.getHelper(config.IDs.frqTextfile); },
    get uploadFileProxy() { return this.getHelper(config.IDs.uploadFileProxy); },
    get uploadRosterProxy() { return this.getHelper(config.IDs.uploadRosterProxy); },
    get uploadRosterBox() { return this.getHelper(config.IDs.uploadRosterBox); },
    get resetConfirmationCnt() { return this.getHelper(config.IDs.resetConfirmationCnt); },
    get teamDivision() { return this.getHelper(config.IDs.teamDivision); },
    get divisionSelector() { return this.getHelper(config.IDs.divisionSelector); },
    get deleteTeamsConfirmationCnt() { return this.getHelper(config.IDs.deleteTeamsConfirmationCnt); },
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
function ordinal(i) {
    var sufixes = ["th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"];
    switch (i % 100) {
        case 11:
        case 12:
        case 13:
            return i + "th";
        default:
            return i + sufixes[i % 10];
    }
}
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
            studentUL.innerHTML = "<b>Primaries</b><br>";
            for (var _i = 0, _a = team.nonAlts; _i < _a.length; _i++) {
                var student = _a[_i];
                var studentLI = document.createElement("li");
                studentLI.classList.add("student");
                studentLI.innerText = student[0];
                studentUL.appendChild(studentLI);
            }
            var altHeader = document.createElement("b");
            altHeader.innerText = "Written Specialist";
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
var StudentType;
(function (StudentType) {
    StudentType[StudentType["PRIMARY"] = 0] = "PRIMARY";
    StudentType[StudentType["WRITTEN_SPECIALIST"] = 1] = "WRITTEN_SPECIALIST";
    StudentType[StudentType["ALTERNATE"] = 2] = "ALTERNATE";
})(StudentType || (StudentType = {}));
var Student = /** @class */ (function () {
    function Student(data, team) {
        this.temp = false; // If they are a temporary user
        this.dom = { tr: null, changeRole: null };
        this.name = data[0];
        this.uid = data[1];
        switch (data[2]) {
            case "PRIMARY":
                this.type = StudentType.PRIMARY;
                break;
            case "WRITTEN_SPECIALIST":
                this.type = StudentType.WRITTEN_SPECIALIST;
                break;
            default:
                this.type = StudentType.ALTERNATE;
                break;
        }
        this.team = team;
        Student.students["" + this.uid] = this;
        if (data[3]) {
            this.mcNumCorrect = data[3][0];
            this.mcNumIncorrect = data[3][1];
            this.mcScore = Math.abs(pageState.mcCorrectPoints) * this.mcNumCorrect - Math.abs(pageState.mcIncorrectPoints) * this.mcNumIncorrect;
        }
        else {
            this.mcNumIncorrect = 0;
            this.mcNumCorrect = 0;
            this.mcScore = null;
        }
    }
    Student.prototype.render = function () {
        var team = this.team;
        if (pageState.mcExists && team != null) {
            this.dom.tr = document.createElement("tr");
            this.dom.tr.classList.add("student");
            if (this.type == StudentType.ALTERNATE)
                this.dom.tr.classList.add("alternateStudent");
            this.dom.tr.onclick = function () {
                Team.toggleTeam(team);
            };
            var nameTD = document.createElement("td");
            nameTD.innerText = this.name;
            this.dom.tr.appendChild(nameTD);
            var tnameTD = document.createElement("td");
            tnameTD.innerText = this.team.tname;
            this.dom.tr.appendChild(tnameTD);
            var numCorrectTD = document.createElement("td");
            if (this.mcScore != null)
                numCorrectTD.innerText = "" + this.mcNumCorrect;
            this.dom.tr.appendChild(numCorrectTD);
            var numIncorrectTD = document.createElement("td");
            if (this.mcScore != null)
                numIncorrectTD.innerText = "" + this.mcNumIncorrect;
            this.dom.tr.appendChild(numIncorrectTD);
            var percentCorrectTD = document.createElement("td");
            if (this.mcScore != null) {
                percentCorrectTD.innerText = getPercentageCorrect(this.mcNumCorrect, this.mcNumIncorrect);
            }
            this.dom.tr.appendChild(percentCorrectTD);
            var totalTD = document.createElement("td");
            if (this.mcScore != null)
                totalTD.innerText = "" + this.mcScore;
            else
                totalTD.innerText = "Not Taken";
            this.dom.tr.appendChild(totalTD);
        }
    };
    Student.students = {};
    return Student;
}());
var frqSubmissions = [];
var frqSubmissionMap = {};
var FRQSubmission = /** @class */ (function () {
    function FRQSubmission(frqSubmission) {
        this.dom = {
            tr: null,
            pNameTD: null,
            tNameTD: null,
            gradedTD: null,
            resultTD: null,
            timeTD: null,
            div: null
        };
        this.update(frqSubmission);
        frqSubmissions.push(this);
        frqSubmissionMap[this.submissionId] = this;
    }
    FRQSubmission.prototype.update = function (frqSubmission) {
        this.problemName = frqSubmission[0];
        this.tname = frqSubmission[1];
        this.graded = frqSubmission[2];
        this.result = frqSubmission[3];
        this.timestamp = frqSubmission[4];
        this.submissionId = frqSubmission[5];
        this.blocked = frqSubmission[6];
        this.render();
    };
    FRQSubmission.prototype.render = function () {
        var thisSubmission = this;
        if (!this.dom.tr) {
            this.dom.tr = dom.frqSubmissionsTable.insertRow(1);
            this.dom.tr.onclick = function () {
                showFRQSubmission(thisSubmission);
            };
            this.dom.tr.id = "frq_" + this.submissionId;
            if (this.blocked)
                this.dom.tr.classList.add("blocked");
        }
        else { // The tr has already been initialized, so delete its contents
            this.dom.tr.innerHTML = "";
        }
        this.dom.pNameTD = document.createElement("td");
        this.dom.pNameTD.innerText = this.problemName;
        this.dom.tr.appendChild(this.dom.pNameTD);
        this.dom.tNameTD = document.createElement("td");
        this.dom.tNameTD.innerText = this.tname;
        this.dom.tr.appendChild(this.dom.tNameTD);
        this.dom.gradedTD = document.createElement("td");
        this.dom.gradedTD.innerText = "" + this.graded;
        if (this.graded)
            this.dom.gradedTD.classList.add("graded");
        else
            this.dom.gradedTD.classList.add("notGraded");
        // gradedTD.id = "showFRQSubmissionGraded" + this.submissionId;
        this.dom.tr.appendChild(this.dom.gradedTD);
        this.dom.resultTD = document.createElement("td");
        this.dom.resultTD.innerText = this.result;
        this.dom.tr.appendChild(this.dom.resultTD);
        this.dom.timeTD = document.createElement("td");
        this.dom.timeTD.innerText = getTime(this.timestamp);
        this.dom.tr.appendChild(this.dom.timeTD);
        return this.dom.tr;
    };
    return FRQSubmission;
}());
var divisions;
var teams = {}; // Maps tids to their team
var Team = /** @class */ (function () {
    function Team(data, tempData) {
        this.editedSinceLastSave = false; // If this team has been edited since the last save
        this.dom = { tr: null, mcTD: null, frqTR: null };
        function createStudent(data) {
            var student = new Student(data, thisTeam);
            student.render();
            if (pageState.isCreator) { // If they are the creator, check if this student is a temporary user
                var temp = tempData["" + student.uid]; // First element is their username, second is their password
                if (temp) {
                    student.temp = true;
                    student.uname = temp[0];
                    student.password = temp[1];
                }
            }
            if (pageState.mcExists) {
                divisions[thisTeam.division].writtenScoreboard.push(student);
            }
            return student;
        }
        var thisTeam = this;
        this.individual = data.individual;
        this.tname = data.tname;
        this.school = data.school;
        this.tid = data.tid;
        this.students = [];
        this.division = data.division;
        if (pageState.frqExists && !this.individual)
            divisions[thisTeam.division].handsOnScoreboard.push(this);
        for (var _i = 0, _a = data.students; _i < _a.length; _i++) {
            var studentData = _a[_i];
            this.students.push(createStudent(studentData));
        }
        if (pageState.frqExists)
            this.frqScore = data.frq;
        else
            this.frqScore = 0;
        this.mcScore = 0;
        if (pageState.mcExists) {
            var scores = []; // A list of sorted scores
            for (var i = 0; i < this.students.length; i++) {
                var student = this.students[i];
                if (student.type == StudentType.ALTERNATE)
                    continue;
                if (student.mcScore) {
                    scores.push(student.mcScore);
                }
            }
            scores.sort(function (a, b) {
                return a - b;
            });
            for (var i = scores.length - 1, j = scores.length - pageState.mcNumScoresToKeep - 1; i > j && i >= 0; i--) {
                this.mcScore += scores[i];
            }
        }
        this.frqResponses = data.frqResponses;
        divisions[this.division].teamScoreboard.push(this);
        teams["" + this.tid] = this;
        this.render();
    }
    // Creates the html objects
    Team.prototype.render = function () {
        function createGeneralRow(team) {
            team.dom.tr = document.createElement("tr");
            team.dom.tr.classList.add("team");
            var tnameTD = document.createElement("td");
            tnameTD.innerText = team.tname;
            team.dom.tr.appendChild(tnameTD);
            var totalScore = 0;
            if (pageState.mcExists) {
                var mcTD = document.createElement("td");
                mcTD.classList.add("right");
                mcTD.innerText = "" + team.mcScore;
                totalScore += team.mcScore;
                team.dom.mcTD = mcTD;
                team.dom.tr.appendChild(mcTD);
            }
            if (pageState.frqExists) {
                var frqTD = document.createElement("td");
                frqTD.classList.add("right");
                totalScore += team.frqScore;
                frqTD.innerText = "" + team.frqScore;
                team.dom.tr.appendChild(frqTD);
            }
            var totalTD = document.createElement("td");
            totalTD.classList.add("right");
            totalTD.innerText = "" + totalScore;
            team.dom.tr.appendChild(totalTD);
            team.dom.tr.onclick = function () {
                Team.toggleTeam(team);
            };
        }
        function createFRQRow(team) {
            // Create the frq table row
            team.dom.frqTR = document.createElement("tr");
            team.dom.frqTR.onclick = function () {
                Team.toggleTeam(team);
            };
            var tnameTD = document.createElement("td");
            tnameTD.innerText = team.tname;
            team.dom.frqTR.appendChild(tnameTD);
            var totalTD = document.createElement("td");
            totalTD.innerText = "" + team.frqScore;
            team.dom.frqTR.appendChild(totalTD);
            for (var _i = 0, _a = team.frqResponses; _i < _a.length; _i++) {
                var frqResponse = _a[_i];
                var td = document.createElement("td");
                if (frqResponse > 0) {
                    td.innerText = "" + (Math.abs(pageState.frqMaxPoints) - Math.abs(frqResponse - 1) * Math.abs(pageState.frqIncorrectPenalty)); // + " pts";
                    td.classList.add("solved");
                }
                else if (frqResponse == 0) {
                    td.innerText = "0"; // tries";
                    td.classList.add("untried");
                }
                else {
                    var tries = Math.abs(frqResponse);
                    if (tries > 1)
                        td.innerText = "" + tries; // + " tries";
                    else
                        td.innerText = "1"; // try";
                    td.classList.add("tried");
                }
                team.dom.frqTR.appendChild(td);
            }
        }
        createGeneralRow(this);
        if (pageState.frqExists)
            createFRQRow(this);
    };
    // Save the team and copy over the temporary information to the official information
    Team.prototype.save = function (callback) {
        function getTeamData(team) {
            var students = [];
            for (var _i = 0, _a = team.students; _i < _a.length; _i++) {
                var student = _a[_i];
                students.push([student.uid, StudentType[student.type]]);
            }
            return { tid: team.tid, students: students, individual: dom.openTeamIsIndividual.checked };
        }
        var data = ["saveTeam", dom.teamDivision.value, getTeamData(this)];
        /*for(let team of pageState.saveTeamList) {
            data[1].push(getTeamData(team));
        }*/
        // pageState.saveTeamList.length = 0;
        pageState.saveTeamCallbacks.push(callback);
        ws.send(JSON.stringify(data));
        pageState.openTeam.editedSinceLastSave = false;
        addSuccessBox(dom.openTeamFeedbackCnt, "Saving team...", false);
    };
    Team.prototype.delete = function () {
        teams["" + this.tid] = undefined;
        for (var _i = 0, _a = this.students; _i < _a.length; _i++) { // Remove this from the selectStudents list
            var student = _a[_i];
            var studentDOMs = dom.selectSignedUpStudent.querySelector("._" + student.uid);
            for (var _b = 0, studentDOMs_1 = studentDOMs; _b < studentDOMs_1.length; _b++) {
                var studentDOM = studentDOMs_1[_b];
                dom.selectSignedUpStudent.removeChild(studentDOM);
            }
        }
        try {
            dom.teamList.removeChild(this.dom.tr);
            if (pageState.frqExists)
                dom.handsOnScoreboardTable.removeChild(this.dom.frqTR);
        }
        catch (e) { }
        dom.teamCnt.style.display = "none";
        pageState.openTeam.dom.tr.classList.remove("selected");
        pageState.openTeam = null;
        ws.send("[\"deleteTeam\"," + this.tid + "]");
    };
    Team.prototype.deleteStudent = function (student) {
        var studentDOM = dom.selectSignedUpStudent.getElementsByClassName("_" + student.uid)[0]; // Remove this from the selectStudents list
        if (studentDOM != null) {
            dom.selectSignedUpStudent.removeChild(studentDOM);
        }
        if (pageState.mcExists) {
            if (student.mcScore) {
                this.mcScore -= student.mcScore;
                this.dom.mcTD.innerText = "" + this.mcScore;
            }
            try {
                dom.writtenScoreboardTable.removeChild(student.dom.tr);
            }
            catch (e) { }
        }
        for (var i = 0; i < this.students.length; i++) {
            if (this.students[i].uid == student.uid)
                this.students.splice(i, 1);
        }
        Team.renderOpenTeam(this);
        Team.editSaveTeam();
        this.save();
        // ws.send("[\"deleteStudent\","+this.tid+","+student.uid+"]");
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
    };
    Team.closeSelectStudent = function () {
        dom.selectStudent.style.display = "none";
    };
    Team.selectStudent = function (student) {
        if (!pageState.isCreator)
            return;
        var openTeam = pageState.openTeam;
        // This must come first, so that if the student is already in this team, this works
        // If team is null, they are adding a student from their class.
        if (student.team != null) {
            var newStudentsList = [];
            for (var _i = 0, _a = student.team.students; _i < _a.length; _i++) {
                var s = _a[_i];
                if (s.uid != student.uid)
                    newStudentsList.push(s);
            }
            student.team.students = newStudentsList;
        }
        student.team = openTeam;
        student.render();
        if (!student.team && pageState.mcExists) {
            dom.writtenScoreboardTable.appendChild(student.dom.tr);
        }
        var alreadyInTeam = false;
        for (var _b = 0, _c = openTeam.students; _b < _c.length; _b++) {
            var s = _c[_b];
            if (student.uid == s.uid) {
                alreadyInTeam = true;
                break;
            }
        }
        if (!alreadyInTeam) {
            pageState.openTeam.students.push(student);
        }
        Team.renderOpenTeam(openTeam);
        Team.editSaveTeam();
        Team.closeSelectStudent();
    };
    Team.renderOpenTeam = function (team) {
        // gets the table row for a student. [0] = uname, [1] = uid, [2] = mc score
        function getStudentTR(student) {
            var tr = document.createElement("tr");
            tr.classList.add("team");
            var nameTD = document.createElement("td");
            var nameP = document.createElement("p");
            nameP.innerText = student.name;
            nameTD.appendChild(nameP);
            if (student.temp) {
                var unameP = document.createElement("p");
                unameP.innerHTML = "<b>Username:</b>";
                var unameNode = document.createTextNode(student.uname);
                unameP.appendChild(unameNode);
                nameTD.appendChild(unameP);
                var passwordP = document.createElement("p");
                passwordP.innerHTML = "<b>Password:</b>";
                var passwordNode = document.createTextNode(student.password);
                passwordP.appendChild(passwordNode);
                nameTD.appendChild(passwordP);
            }
            tr.appendChild(nameTD);
            var changeRoleTd = document.createElement("td");
            var changeRole = document.createElement("select");
            student.dom.changeRole = changeRole;
            changeRole.disabled = true;
            changeRoleTd.appendChild(changeRole);
            // @ts-ignore
            Object.values(StudentType).forEach(function (value) {
                var option = document.createElement("option");
                var asString;
                switch (value) {
                    case StudentType.PRIMARY:
                        asString = "Primary";
                        break;
                    case StudentType.ALTERNATE:
                        asString = "Alternate";
                        break;
                    case StudentType.WRITTEN_SPECIALIST:
                        asString = "Written Specialist";
                        break;
                    default:
                        return;
                }
                option.innerText = asString;
                option.value = value;
                if (value == student.type) {
                    option.selected = true;
                }
                changeRole.appendChild(option);
            });
            changeRole.onchange = function () {
                var selected = changeRole.selectedOptions[0].value;
                if (selected == "0")
                    student.type = StudentType.PRIMARY;
                else if (selected == "1")
                    student.type = StudentType.WRITTEN_SPECIALIST;
                else
                    student.type = StudentType.ALTERNATE;
            };
            tr.appendChild(changeRoleTd);
            /* if(pageState.mcExists) {
                let mcTD = document.createElement("td");
                mcTD.classList.add("right");
                if(student.mcScore != null) mcTD.innerText = student.mcScore + "pts";
                else mcTD.innerText = "Not taken";
                tr.appendChild(mcTD);
            }*/
            var deleteTD = document.createElement("td");
            deleteTD.classList.add("editTeam");
            deleteTD.innerHTML = "<img src='/res/console/delete.svg' class='deleteStudent'/>";
            deleteTD.onclick = function () {
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
        dom.teamCnt.style.display = "block";
        dom.openTeamName.innerText = team.tname;
        if (pageState.isCreator) {
            dom.openTeamCode.innerText = team.code;
            dom.openTeamIsIndividual.checked = team.individual;
            dom.openTeamIsIndividual.onclick = null;
            dom.openTeamIsIndividual.disabled = true;
            dom.teamDivision.disabled = true;
        }
        dom.teamDivision.value = team.division;
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
        pageState.openTeam = team;
        pageState.editingTeam = false;
    };
    /*
     Toggles between looking at a team and not. If the given team object is null or equal to the currently open team,
     then close the 'teamCnt'. Otherwise, open it and set its information properly.
     If requestLoad is true, it will fire the requestLoadScoreboards
     */
    Team.toggleTeam = function (team, requestLoad) {
        if (requestLoad === void 0) { requestLoad = true; }
        if (!team || team == pageState.openTeam) { // Close the "#teamCnt"
            dom.teamCnt.style.display = "none";
            pageState.openTeam.dom.tr.classList.remove("selected");
            if (requestLoad && team.editedSinceLastSave)
                requestLoadScoreboard();
            if (pageState.mcExists && pageState.openTeam) {
                for (var _i = 0, _a = pageState.openTeam.students; _i < _a.length; _i++) {
                    var student = _a[_i];
                    student.dom.tr.classList.remove("selected");
                }
                if (pageState.openTeam.alt)
                    pageState.openTeam.alt.dom.tr.classList.remove("selected");
            }
            if (pageState.frqExists && pageState.openTeam) {
                pageState.openTeam.dom.frqTR.classList.remove("selected");
            }
            pageState.openTeam = null;
        }
        else {
            deleteErrorSuccessBox(dom.openTeamFeedbackCnt);
            if (pageState.openTeam) {
                pageState.openTeam.dom.tr.classList.remove("selected");
                if (pageState.mcExists) {
                    for (var _b = 0, _c = pageState.openTeam.students; _b < _c.length; _b++) {
                        var student = _c[_b];
                        student.dom.tr.classList.remove("selected");
                    }
                    if (pageState.openTeam.alt)
                        pageState.openTeam.alt.dom.tr.classList.remove("selected");
                }
                if (pageState.frqExists)
                    pageState.openTeam.dom.frqTR.classList.remove("selected");
                if (requestLoad && pageState.openTeam.editedSinceLastSave)
                    requestLoadScoreboard();
            }
            team.dom.tr.classList.add("selected");
            if (pageState.mcExists) {
                for (var _d = 0, _e = team.students; _d < _e.length; _d++) {
                    var student = _e[_d];
                    student.dom.tr.classList.add("selected");
                }
            }
            if (pageState.frqExists)
                team.dom.frqTR.classList.add("selected");
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
                pageState.openTeam.editedSinceLastSave = true;
                dom.openTeamIsIndividual.onclick = function () {
                    pageState.openTeam.individual = dom.openTeamIsIndividual.checked;
                };
                dom.editSaveTeam.src = "/res/console/save.svg";
                dom.teamCnt.classList.add("editing");
                dom.openTeamIsIndividual.disabled = false;
                dom.addPrimaryCompetitor.style.display = "block";
                dom.teamDivision.disabled = false;
                for (var _i = 0, _a = pageState.openTeam.students; _i < _a.length; _i++) {
                    var student = _a[_i];
                    student.dom.changeRole.disabled = false;
                }
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
            if (questionDOM.classList.contains("saqQuestion")) { // This is an saq question
                var tableCell = questionDOM.lastChild;
                if (tableCell == null)
                    continue;
                var input = tableCell.firstChild;
                input.value = savedMCAnswer;
            }
            else { // This is an mc question
                var tableCell = questionDOM.childNodes.item(savedMCAnswer.charCodeAt(0) - aCharCode + 1);
                if (tableCell == null)
                    continue;
                var div = tableCell.firstChild;
                if (div instanceof HTMLInputElement)
                    return; // In this case it is an saq question
                div.classList.add("mcSelected");
            }
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
// Shows the scoreboard for a certain division
function selectDivision(division) {
    if (pageState.openTeam != null)
        Team.toggleTeam(null, false);
    pageState.viewingDivision = division;
    dom.teamList.innerHTML = "";
    dom.teamList.appendChild(divisions[division].teamDOM);
    if (pageState.mcExists) {
        dom.writtenScoreboardTable.innerHTML = "";
        dom.writtenScoreboardTable.appendChild(divisions[division].writtenDOM);
    }
    if (pageState.frqExists) {
        dom.handsOnScoreboardTable.innerHTML = "";
        dom.handsOnScoreboardTable.appendChild(divisions[division].handsOnDOM);
    }
}
// Starts the load scoreboard
// @ts-ignore
function requestLoadScoreboard() {
    return __awaiter(this, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    if (!(ws == null || ws.readyState === 0)) return [3 /*break*/, 2];
                    // @ts-ignore
                    return [4 /*yield*/, new Promise(function (r) { return setTimeout(r, 100); })];
                case 1:
                    // @ts-ignore
                    _a.sent();
                    return [3 /*break*/, 0];
                case 2:
                    ws.send("[\"loadScoreboard\"]");
                    ws.send("[\"loadFRQSubmissions\"]");
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
function sortFRQSubmissions(compare, ascending) {
    frqSubmissions.sort(function (f1, f2) {
        if (ascending)
            return compare(f1, f2);
        else
            return -compare(f1, f2);
    });
    var table = dom.frqSubmissionsTable;
    var rows = table.rows;
    for (var i = 0; i < frqSubmissions.length; i++) {
        table.insertBefore(frqSubmissions[i].dom.tr, rows[i + 1]);
    }
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
    deleteErrorSuccessBox(dom.errorBoxERROR);
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
        dom.signUpIsAlternateCnt.style.display = "none";
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
        if (pageState.writtenSpecialistExists || pageState.writtenSpecialistExists == null) { // If alternates exist or the scoreboard hasn't loaded
            dom.signUpIsAlternateCnt.style.display = "block";
        }
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
function createTeamHelper(tname, showErrors, callback) {
    $.ajax({
        url: window.location.href,
        method: "POST",
        data: { "action": "createteam", "cid": cid, "tname": tname },
        success: function (result) {
            if (result == null || result["status"] === "error" && showErrors)
                addErrorBox(dom.errorBoxERROR, result["error"], true);
            else if (result["status"] === "success") {
                if (pageState.isCreator) { // If they are the creator, add in the new team
                    var newTeam = void 0;
                    var division = pageState.viewingDivision;
                    if (!division) {
                        division = "6A";
                        addDivision("6A");
                    }
                    if (pageState.frqExists) {
                        var frqResponses = [];
                        frqResponses.length = pageState.frqProblemMap.length;
                        frqResponses.fill(0);
                        newTeam = new Team({
                            tname: result["tname"], school: "", tid: result["tid"],
                            students: [], frq: 0, frqResponses: frqResponses, individual: false, division: division
                        });
                    }
                    else {
                        newTeam = new Team({
                            tname: result["tname"], school: "", tid: result["tid"],
                            students: [], frq: 0, individual: false, division: division
                        });
                    }
                    newTeam.code = result["code"];
                    divisions[newTeam.division].teamDOM.appendChild(newTeam.dom.tr);
                    if (pageState.frqExists)
                        divisions[newTeam.division].handsOnDOM.appendChild(newTeam.dom.frqTR);
                    if (callback)
                        callback(newTeam);
                }
                else
                    location.reload();
            }
        }
    });
}
function createTeam() {
    addSuccessBox(dom.errorBoxERROR, "Creating team...", false);
    var data;
    if (pageState.addingExistingTeam) {
        var data_1 = ["addExistingTeam", dom.teamCode.value, pageState.existingGlobalTeacher.teacher.uid, pageState.existingTeam.tid];
        ws.send(JSON.stringify(data_1));
    }
    else {
        createTeamHelper($("#teamCode").val(), true, function (team) {
            Team.toggleTeam(team);
            hideSignup();
        });
    }
}
// Add a team that a teacher has created
function showAddExistingTeam() {
    if (!pageState.isCreator)
        return;
    if (!pageState.globalTeamsLoaded)
        ws.send("[\"fetchGlobalTeams\"]");
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
    setCookie(cid + "MC", JSON.stringify(choices), 1);
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
function deleteErrorSuccessBox(box) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (errorBox)
        box.removeChild(errorBox);
}
function addErrorBox(box, error, timeout) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (!errorBox) {
        errorBox = document.createElement("div");
        errorBox.classList.add("error");
        errorBox.id = box.id + "ERROR";
        errorBox.innerText = error;
        box.insertAdjacentElement("afterbegin", errorBox);
    }
    else {
        errorBox.classList.remove("success");
        errorBox.classList.add("error");
        errorBox.innerHTML = "" + error;
    }
    if (timeout) {
        setTimeout(function () {
            try {
                box.removeChild(errorBox);
            }
            catch (e) {
            }
        }, 10000);
    }
}
function addSuccessBox(box, success, timeout) {
    var errorBox = document.getElementById(box.id + "ERROR");
    if (!errorBox) {
        errorBox = document.createElement("div");
        errorBox.classList.add("success");
        errorBox.id = box.id + "ERROR";
        errorBox.innerText = success;
        box.insertAdjacentElement("afterbegin", errorBox);
    }
    else {
        errorBox.classList.remove("error");
        errorBox.classList.add("success");
        errorBox.innerHTML = "" + success;
    }
    if (timeout) {
        setTimeout(function () {
            try {
                box.removeChild(errorBox);
            }
            catch (e) {
            }
        }, 10000);
    }
}
var box = null;
function submitFRQ() {
    if (!box)
        box = document.getElementById("submit");
    addSuccessBox(box, "Submitting...", false);
    var probSelector = document.getElementById("frqProblem");
    var probId = probSelector.options[probSelector.selectedIndex].value;
    var fullPath = dom.frqTextfile.value;
    var filename;
    if (fullPath) {
        var startIndex = (fullPath.indexOf('\\') >= 0 ? fullPath.lastIndexOf('\\') : fullPath.lastIndexOf('/'));
        filename = fullPath.substring(startIndex);
        if (filename.indexOf('\\') === 0 || filename.indexOf('/') === 0) {
            filename = filename.substring(1);
        }
    }
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                var response = JSON.parse(xhr.responseText);
                if (response["status"] == "success") {
                    addSuccessBox(box, response["scored"], true);
                    $("frqProblem" + probId).hide();
                }
                else {
                    addErrorBox(box, response["error"], true);
                }
                // grabFRQProblems();
            }
            else {
                addSuccessBox(box, "SUCCESS: " + filename + " has been submitted.", true);
            }
        }
    };
    xhr.open('POST', window.location.href, true);
    // xhr.setRequestHeader('Content-type', 'multipart/form-data');
    var formData = new FormData();
    formData.append("textfile", dom.frqTextfile.files[0]);
    formData.append("probNum", probId);
    formData.append("action", "submitFRQ");
    xhr.send(formData);
    dom.frqTextfile.value = "";
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
function codeEntered(code) {
    if (code.value.length == 6) { // If the code is fully entered
        // First, put a "verifying" box
        addSuccessBox(dom.errorBoxERROR, "Joining...", false);
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
                        addErrorBox(dom.errorBoxERROR, response["error"], true);
                    }
                }
                else { // A server error occurred. Show an error message
                    addErrorBox(dom.errorBoxERROR, "Whoops! A server error occurred. Contact an admin if the problem continues.", true);
                }
            }
        };
        xhr.open('POST', '/console/competitions', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        xhr.send('cid=' + cid + '&action=jointeam&code=' + code.value + '&isAlternate=' + dom.signUpIsAlternate.checked);
    }
}
function changeMCJudgement(element, uid, probNum) {
    var newJudgement = element.value; // "Correct" or "Incorrect"
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=changeMCJudgement&uid=" + uid + "&judgement=" + newJudgement + "&probNum=" + probNum);
    // document.getElementById("showFRQSubmission"+submissionId).innerText = result_cnt_changeJudgement.options[result_cnt_changeJudgement.selectedIndex].text;
}
function hideFRQSubmission() {
    ws.send("[\"unblockFRQ\"," + showingFRQSubmission.submissionId + "]");
    showingFRQSubmission.dom.div.style.display = "none";
    showingFRQSubmission = null;
    dom.frqSubmissions.style.display = "block";
}
/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
var showingFRQSubmission = null;
// let showingFRQSubmissionTR:HTMLTableRowElement = null;  // The table row element they clicked on to show this frq submission
function showFRQSubmission(submission) {
    function add(element) {
        if (!showingFRQSubmission) {
            dom.frq.appendChild(element);
        }
        else {
            try {
                showingFRQSubmission.dom.div.replaceWith(element);
            }
            catch (e) {
                dom.frq.appendChild(element);
            }
        }
        showingFRQSubmission = submission;
        dom.frqSubmissions.style.display = "none";
        element.style.display = "block";
        ws.send("[\"blockFRQ\"," + submission.submissionId + "]");
        /*if(showingFRQSubmissionTR) showingFRQSubmissionTR.classList.remove("selected");
        showingFRQSubmissionTR = row;
        showingFRQSubmissionTR.classList.add("selected");*/
    }
    if (frqSubmissionMap[submission.submissionId].blocked)
        return;
    if (frqSubmissionMap[submission.submissionId].dom.div != null) {
        add(frqSubmissionMap[submission.submissionId].dom.div);
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
                var graded_1 = response["graded"];
                var blocked = response["blocked"];
                var div = document.createElement("div");
                div.classList.add("frqSubmissionEditor");
                div.innerHTML = "<img src='/res/close.svg' onclick='hideFRQSubmission()' class='hideFRQSubmission'>";
                div.id = "subEditor_" + submission.submissionId;
                div.dataset.id = "" + submission.submissionId;
                var probName_cnt = document.createElement("div");
                probName_cnt.innerHTML = "<b>Problem</b><h2>" + name_1 + "</h2>";
                probName_cnt.classList.add("half");
                div.appendChild(probName_cnt);
                var teamName_cnt = document.createElement("div");
                teamName_cnt.innerHTML = "<b>Team</b><h2>" + team + "</h2>";
                teamName_cnt.classList.add("half");
                div.appendChild(teamName_cnt);
                var frqIsGraded = document.createElement("div");
                frqIsGraded.classList.add("frqIsGraded");
                var frqIsGradedButton = document.createElement("button");
                frqIsGradedButton.classList.add("chngButton");
                frqIsGradedButton.innerText = "Send Graded";
                frqIsGraded.appendChild(frqIsGradedButton);
                div.appendChild(frqIsGraded);
                var regradeButton = document.createElement("button");
                regradeButton.classList.add("chngButton");
                regradeButton.classList.add("secButton");
                regradeButton.innerText = "Regrade";
                regradeButton.onclick = function () { regradeFRQ(submission.submissionId); };
                div.appendChild(regradeButton);
                var result_cnt = document.createElement("p");
                result_cnt.id = "frqSubmissionEditorResponse";
                // addSuccessBox(result_cnt, "Running...", false);
                result_cnt.classList.add("resultCnt");
                result_cnt.innerHTML = "<b>Judgement:</b>";
                div.appendChild(result_cnt);
                var result_cnt_changeJudgement_1 = document.createElement("select");
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
                frqIsGradedButton.onclick = function () {
                    if (!graded_1) { // In this case, tell the team that they can view the graded submission
                        submission.dom.gradedTD.innerText = "true";
                        submission.dom.gradedTD.classList.remove("notGraded");
                        submission.dom.gradedTD.classList.add("graded");
                        graded_1 = true;
                        ws.send("[\"publishGradedFRQ\"," + submission.submissionId + "]");
                    }
                    var xhr = new XMLHttpRequest();
                    xhr.open('POST', "/console/competitions", true);
                    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
                    xhr.send("cid=" + cid + "&action=changeFRQJudgement&id=" + submission.submissionId + "&judgeId=" + result_cnt_changeJudgement_1.value);
                    submission.dom.resultTD.innerText = result_cnt_changeJudgement_1.options[result_cnt_changeJudgement_1.selectedIndex].text;
                    hideFRQSubmission();
                };
                result_cnt.appendChild(result_cnt_changeJudgement_1);
                addFRQOutputHTML(response["input"], response["output"], response["outputFile"], result, div);
                submission.dom.div = div;
                submission.input = response["input"];
                if (!blocked)
                    add(div);
            }
        }
    };
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=showFRQSubmission&id=" + submission.submissionId);
}
function escapeHtml(html) {
    var text = document.createTextNode(html);
    var p = document.createElement('p');
    p.appendChild(text);
    return p.innerHTML;
}
function addFRQOutputHTML(input, output, outputFile, result, div_wrapper) {
    var div = document.createElement("span");
    div.classList.add("frqUpdateWrapper");
    if (input) {
        var inputOutputTabCnt = document.createElement("div");
        inputOutputTabCnt.id = "inputOutputTabCnt";
        div.appendChild(inputOutputTabCnt);
        var input_cnt = document.createElement("div");
        input_cnt.classList.add("inputCnt");
        div.appendChild(input_cnt);
        var b_input_1 = document.createElement("b");
        b_input_1.innerText = "Input";
        inputOutputTabCnt.appendChild(b_input_1);
        var span_input_1 = document.createElement("span");
        // span_input.innerHTML = input.replace(/\r\n/g, "<br>").replace(/\t/g, "<div class='tab'></div>");
        var codeLines = input.split("<br>");
        for (var i = 0, j = codeLines.length; i < j; i++) {
            var line = codeLines[i];
            var pre = document.createElement("pre");
            pre.innerHTML = line;
            span_input_1.appendChild(pre);
        }
        var viewingInput_1 = false;
        input_cnt.appendChild(span_input_1);
        if (!output) {
            b_input_1.classList.add("selected");
        }
        else {
            span_input_1.style.display = "none";
            var b_output_1 = document.createElement("b");
            b_output_1.innerText = "Output";
            b_output_1.classList.add("selected");
            inputOutputTabCnt.appendChild(b_output_1);
            var span_output_1 = document.createElement("span");
            div.appendChild(span_output_1);
            var outputDiv = document.createElement("div");
            var inputLines = [];
            if (result == "Correct" || result == "Incorrect") {
                var lines_2 = htmldiff(output, outputFile).split("<br>");
                for (var _i = 0, lines_1 = lines_2; _i < lines_1.length; _i++) {
                    var line = lines_1[_i];
                    var pre = document.createElement("pre");
                    pre.innerHTML = line;
                    outputDiv.appendChild(pre);
                    inputLines.push(pre);
                }
            }
            else
                outputDiv.innerHTML = output;
            b_input_1.onclick = function () {
                if (!viewingInput_1) {
                    b_output_1.classList.remove("selected");
                    span_output_1.style.display = "none";
                    span_input_1.style.display = "block";
                    b_input_1.classList.add("selected");
                    viewingInput_1 = !viewingInput_1;
                }
            };
            b_output_1.onclick = function () {
                if (viewingInput_1) {
                    b_output_1.classList.add("selected");
                    span_output_1.style.display = "block";
                    span_input_1.style.display = "none";
                    b_input_1.classList.remove("selected");
                    viewingInput_1 = !viewingInput_1;
                }
            };
            var output_cnt = document.createElement("div");
            output_cnt.classList.add("outputCnt");
            output_cnt.classList.add("frqHalf");
            output_cnt.innerHTML = "<b>Team</b>";
            output_cnt.appendChild(outputDiv);
            span_output_1.appendChild(output_cnt);
            var selectedLine_1 = null; // the line that is selected
            var outputFileDiv = document.createElement("div");
            var lines = outputFile.split("<br>");
            var _loop_5 = function (i, j) {
                var line = lines[i];
                var pre = document.createElement("pre");
                pre.innerHTML = line;
                outputFileDiv.appendChild(pre);
                var inputLine = inputLines[i];
                pre.onclick = function () {
                    var removedSelected = false; // If we are clicking an item we have already clicked
                    if (selectedLine_1) {
                        if (selectedLine_1 == pre || selectedLine_1 == inputLine) {
                            removedSelected = true;
                            pre.classList.remove("selected");
                            if (inputLine)
                                inputLine.classList.remove("selected");
                        }
                        else
                            selectedLine_1.click();
                    }
                    if (removedSelected) {
                        selectedLine_1 = null;
                    }
                    else {
                        pre.classList.add("selected");
                        if (inputLine) {
                            inputLine.classList.add("selected");
                        }
                        selectedLine_1 = pre;
                    }
                };
                if (inputLine) {
                    inputLine.onclick = function () {
                        pre.click();
                    };
                }
            };
            for (var i = 0, j = lines.length; i < j; i++) {
                _loop_5(i, j);
            }
            var _loop_6 = function (i) {
                var inputLine = inputLines[i];
                inputLine.onclick = function () {
                    var removedSelected = false; // If we are clicking an item we have already clicked
                    if (selectedLine_1) {
                        if (selectedLine_1 == inputLine || selectedLine_1 == inputLine) {
                            removedSelected = true;
                            inputLine.classList.remove("selected");
                        }
                        else
                            selectedLine_1.click();
                    }
                    if (removedSelected) {
                        selectedLine_1 = null;
                    }
                    else {
                        inputLine.classList.add("selected");
                        if (inputLine) {
                            inputLine.classList.add("selected");
                        }
                        selectedLine_1 = inputLine;
                    }
                };
            };
            for (var i = lines.length; i < inputLines.length; i++) {
                _loop_6(i);
            }
            var judge_cnt = document.createElement("div");
            judge_cnt.classList.add("frqHalf");
            judge_cnt.innerHTML = "<b>Judge</b>";
            judge_cnt.appendChild(outputFileDiv);
            span_output_1.appendChild(judge_cnt);
        }
    }
    div_wrapper.appendChild(div);
}
// Tell the server to rerun the frq
function regradeFRQ(submissionID) {
    ws.send("[\"regradeFRQ\"," + submissionID + "]");
}
/**
 * Takes in the submission index (submissionId) of the submission on the server. Contacts the server, retrieves the
 * submission information, and displays it.
 * @param submissionId
 */
var mcSubmissionMap = {};
var showingMCSubmission = null;
function showMCSubmission(uid) {
    function add(element) {
        if (!showingMCSubmission) {
            dom.mc.appendChild(element);
        }
        else {
            showingMCSubmission.replaceWith(element);
        }
        showingMCSubmission = element;
    }
    if (mcSubmissionMap[uid] != null) {
        add(mcSubmissionMap[uid]);
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
                var percentageCorrect = 100 * scoringReport[1];
                if (scoringReport[3] + scoringReport[1] != 0)
                    percentageCorrect /= scoringReport[3] + scoringReport[1];
                scoring_cnt.innerHTML = "<b>Score: </b>" + scoringReport[0] + "<br><b>Percentage Correct: </b>" +
                    percentageCorrect.toFixed(2) +
                    "<br><b>Correct: </b>" + scoringReport[1] + "" +
                    "<br><b>Incorrect: </b>" + scoringReport[3] + "<br><b>Skipped: </b>" + scoringReport[2];
                div.appendChild(scoring_cnt);
                var result_cnt = document.createElement("p");
                result_cnt.classList.add("resultCnt");
                result_cnt.innerHTML = "<b>Answer Sheet:</b>";
                div.appendChild(result_cnt);
                var test = response["answers"];
                var submission_cnt = document.createElement("div");
                submission_cnt.classList.add("mcSubmissionCnt_teacher");
                submission_cnt.innerHTML = test;
                div.appendChild(submission_cnt);
                add(div);
                mcSubmissionMap[uid] = div;
            }
        }
    };
    xhr.open('POST', "/console/competitions", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("cid=" + cid + "&action=showMCSubmission&uid=" + uid);
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
function inputMaxLength(element) {
    if (element.value.length > element.maxLength) {
        element.value = element.value.slice(0, element.maxLength);
    }
}
// Send the student name search to the socket
function searchForStudent(input) {
    var data = ["ssearch", input.value];
    ws.send(JSON.stringify(data));
}
function proper(mySentence) {
    var words = mySentence.split(" ");
    for (var i = 0; i < words.length; i++) {
        words[i] = words[i][0].toUpperCase() + words[i].substr(1);
    }
    return words.join(" ");
}
function parseExcel(file) {
    var reader = new FileReader();
    reader.onload = function (e) {
        var data = e.target.result;
        var lines = data.split("\r\n");
        // The first three lines are useless
        lines.shift();
        lines.shift();
        lines.shift();
        var teams = {};
        var numTeams = 0;
        for (var _i = 0, lines_3 = lines; _i < lines_3.length; _i++) {
            var line = lines_3[_i];
            try {
                var cols = line.split("\t");
                var tname = void 0;
                var splitName = cols[2].split("."); // last name, first name
                var type = cols[3] ? cols[3].toLowerCase() : ""; // Either I, A, or empty. If I or A, they are on their own team as an individual. If A, they are marked as an alternate on that team.
                if (type != "i" && type != "a") {
                    tname = proper(cols[1].split(",")[0].replace(" H S", " HS").replace(/["']/g, "").trim());
                    if (!teams[tname]) {
                        numTeams++;
                        var division = cols[0].split(" ")[0]; // Like 6A
                        teams[tname] = { division: division, students: [] };
                    }
                }
                else { // This is an individual team
                    var baseTname = proper(cols[1].split(",")[0].replace(" H S", " HS").replace(/["']/g, "").trim()) + "-Individual";
                    // proper(splitName[1].trim() + " " + splitName[0].trim());
                    tname = baseTname;
                    var i = 1;
                    while (teams[tname]) { // There is another team with the same name, so we add on numbers
                        tname = baseTname + i;
                        i++;
                    }
                    numTeams++;
                    var division = cols[0].split(" ")[0]; // Like 6A
                    teams[tname] = { division: division, students: [] };
                }
                teams[tname].students.push([splitName[1].trim(), splitName[0].trim(), type]);
            }
            catch (e) { }
        }
        var json = ["uploadRoster", teams];
        ws.send(JSON.stringify(json));
        /*let i=0;
        for(let tname in teams) {
            dom.uploadRosterBox.innerText = "Creating team '" + tname + "'";

            createTeamHelper(tname,false, function(team:Team) {
                // Now, add in all of the student temporary accounts
                for(let c=0,j=teams[tname].length;c<j;c++) {
                    let student = teams[tname][c];
                    let data = ["createTempStudent", student[0], student[1], "", team.tid];
                    ws.send(JSON.stringify(data));
                    dom.uploadRosterBox.innerText = "Creating student '" + student[0] + " " + student[1] + "'";

                    console.log("c="+c+",i="+i+",numTeams="+numTeams+",j="+j);
                    if(c==j-1 && i==numTeams) {
                        console.log("-----");
                        dom.uploadRosterBox.innerText = "Uploading Roster...";
                        dom.uploadRosterBox.style.display = "none";
                    }
                }
            });
            i++;
        }*/
    };
    reader.onerror = function (ex) {
        console.log(ex);
    };
    reader.readAsBinaryString(file);
    return reader;
}
// Sends the temporary student data to the server
function createTempStudent() {
    Team.closeSelectStudent();
    var openTeam = pageState.openTeam;
    openTeam.save(function () {
        var data = ["createTempStudent", dom.fnameTemp.value, dom.lnameTemp.value, dom.inputSchool.value, openTeam.tid];
        addSuccessBox(dom.openTeamFeedbackCnt, "Saving team...", false);
        ws.send(JSON.stringify(data));
    });
}
function writeToPDF(doc, text, x, status, maxwidth) {
    if (status.row > (doc.internal.pageSize.height - 40) * status.count) {
        doc.addPage();
        status.row -= (doc.internal.pageSize.height - 40) * status.count;
        status.count++;
    }
    if (!maxwidth)
        maxwidth = doc.internal.pageSize.width - 40;
    doc.text(x, status.row, text, {
        maxWidth: maxwidth,
        align: 'left'
    });
}
function exportCSV(arrayData, delimiter, fileName) {
    var csv = "";
    arrayData.forEach(function (array) {
        csv += array.join(delimiter) + "\n";
    });
    var csvData = new Blob([csv], { type: 'text/csv' });
    var csvUrl = URL.createObjectURL(csvData);
    var hiddenElement = document.createElement('a');
    hiddenElement.href = csvUrl;
    hiddenElement.target = '_blank';
    hiddenElement.download = fileName + '.csv';
    hiddenElement.click();
}
function getPercentageCorrect(correct, incorrect) {
    var percentageCorrect = 100 * correct;
    if (incorrect + correct != 0)
        percentageCorrect /= incorrect + correct;
    return percentageCorrect.toFixed(2);
}
// Creates a pdf of the scoreboard and downloads it
function downloadScoreboard() {
    if (!pageState.isCreator)
        return;
    var data = [];
    for (var division in divisions) {
        data.push([division]);
        data.push(["Name", "Written", "Hands-On", "Total"]);
        for (var _i = 0, _a = divisions[division].teamScoreboard; _i < _a.length; _i++) {
            var team = _a[_i];
            var totalScore = 0;
            var teamData = [team.tname.replace(/[^a-zA-Z0-9 ]/g, '')];
            if (pageState.mcExists) {
                totalScore += team.mcScore;
                teamData.push("" + team.mcScore);
            }
            if (pageState.frqExists) {
                totalScore += team.frqScore;
                teamData.push("" + team.frqScore);
            }
            teamData.push("" + totalScore);
            data.push(teamData);
        }
        data.push([]);
        if (pageState.mcExists) {
            data.push(["Written Scoreboard"]);
            data.push(["Name", "Team", "Correct", "Incorrect", "% Correct", "Total"]);
            for (var _b = 0, _c = divisions[division].writtenScoreboard; _b < _c.length; _b++) {
                var student = _c[_b];
                if (student.type == StudentType.ALTERNATE)
                    continue;
                var mcCorrect = "";
                var mcIncorrect = "";
                var mcPercentCorrect = "";
                var mcScoreString = "Not Taken";
                if (student.mcScore != null) {
                    mcCorrect = "" + student.mcNumCorrect;
                    mcIncorrect = "" + student.mcNumIncorrect;
                    mcPercentCorrect = "" + getPercentageCorrect(student.mcNumCorrect, student.mcNumIncorrect);
                    mcScoreString = "" + student.mcScore;
                }
                var studentData = [student.name.replace(/[^a-zA-Z0-9 ]/g, ''),
                    student.team.tname.replace(/[^a-zA-Z0-9 ]/g, ''), mcCorrect, mcIncorrect, mcPercentCorrect, mcScoreString];
                data.push(studentData);
            }
        }
        data.push([]);
        if (pageState.frqExists) {
            data.push(["Hands-On Scoreboard"]);
            var frqHeader = ["", "Total"];
            for (var _d = 0, _e = pageState.frqProblemMap; _d < _e.length; _d++) {
                var problem = _e[_d];
                frqHeader.push(problem);
            }
            data.push(frqHeader);
            for (var _f = 0, _g = divisions[division].handsOnScoreboard; _f < _g.length; _f++) {
                var team = _g[_f];
                var frqTeamData = [team.tname.replace(/[^a-zA-Z0-9 ]/g, ''), "" + team.frqScore];
                for (var _h = 0, _j = team.frqResponses; _h < _j.length; _h++) {
                    var frqResponse = _j[_h];
                    if (frqResponse > 0) {
                        frqTeamData.push("" + (Math.abs(pageState.frqMaxPoints) - Math.abs(frqResponse - 1) * Math.abs(pageState.frqIncorrectPenalty)));
                    }
                    else if (frqResponse == 0) {
                        frqTeamData.push("0");
                    }
                    else {
                        var tries = Math.abs(frqResponse);
                        frqTeamData.push("" + tries);
                    }
                }
                data.push(frqTeamData);
            }
        }
    }
    exportCSV(data, ",", "scoreboard");
}
// Creates a pdf of the roster and downloads it
function downloadRoster() {
    if (!pageState.isCreator)
        return;
    var data = [["Team", "Name", "Username", "Password"]];
    for (var tid in teams) {
        var team = teams[tid];
        for (var _i = 0, _a = team.students; _i < _a.length; _i++) {
            var student = _a[_i];
            if (student.type == StudentType.ALTERNATE)
                continue;
            var studentData = [];
            studentData.push(team.tname.replace(/[^a-zA-Z0-9 ]/g, ''));
            studentData.push(student.name.replace(/[^a-zA-Z0-9 ]/g, ''));
            if (student.temp) {
                studentData.push(student.uname.replace(/[^a-zA-Z0-9 ]/g, ''));
                studentData.push(student.password);
            }
            data.push(studentData);
        }
    }
    exportCSV(data, ",", "roster");
}
function showGeneralScoreboard() {
    dom.teamListCnt.classList.add("showGeneral");
    dom.teamListCnt.classList.remove("showWritten");
    dom.teamListCnt.classList.remove("showHandsOn");
}
function showWrittenScoreboard() {
    dom.teamListCnt.classList.add("showWritten");
    dom.teamListCnt.classList.remove("showGeneral");
    dom.teamListCnt.classList.remove("showHandsOn");
}
function showHandsOnScoreboard() {
    dom.teamListCnt.classList.add("showHandsOn");
    dom.teamListCnt.classList.remove("showGeneral");
    dom.teamListCnt.classList.remove("showWritten");
}
/*
The start and stop controls for the written and hands-on.
 */
function getNowString() {
    var d = new Date();
    // @ts-ignore
    return ("" + (d.getMonth() + 1)).padStart(2) + "/" + ("" + d.getDate()).padStart(2) + "/" + d.getFullYear() + " " + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
}
function stopWritten() {
    ws.send("[\"stopWritten\"]");
}
function startWritten() {
    ws.send("[\"startWritten\",\"" + getNowString() + "\"]");
}
function stopHandsOn() {
    ws.send("[\"stopHandsOn\"]");
}
function startHandsOn() {
    ws.send("[\"startHandsOn\",\"" + getNowString() + "\"]");
}
function stopDryRun() {
    ws.send("[\"stopDryRun\",\"" + getNowString() + "\"]");
}
function startDryRun() {
    ws.send("[\"startDryRun\",\"" + getNowString() + "\"]");
}
function releaseMCScores(element) {
    element.innerText = "Hide Scores";
    element.onclick = function () {
        hideMCScore(element);
    };
    ws.send("[\"releaseMCScores\"]");
}
function hideMCScore(element) {
    element.innerText = "Release Scores";
    element.onclick = function () {
        releaseMCScores(element);
    };
    ws.send("[\"hideMCScores\"]");
}
function getTime(timestamp) {
    var date = new Date(timestamp);
    return "" + (date.getMonth() + 1) + "/" + ("" + date.getDate()).padStart(2, '0') + " " + date.getHours() + ":" + ("" + date.getMinutes()).padStart(2, '0');
}
// Reads in the roster file and creates teams accordingly
function uploadRoster() {
    dom.uploadRosterBox.style.display = "block";
    parseExcel(dom.uploadRosterProxy.files[0]);
}
// Clicks the upload roster file input
function uploadRosterProxy() {
    dom.uploadRosterProxy.click();
}
// Deletes all submissions
function resetSubmissions() {
    ws.send("[\"resetSubmissions\"]");
    closeResetSubmissions();
}
function showResetSubmissions() {
    dom.resetConfirmationCnt.style.display = "block";
}
function closeResetSubmissions() {
    dom.resetConfirmationCnt.style.display = "none";
}
function showDeleteTeams() {
    dom.deleteTeamsConfirmationCnt.style.display = "block";
}
function closeDeleteTeams() {
    dom.deleteTeamsConfirmationCnt.style.display = "none";
}
function deleteTeams() {
    ws.send("[\"deleteAllTeams\"]");
    closeDeleteTeams();
}
function toggleShowAlternates() {
    if (pageState.showAlternates) {
        document.body.classList.remove("showAlternates");
        pageState.showAlternates = false;
        var team = pageState.openTeam;
        if (team && team.individual) { // Check if this student is alternate
            var student = team.students[0];
            if (student) {
                if (student.type == StudentType.ALTERNATE)
                    Team.toggleTeam(null, false);
            }
        }
    }
    else {
        document.body.classList.add("showAlternates");
        pageState.showAlternates = true;
    }
}
function downloadWrittenStatistics() {
    ws.send("[\"fetchWrittenStatistics\"]");
}
