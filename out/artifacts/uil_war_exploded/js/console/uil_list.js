var config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        class_competitions: "class_competitions"
    },
    COMPETITION: {
        mcOptions: ["a", "b", "c", "d", "e"]
    },
    SOCKET_FUNCTIONS: {}
};
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
    get class_competitions() { return this.getHelper(config.IDs.class_competitions); }
};
var ws;
(function () {
    ws = new WebSocket("wss://" + window.location.host + "/profilesocket");
    ws.onmessage = function (evt) {
        try {
            var msg = JSON.parse(evt.data);
            config.SOCKET_FUNCTIONS[msg.action](msg);
        }
        catch (e) { }
    };
})();
function showPublic(nav) {
    var nodes = document.getElementById("nav").getElementsByTagName("p");
    for (var i = 0; i < nodes.length; i++) {
        nodes[i].classList.remove("selected");
    }
    nav.classList.add("selected");
    document.getElementById("public_competitions").style.display = "block";
    try {
        document.getElementById("upcoming_competitions").style.display = "none";
        document.getElementById("class_competitions").style.display = "none";
    }
    catch (e) { }
}
function showClassComps(nav) {
    var nodes = document.getElementById("nav").getElementsByTagName("p");
    for (var i = 0; i < nodes.length; i++) {
        nodes[i].classList.remove("selected");
    }
    nav.classList.add("selected");
    document.getElementById("public_competitions").style.display = "none";
    try {
        document.getElementById("class_competitions").style.display = "block";
        document.getElementById("upcoming_competitions").style.display = "none";
    }
    catch (e) { }
}
function showUpcomingComps(nav) {
    var nodes = document.getElementById("nav").getElementsByTagName("p");
    for (var i = 0; i < nodes.length; i++) {
        nodes[i].classList.remove("selected");
    }
    nav.classList.add("selected");
    document.getElementById("public_competitions").style.display = "none";
    try {
        document.getElementById("class_competitions").style.display = "none";
        document.getElementById("upcoming_competitions").style.display = "block";
    }
    catch (e) { }
}
var WrittenType;
(function (WrittenType) {
    WrittenType[WrittenType["MC"] = 0] = "MC";
    WrittenType[WrittenType["SAQ"] = 1] = "SAQ";
})(WrittenType || (WrittenType = {}));
var WrittenProblem = /** @class */ (function () {
    function WrittenProblem(answer, type) {
        this.answer = answer;
        this.type = type;
    }
    return WrittenProblem;
}());
var HandsOnProblem = /** @class */ (function () {
    function HandsOnProblem() {
        this.dom = {
            name: null,
            input: null,
            output: null
        };
        /* If this problem has been saved to the server, oldIndex stores the index of this problem in the server.
        *  If it has not been saved, it is set to -1, and when it is saved it will be overwritten to be the index
        *  at the time of saving */
        this.oldIndex = -1;
    }
    return HandsOnProblem;
}());
var competitions = [];
var Competition = /** @class */ (function () {
    function Competition(cid, published, isPublic, name, writtenObj, handsOnObj) {
        this.written = {
            opens: "",
            key: [],
            correctPoints: 0,
            incorrectPoints: 0,
            instructions: "",
            testLink: "",
            answersLink: "",
            time: 0
        };
        this.handsOn = {
            opens: "",
            maxPoints: 0,
            incorrectPenalty: 0,
            problemMap: [],
            studentPacketLink: "",
            judgePacketLink: "",
            time: 0
        };
        this.dom = {
            form: null,
            comp_head: null,
            comp_head_name: null,
            comp_edit: null,
            compName: null,
            compPublic: null,
            viewCompetition: null,
            controls: null,
            controlsEdit: null,
            controlsOpen: null,
            controlsSave: null,
            controlsDelete: null,
            description: null,
            rules: null,
            writtenSection: null,
            writtenOpen: null,
            writtenTime: null,
            writtenKey: null,
            writtenAnswerList: null,
            writtenInstructionCnt: null,
            writtenInstructions: null,
            writtenTestLink: null,
            writtenPointsPerCorrect: null,
            writtenPointsPerIncorrect: null,
            handsOnSection: null,
            handsOnStart: null,
            handsOnTime: null,
            handsOnProblems: null,
            handsOnStudentPacket: null,
            handsOnMaxPoints: null,
            handsOnIncorrectPenalty: null
        };
        competitions.push(this);
        this.cid = cid;
        this.published = published;
        this.isPublic = isPublic;
        this.name = name;
        this.writtenExists = !!writtenObj;
        this.handsOnExists = !!handsOnObj;
        if (this.writtenExists) { /* written exists */
            this.written.opens = writtenObj.opens;
            /* Add in key */
            var answer = void 0; /* First element is the answer, second is the type. type=0 means MC, type=1 means SAQ */
            for (var _i = 0, _a = writtenObj.answers; _i < _a.length; _i++) {
                answer = _a[_i];
                var type = WrittenType.MC;
                if (parseInt(answer[1]) === 1)
                    type = WrittenType.SAQ;
                this.written.key.push(new WrittenProblem(answer[0], type));
            }
            this.written.correctPoints = writtenObj.correctPoints;
            this.written.incorrectPoints = writtenObj.incorrectPoints;
            this.written.instructions = writtenObj.instructions;
            this.written.testLink = writtenObj.testLink;
            this.written.answersLink = writtenObj.answersLink;
            this.written.time = writtenObj.time;
        }
        if (this.handsOnExists) { /* HandsOn exists */
            this.handsOn.opens = handsOnObj.opens;
            this.handsOn.maxPoints = handsOnObj.maxPoints;
            this.handsOn.incorrectPenalty = handsOnObj.incorrectPenalty;
            this.handsOn.studentPacketLink = handsOnObj.studentPacketLink;
            this.handsOn.judgePacketLink = handsOnObj.judgePacketLink;
            this.handsOn.time = handsOnObj.time;
        }
    }
    Competition.prototype.toggleWrittenKey = function (h2) {
        if (this.dom.writtenKey.style.display !== "block") {
            this.dom.writtenKey.style.display = "block";
            h2.innerHTML = "Answers<span style='float:right;font-weight:bold;'>-</span>";
        }
        else {
            this.dom.writtenKey.style.display = "none";
            h2.innerHTML = "Answers<span style='float:right;font-weight:bold;'>+</span>";
        }
    };
    Competition.prototype.toggleHandsOnProblems = function (h2) {
        if (this.dom.handsOnProblems.style.display !== "block") {
            this.dom.handsOnProblems.style.display = "block";
            h2.innerHTML = "Problems<span style='float:right;font-weight:bold;'>-</span>";
        }
        else {
            this.dom.handsOnProblems.style.display = "none";
            h2.innerHTML = "Problems<span style='float:right;font-weight:bold;'>+</span>";
        }
    };
    Competition.prototype.toggleWrittenTest = function () {
        if (this.writtenExists) { /* It is now unchecked so show the div */
            this.dom.writtenSection.style.display = "none";
        }
        else {
            this.dom.writtenSection.style.display = "grid";
        }
        this.writtenExists = !this.writtenExists;
    };
    Competition.prototype.toggleHandsOnTest = function () {
        if (this.handsOnExists) { /* It is now unchecked so show the div */
            this.dom.handsOnSection.style.display = "none";
        }
        else {
            this.dom.handsOnSection.style.display = "grid";
        }
        this.handsOnExists = !this.handsOnExists;
    };
    Competition.prototype.toggleWrittenInstructions = function (h2) {
        if (this.dom.writtenInstructionCnt.style.display === "none") {
            this.dom.writtenInstructionCnt.style.display = "block";
            h2.innerHTML = "Instructions<span style='float:right;font-weight:bold'>-</span>";
        }
        else {
            this.dom.writtenInstructionCnt.style.display = "none";
            this.written.instructions = this.dom.writtenInstructions.value;
            h2.innerHTML = "Instructions<span style='float:right;font-weight:bold'>+</span>";
        }
    };
    Competition.prototype.delete = function () {
        if (this.cid.length !== 0) { /* This competition has already been saved */
            var xhr = new XMLHttpRequest();
            xhr.open('POST', "/console/profile", true);
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            xhr.send("action=deleteCompetition&cid=" + this.cid);
        }
        dom.class_competitions.removeChild(this.dom.form);
        competitions.splice(competitions.indexOf(this), 1);
        if (dom.class_competitions.childNodes.length <= 0) {
            dom.class_competitions.dataset.hasCompetitions = "false";
            dom.class_competitions.innerHTML = "Click 'New' to create a competition.";
        }
    };
    Competition.prototype.getFormData = function () {
        /* First, set the name and open date in the title */
        this.dom.comp_head_name.innerHTML = this.dom.compName.value;
        var thisComp = this; // So that it can be referenced in other functions
        /*let deleteCompetition = document.createElement("p");
        deleteCompetition.innerText = "Delete";
        deleteCompetition.classList.add("deleteCompetition");
        deleteCompetition.onclick = function() {
            thisComp.delete();
        };
        this.dom.comp_head.appendChild(deleteCompetition);*/
        var formData = new FormData();
        formData.append("cid", this.cid);
        formData.append("name", this.dom.compName.value);
        formData.append("isPublic", "" + this.dom.compPublic.checked);
        formData.append("description", this.dom.description.value);
        formData.append("writtenExists", "" + this.writtenExists);
        if (this.writtenExists) {
            formData.append("mcOpens", this.dom.writtenOpen.value);
            formData.append("mcTime", "" + this.dom.writtenTime.value);
            formData.append("mcCorrectPoints", "" + this.dom.writtenPointsPerCorrect.value);
            formData.append("mcIncorrectPoints", "" + this.dom.writtenPointsPerIncorrect.value);
            formData.append("mcInstructions", this.dom.writtenInstructions.value);
            formData.append("mcTestLink", this.dom.writtenTestLink.value);
            formData.append("mcAnswersLink", ""); // TODO: Add this
            var answers = [];
            for (var _i = 0, _a = this.written.key; _i < _a.length; _i++) {
                var answer = _a[_i];
                answers.push([answer.answer, "" + answer.type]);
            }
            formData.append("mcAnswers", JSON.stringify(answers));
        }
        formData.append("handsOnExists", "" + this.handsOnExists);
        if (this.handsOnExists) {
            formData.append("frqOpens", this.dom.handsOnStart.value);
            formData.append("frqTime", "" + this.dom.handsOnTime.value);
            formData.append("frqMaxPoints", "" + this.dom.handsOnMaxPoints.value);
            formData.append("frqIncorrectPenalty", "" + this.dom.handsOnIncorrectPenalty.value);
            formData.append("frqStudentPacket", this.dom.handsOnStudentPacket.value);
            formData.append("frqJudgePacket", ""); // TODO: Add this
            var problems = [];
            var problemIndices = []; /* A list like [1, 2, -1, 9, 5], corresponding to a name in the problems array */
            for (var i = 0, j = this.handsOn.problemMap.length; i < j; i++) {
                var problem = this.handsOn.problemMap[i];
                var probName = problem.dom.name.value;
                problems.push(probName);
                problemIndices.push(problem.oldIndex);
                problem.oldIndex = i + 1; /* Now that the problem has been saved, we set it to be its current index */
                if (problem.dom.input.files.length > 0) {
                    formData.append("fi:" + i, problem.dom.input.files[0]);
                }
                if (problem.dom.output.files.length > 0) {
                    formData.append("fo:" + i, problem.dom.output.files[0]);
                }
            }
            formData.append("frqProblemMap", JSON.stringify(problems));
            formData.append("frqIndices", JSON.stringify(problemIndices));
        }
        return formData;
    };
    Competition.prototype.saveCompetition = function () {
        // Remove the error box
        try {
            this.dom.comp_edit.removeChild(document.getElementById("ERROR"));
        }
        catch (e) { }
        var formData = this.getFormData();
        var thisComp = this;
        formData.append("action", "saveCompetition");
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    var response = JSON.parse(xhr.responseText);
                    if (response["success"] != null) {
                        for (var _i = 0, _a = thisComp.handsOn.problemMap; _i < _a.length; _i++) {
                            var problem = _a[_i];
                            problem.dom.input.value = "";
                            problem.dom.output.value = "";
                        }
                        addSuccessBox(thisComp.dom.comp_edit, response["success"]);
                        if (thisComp.cid.length === 0 && response["cid"] != null)
                            thisComp.cid = response["cid"];
                        thisComp.dom.viewCompetition.onclick = function () { window.location.href = "/console/competitions?cid=" + thisComp.cid; };
                    }
                    else if (response["error"] != null) { // An error occurred
                        addErrorBox(thisComp.dom.comp_edit, response["error"]);
                    }
                    else {
                        addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                    }
                }
                else {
                    addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                }
            }
        };
        xhr.open('POST', "/console/profile", true);
        xhr.send(formData);
        return false;
    };
    Competition.prototype.unPublishCompetition = function (callback) {
        try {
            this.dom.comp_edit.removeChild(document.getElementById("ERROR"));
        }
        catch (e) { }
        this.published = false;
        var thisComp = this;
        var formData = new FormData();
        formData.append("action", "unPublishCompetition");
        formData.append("cid", this.cid);
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    var response = JSON.parse(xhr.responseText);
                    if (response["success"] != null) {
                        addSuccessBox(thisComp.dom.comp_edit, response["success"]);
                        callback();
                    }
                    else {
                        addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                    }
                }
            }
        };
        xhr.open('POST', "/console/profile", true);
        xhr.send(formData);
        return false;
    };
    Competition.prototype.publishCompetition = function (callback) {
        // Remove the error box
        try {
            this.dom.comp_edit.removeChild(document.getElementById("ERROR"));
        }
        catch (e) { }
        var formData = this.getFormData();
        var thisComp = this;
        formData.append("action", "publishCompetition");
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    var response = JSON.parse(xhr.responseText);
                    if (response["success"] != null) {
                        for (var _i = 0, _a = thisComp.handsOn.problemMap; _i < _a.length; _i++) {
                            var problem = _a[_i];
                            problem.dom.input.value = "";
                            problem.dom.output.value = "";
                        }
                        addSuccessBox(thisComp.dom.comp_edit, response["success"]);
                        if (thisComp.cid.length === 0 && response["cid"] != null)
                            thisComp.cid = response["cid"];
                        thisComp.dom.comp_head.onclick = function (event) { event.stopPropagation(); window.location.href = "/console/competitions?cid=" + thisComp.cid; };
                        thisComp.dom.controls.insertBefore(thisComp.getOpenCompetition(), thisComp.dom.controlsSave);
                        callback();
                    }
                    else if (response["error"] != null) { // An error occurred
                        addErrorBox(thisComp.dom.comp_edit, response["error"]);
                    }
                    else {
                        addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                    }
                }
                else {
                    addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                }
            }
        };
        xhr.open('POST', "/console/profile", true);
        xhr.send(formData);
    };
    Competition.prototype.addWrittenQuestion = function (writtenProblem) {
        if (writtenProblem == null) { // This question doesn't exist
            writtenProblem = new WrittenProblem("a", WrittenType.MC);
            this.written.key.push(writtenProblem);
        }
        var thisComp = this;
        var newLi = document.createElement("li");
        var newP;
        var newInput = document.createElement("input");
        newInput.type = "text";
        newInput.value = writtenProblem.answer;
        newInput.onchange = function () {
            var newValue = newInput.value;
            writtenProblem.answer = newValue;
            if (newValue.length > 0 && !config.COMPETITION.mcOptions.includes(newValue)) { /* Switch to SAQ */
                writtenProblem.type = WrittenType.SAQ;
                newP.selectedIndex = 1;
            }
        };
        newLi.appendChild(newInput);
        newP = document.createElement("select");
        var mcSelected = "";
        var saqSelected = "selected";
        if (writtenProblem.type === WrittenType.MC) {
            mcSelected = "selected";
            saqSelected = "";
        }
        newP.innerHTML = "<option " + mcSelected + ">MC</option><option " + saqSelected + ">SAQ</option>";
        newP.onchange = function () {
            if (writtenProblem.type === WrittenType.MC) { /* Switch to SAQ */
                writtenProblem.type = WrittenType.SAQ;
            }
            else if (newInput.value.length === 0 || config.COMPETITION.mcOptions.includes(newInput.value)) { /* It must be a valid multiple choice option */
                writtenProblem.type = WrittenType.MC;
            }
            else {
                newP.selectedIndex = 1;
            }
        };
        newP.classList.add('writtenAnswerType');
        newLi.appendChild(newP);
        var delQuestion = document.createElement("img");
        delQuestion.src = "/res/close.svg";
        delQuestion.classList.add("deleteProblem");
        delQuestion.onclick = function () {
            thisComp.written.key.splice(thisComp.written.key.indexOf(writtenProblem), 1);
            thisComp.dom.writtenAnswerList.removeChild(newLi);
        };
        newLi.appendChild(delQuestion);
        this.dom.writtenAnswerList.appendChild(newLi);
        newLi.focus();
    };
    Competition.prototype.getOpenCompetition = function () {
        var thisComp = this;
        var controls_open = document.createElement("img");
        controls_open.src = "/res/console/open.svg";
        controls_open.onclick = function () {
            if (thisComp.published)
                window.location.href = "/console/competitions?cid=" + thisComp.cid;
        };
        this.dom.controlsOpen = controls_open;
        return controls_open;
    };
    Competition.prototype.getDOM = function (handsOnProblemNames, whatItIsText, rulesText) {
        function makeHalf(element) {
            element.classList.add("profile_cmpnt");
            element.classList.add("half");
        }
        function makeFull(element) {
            element.classList.add("profile_cmpnt");
            element.classList.add("full");
        }
        function getWrittenSection() {
            var written_section = document.createElement("span");
            written_section.classList.add("writtenSection");
            thisComp.dom.writtenSection = written_section;
            if (!thisComp.writtenExists)
                written_section.style.display = "none";
            thisComp.dom.writtenSection = written_section;
            /* OPEN */
            var written_open = document.createElement("div");
            makeHalf(written_open);
            written_section.appendChild(written_open);
            var h2_written_open = document.createElement("h3");
            h2_written_open.innerText = "Start";
            written_open.appendChild(h2_written_open);
            var input_written_open = document.createElement("input");
            input_written_open.type = "datetime-local";
            // input_written_open.value = (thisComp.written.opens?thisComp.written.opens:"");
            input_written_open.name = "mcOpens";
            input_written_open.classList.add("start");
            written_open.appendChild(input_written_open);
            // thisComp.dom.writtenOpen = input_written_open;
            var curDate = new Date();
            var defaultDate = curDate;
            if (thisComp.written.opens)
                defaultDate = new Date(thisComp.written.opens);
            var minDate = curDate;
            if (defaultDate < curDate)
                minDate = defaultDate;
            thisComp.dom.writtenOpen = flatpickr(input_written_open, {
                altInput: true,
                enableTime: true,
                altFormat: "F j, Y h:i K",
                dateFormat: "m/d/Y H:i:S",
                defaultDate: defaultDate,
                minDate: minDate,
                maxDate: new Date(curDate.getFullYear() + 10, curDate.getMonth(), curDate.getDay())
            }).input;
            /* CLOSE */
            /* OPEN */
            var written_length = document.createElement("div");
            makeHalf(written_length);
            written_section.appendChild(written_length);
            var h2_written_length = document.createElement("h3");
            h2_written_length.innerText = "Length";
            written_length.appendChild(h2_written_length);
            var input_written_length = document.createElement("input");
            input_written_length.name = "mcTime";
            input_written_length.type = "text";
            input_written_length.value = ("" + thisComp.written.time ? "" + thisComp.written.time : "45");
            input_written_length.classList.add("length");
            written_length.appendChild(input_written_length);
            thisComp.dom.writtenTime = input_written_length;
            var span_written_length = document.createElement("span");
            span_written_length.classList.add("unit");
            span_written_length.innerText = "min";
            written_length.appendChild(span_written_length);
            /* CLOSE */
            /* OPEN */
            var written_answers = document.createElement("div");
            makeFull(written_answers);
            written_answers.onclick = function () { thisComp.toggleWrittenKey(h2_written_answers); };
            written_section.appendChild(written_answers);
            written_answers.style.cursor = "pointer";
            var h2_written_answers = document.createElement("h3");
            h2_written_answers.innerHTML = "Answers<span style='float:right;font-weight:bold;'>+</span>";
            written_answers.appendChild(h2_written_answers);
            /* CLOSE */
            /* OPEN */
            var written_key = document.createElement("div");
            makeFull(written_key);
            written_key.classList.add("competitionSubgroup");
            written_key.classList.add("changeWrittenKey");
            written_key.style.display = "none";
            written_key.innerHTML = "<p>Key</p>";
            written_section.appendChild(written_key);
            thisComp.dom.writtenKey = written_key;
            /*let save_written_key = document.createElement("p");
            save_written_key.classList.add("saveWrittenAnswers");
            save_written_key.classList.add("save");
            save_written_key.onclick = function(){thisComp.toggleWrittenKey();};
            save_written_key.innerText = "Save";
            written_key.appendChild(save_written_key);*/
            var list_written_key = document.createElement("ol");
            list_written_key.classList.add("writtenSectionAnswerList");
            written_key.appendChild(list_written_key);
            thisComp.dom.writtenAnswerList = list_written_key;
            if (thisComp.writtenExists) {
                for (var i = 0, j = thisComp.written.key.length; i < j; i++) {
                    thisComp.addWrittenQuestion(thisComp.written.key[i]);
                }
            }
            var add_written_key = document.createElement("button");
            add_written_key.classList.add("addWrittenQuestion");
            add_written_key.onclick = function () { thisComp.addWrittenQuestion(null); };
            add_written_key.innerText = "Add";
            written_key.appendChild(add_written_key);
            /* CLOSE */
            /* OPEN */
            var written_instructions = document.createElement("div");
            makeFull(written_instructions);
            written_instructions.onclick = function () { thisComp.toggleWrittenInstructions(h2_written_instructions); };
            written_section.appendChild(written_instructions);
            written_instructions.style.cursor = "pointer";
            var h2_written_instructions = document.createElement("h3");
            h2_written_instructions.innerHTML = "Instructions<span style='float:right;font-weight:bold'>+</span>";
            written_instructions.appendChild(h2_written_instructions);
            /* CLOSE */
            /* OPEN */
            var written_instructions_change = document.createElement("div");
            makeFull(written_instructions_change);
            written_instructions_change.classList.add("competitionSubgroup");
            written_instructions_change.classList.add("changeWrittenInstructions");
            written_instructions_change.style.display = "none";
            written_instructions_change.innerHTML = "<p>Instructions</p>";
            written_section.appendChild(written_instructions_change);
            thisComp.dom.writtenInstructionCnt = written_instructions_change;
            /*let save_written_instructions_change = document.createElement("p");
            save_written_instructions_change.classList.add("saveWrittenInstructions");
            save_written_instructions_change.classList.add("save");
            save_written_instructions_change.onclick = function(){thisComp.toggleWrittenInstructions();};
            save_written_instructions_change.innerText = "Save";
            written_instructions_change.appendChild(save_written_instructions_change); */
            var textarea_written_instructions_change = document.createElement("textarea");
            textarea_written_instructions_change.classList.add("writtenSectionInstructions");
            textarea_written_instructions_change.maxLength = 144;
            written_instructions_change.appendChild(textarea_written_instructions_change);
            textarea_written_instructions_change.value = thisComp.written.instructions;
            thisComp.dom.writtenInstructions = textarea_written_instructions_change;
            /* CLOSE */
            /* OPEN */
            var written_testlink = document.createElement("div");
            makeFull(written_testlink);
            written_section.appendChild(written_testlink);
            var h2_written_testlink = document.createElement("h3");
            h2_written_testlink.innerText = "Test Link";
            written_testlink.appendChild(h2_written_testlink);
            var input_written_testLink = document.createElement("input");
            input_written_testLink.name = "mcTestLink";
            input_written_testLink.type = "url";
            input_written_testLink.placeholder = "https://what_the_student_sees.com";
            input_written_testLink.value = thisComp.written.testLink ? thisComp.written.testLink : "";
            written_testlink.appendChild(input_written_testLink);
            thisComp.dom.writtenTestLink = input_written_testLink;
            /* CLOSE */
            /* OPEN */
            var written_ppc = document.createElement("div");
            makeHalf(written_ppc);
            written_section.appendChild(written_ppc);
            var h2_written_ppc = document.createElement("h3");
            h2_written_ppc.innerText = "Points Per Correct Problem";
            written_ppc.appendChild(h2_written_ppc);
            var input_written_ppc = document.createElement("input");
            input_written_ppc.name = "mcCorrectPoints";
            input_written_ppc.type = "number";
            input_written_ppc.placeholder = "6";
            input_written_ppc.min = "0";
            input_written_ppc.max = "999";
            input_written_ppc.step = "1";
            var input_written_ppc_old = "6";
            input_written_ppc.onchange = function () {
                if (!input_written_ppc.validity.valid)
                    input_written_ppc.value = input_written_ppc_old;
                else
                    input_written_ppc_old = input_written_ppc.value;
            };
            input_written_ppc.value = "" + thisComp.written.correctPoints ? "" + thisComp.written.correctPoints : "";
            written_ppc.appendChild(input_written_ppc);
            thisComp.dom.writtenPointsPerCorrect = input_written_ppc;
            /* CLOSE */
            /* OPEN */
            var written_ppi = document.createElement("div");
            makeHalf(written_ppi);
            written_section.appendChild(written_ppi);
            var h2_written_ppi = document.createElement("h3");
            h2_written_ppi.innerText = "Points Per Incorrect Problem";
            written_ppi.appendChild(h2_written_ppi);
            var input_written_ppi = document.createElement("input");
            input_written_ppi.name = "mcIncorrectPoints";
            input_written_ppi.type = "number";
            input_written_ppi.placeholder = "-2";
            input_written_ppi.max = "0";
            input_written_ppi.min = "-999";
            input_written_ppi.step = "1";
            var input_written_ppi_old = "-2";
            input_written_ppi.onchange = function () {
                if (!input_written_ppi.validity.valid)
                    input_written_ppi.value = input_written_ppi_old;
                else
                    input_written_ppi_old = input_written_ppi.value;
            };
            input_written_ppi.value = "" + thisComp.written.incorrectPoints ? "" + thisComp.written.incorrectPoints : "";
            written_ppi.appendChild(input_written_ppi);
            thisComp.dom.writtenPointsPerIncorrect = input_written_ppi;
            /* CLOSE */
            return written_section;
        }
        function getHandsOnSection() {
            function addProblem(probIndex) {
                var index = thisComp.handsOn.problemMap.length;
                var problem = new HandsOnProblem();
                problem.oldIndex = probIndex;
                thisComp.handsOn.problemMap.push(problem);
                var li = document.createElement("li");
                li.dataset.probNum = "" + index;
                var input_name = document.createElement("input");
                input_name.type = "text";
                if (handsOnProblemNames.length > index)
                    input_name.value = handsOnProblemNames[index];
                else
                    input_name.value = "";
                input_name.classList.add("handsOn_probName");
                li.appendChild(input_name);
                problem.dom.name = input_name;
                /* Each input has the actual input field that takes in the file but is hidden to the user, as well
                *  as an alias button that serves as the proxy between the user and the input field. This is to prevent
                *  weird file input styling. */
                var input_in_hidden = document.createElement("input");
                input_in_hidden.type = "file";
                input_in_hidden.style.display = "none";
                li.appendChild(input_in_hidden);
                problem.dom.input = input_in_hidden;
                var input_in_proxy = document.createElement("button");
                input_in_proxy.classList.add("handsOn_probIn");
                input_in_proxy.innerText = "Input";
                li.appendChild(input_in_proxy);
                input_in_proxy.onclick = function () { input_in_hidden.click(); };
                var input_out_hidden = document.createElement("input");
                input_out_hidden.type = "file";
                input_out_hidden.style.display = "none";
                li.appendChild(input_out_hidden);
                problem.dom.output = input_out_hidden;
                var input_out_proxy = document.createElement("button");
                input_out_proxy.classList.add("handsOn_probOut");
                input_out_proxy.innerText = "Output";
                input_out_proxy.onclick = function () { input_out_hidden.click(); };
                li.appendChild(input_out_proxy);
                var delQuestion = document.createElement("img");
                delQuestion.src = "/res/close.svg";
                delQuestion.classList.add("deleteProblem");
                delQuestion.onclick = function () {
                    thisComp.handsOn.problemMap.splice(thisComp.handsOn.problemMap.indexOf(problem), 1);
                    list_handsOn_changeproblems.removeChild(li);
                };
                li.appendChild(delQuestion);
                list_handsOn_changeproblems.appendChild(li);
            }
            var handsOn_section = document.createElement("span");
            handsOn_section.classList.add("handsOnSection");
            if (!thisComp.handsOnExists)
                handsOn_section.style.display = "none";
            written_toggle.appendChild(handsOn_section);
            thisComp.dom.handsOnSection = handsOn_section;
            /* OPEN */
            var handsOn_start = document.createElement("div");
            makeHalf(handsOn_start);
            handsOn_start.innerHTML = "<h3>Start</h3>";
            handsOn_section.appendChild(handsOn_start);
            var input_handsOn_start = document.createElement("input");
            input_handsOn_start.name = "frqOpens";
            input_handsOn_start.type = "datetime-local";
            input_handsOn_start.classList.add("start");
            handsOn_start.appendChild(input_handsOn_start);
            // thisComp.dom.handsOnStart = input_handsOn_start;
            var curDate = new Date();
            var defaultDate = curDate;
            if (thisComp.handsOn.opens)
                defaultDate = new Date(thisComp.handsOn.opens);
            var minDate = curDate;
            if (defaultDate < curDate)
                minDate = defaultDate;
            thisComp.dom.handsOnStart = flatpickr(input_handsOn_start, {
                altInput: true,
                enableTime: true,
                altFormat: "F j, Y h:i K",
                dateFormat: "m/d/Y H:i:S",
                defaultDate: defaultDate,
                minDate: minDate,
                maxDate: new Date(curDate.getFullYear() + 10, curDate.getMonth(), curDate.getDay())
            }).input;
            /* CLOSE */
            /* OPEN */
            var handsOn_length = document.createElement("div");
            makeHalf(handsOn_length);
            handsOn_length.innerHTML = "<h3>Length</h3>";
            handsOn_section.appendChild(handsOn_length);
            var input_handsOn_length = document.createElement("input");
            input_handsOn_length.name = "frqTime";
            input_handsOn_length.type = "number";
            input_handsOn_length.classList.add("length");
            input_handsOn_length.value = "" + thisComp.handsOn.time ? "" + thisComp.handsOn.time : "120";
            handsOn_length.appendChild(input_handsOn_length);
            thisComp.dom.handsOnTime = input_handsOn_length;
            var unit_handsOn_length = document.createElement("span");
            unit_handsOn_length.classList.add("unit");
            unit_handsOn_length.innerText = "min";
            handsOn_length.appendChild(unit_handsOn_length);
            /* CLOSE */
            /* OPEN */
            var handsOn_problems = document.createElement("div");
            makeFull(handsOn_problems);
            handsOn_problems.onclick = function () { thisComp.toggleHandsOnProblems(handsOn_problems); };
            handsOn_problems.innerHTML = "Problems<span style='float:right;font-weight:bold;'>+</span>";
            handsOn_problems.style.cursor = "pointer";
            handsOn_section.appendChild(handsOn_problems);
            /* CLOSE */
            /* OPEN */
            var handsOn_changeproblems = document.createElement("div");
            makeFull(handsOn_changeproblems);
            handsOn_changeproblems.classList.add("handsOnSectionProblemCnt");
            handsOn_changeproblems.classList.add("competitionSubgroup");
            handsOn_changeproblems.style.display = "none";
            handsOn_changeproblems.innerHTML = "<p>Problems</p>";
            handsOn_section.appendChild(handsOn_changeproblems);
            thisComp.dom.handsOnProblems = handsOn_changeproblems;
            var list_handsOn_changeproblems = document.createElement("ol");
            list_handsOn_changeproblems.classList.add("handsOnProblemList");
            if (thisComp.handsOnExists) {
                thisComp.handsOn.problemMap.length = 0;
                for (var i = 0, j = handsOnProblemNames.length; i < j; i++) {
                    addProblem(i);
                }
            }
            handsOn_changeproblems.appendChild(list_handsOn_changeproblems);
            var add_handsOn_changeproblems = document.createElement("button");
            add_handsOn_changeproblems.innerText = "Add";
            add_handsOn_changeproblems.onclick = function () {
                addProblem(-1);
            };
            handsOn_changeproblems.appendChild(add_handsOn_changeproblems);
            /* CLOSE */
            /* OPEN */
            var handsOn_studentlink = document.createElement("div");
            makeFull(handsOn_studentlink);
            handsOn_studentlink.innerText = "Student Packet Link";
            handsOn_section.appendChild(handsOn_studentlink);
            var input_handsOn_studentlink = document.createElement("input");
            input_handsOn_studentlink.name = "frqStudentPacket";
            input_handsOn_studentlink.type = "url";
            input_handsOn_studentlink.placeholder = "https://what_the_student_sees.com";
            input_handsOn_studentlink.value = thisComp.handsOn.studentPacketLink ? thisComp.handsOn.studentPacketLink : "";
            handsOn_studentlink.appendChild(input_handsOn_studentlink);
            thisComp.dom.handsOnStudentPacket = input_handsOn_studentlink;
            /* CLOSE */
            /* OPEN */
            var handsOn_maxPoints = document.createElement("div");
            makeHalf(handsOn_maxPoints);
            handsOn_maxPoints.innerHTML = "<h3>Maximum Points</h3>";
            handsOn_section.appendChild(handsOn_maxPoints);
            var input_handsOn_maxPoints = document.createElement("input");
            input_handsOn_maxPoints.type = "number";
            input_handsOn_maxPoints.name = "frqMaxPoints";
            input_handsOn_maxPoints.value = "" + thisComp.handsOn.maxPoints ? "" + thisComp.handsOn.maxPoints : "60";
            input_handsOn_maxPoints.min = "0";
            input_handsOn_maxPoints.max = "999";
            input_handsOn_maxPoints.step = "1";
            var input_handsOn_maxPoints_old = "" + thisComp.handsOn.maxPoints ? "" + thisComp.handsOn.maxPoints : "60";
            input_handsOn_maxPoints.onchange = function () {
                if (!input_handsOn_maxPoints.validity.valid)
                    input_handsOn_maxPoints.value = input_handsOn_maxPoints_old;
                else
                    input_handsOn_maxPoints_old = input_handsOn_maxPoints.value;
            };
            handsOn_maxPoints.appendChild(input_handsOn_maxPoints);
            thisComp.dom.handsOnMaxPoints = input_handsOn_maxPoints;
            /* CLOSE */
            /* OPEN */
            var handsOn_incorrectPenalty = document.createElement("div");
            makeHalf(handsOn_incorrectPenalty);
            handsOn_incorrectPenalty.innerHTML = "<h3>Incorrect Submission Penalty</h3>";
            handsOn_section.appendChild(handsOn_incorrectPenalty);
            var input_handsOn_incorrectPenalty = document.createElement("input");
            input_handsOn_incorrectPenalty.name = "frqIncorrectPenalty";
            input_handsOn_incorrectPenalty.type = "number";
            input_handsOn_incorrectPenalty.value = "" + thisComp.handsOn.incorrectPenalty ? "" + thisComp.handsOn.incorrectPenalty : "-5";
            input_handsOn_incorrectPenalty.min = "-999";
            input_handsOn_incorrectPenalty.max = "0";
            input_handsOn_incorrectPenalty.step = "1";
            input_handsOn_incorrectPenalty.placeholder = "-5";
            var input_handsOn_incorrectPenalty_old = "" + thisComp.handsOn.incorrectPenalty ? "" + thisComp.handsOn.incorrectPenalty : "-5";
            input_handsOn_incorrectPenalty.onchange = function () {
                if (!input_handsOn_incorrectPenalty.validity.valid)
                    input_handsOn_incorrectPenalty.value = input_handsOn_incorrectPenalty_old;
                else
                    input_handsOn_incorrectPenalty_old = input_handsOn_incorrectPenalty.value;
            };
            handsOn_incorrectPenalty.appendChild(input_handsOn_incorrectPenalty);
            thisComp.dom.handsOnIncorrectPenalty = input_handsOn_incorrectPenalty;
            /* CLOSE */
            return handsOn_section;
        }
        var thisComp = this; /* used because event handlers override the "this" variable */
        var form = document.createElement("div");
        form.classList.add("competition");
        this.dom.form = form;
        var header = document.createElement("div");
        header.onclick = function (event) {
            event.stopPropagation();
            if (thisComp.published)
                window.location.href = "/console/competitions?cid=" + thisComp.cid;
        };
        header.classList.add("comp_head");
        form.appendChild(header);
        this.dom.comp_head = header;
        var header_name = document.createElement("span");
        header_name.innerText = this.name;
        this.dom.comp_head_name = header_name;
        header.appendChild(header_name);
        /* OPEN */
        var controls = document.createElement("div");
        controls.classList.add("competition_controls");
        controls.onclick = function (event) {
            event.stopPropagation();
        };
        header.appendChild(controls);
        var controls_edit = document.createElement("img");
        controls_edit.src = "/res/console/edit.svg";
        controls_edit.classList.add("competition_edit");
        controls_edit.onclick = function () {
            toggleEditCompetition(thisComp);
        };
        this.dom.controlsEdit = controls_edit;
        controls.appendChild(controls_edit);
        if (this.published)
            controls.appendChild(this.getOpenCompetition());
        var controls_save = document.createElement("img");
        controls_save.src = "/res/console/save.svg";
        controls_save.onclick = function (event) {
            event.stopPropagation();
            thisComp.saveCompetition();
        };
        this.dom.controlsSave = controls_save;
        controls.appendChild(controls_save);
        var controls_delete = document.createElement("img");
        controls_delete.src = "/res/console/delete.svg";
        controls_delete.onclick = function (event) {
            event.stopPropagation();
            thisComp.delete();
        };
        this.dom.controlsDelete = controls_delete;
        controls.appendChild(controls_delete);
        /* CLOSE */
        /*let deleteCompetition = document.createElement("p");
        deleteCompetition.innerText = "Delete";
        deleteCompetition.classList.add("deleteCompetition");
        deleteCompetition.classList.add("competitionControl");
        deleteCompetition.onclick = function(event) {
            event.stopPropagation();
            thisComp.delete();
        };
        header.appendChild(deleteCompetition);*/
        var publishCompetition = document.createElement("p");
        if (this.published) {
            publishCompetition.innerText = "Published";
            publishCompetition.style.backgroundColor = "unset";
            publishCompetition.style.color = "var(--body-col)";
            form.classList.add("published");
        }
        else {
            publishCompetition.innerText = "Publish";
            publishCompetition.onclick = function (event) {
                event.stopPropagation();
                thisComp.publishCompetition(function () {
                    publishCompetition.onclick = null;
                    publishCompetition.innerHTML = "Published";
                    publishCompetition.style.backgroundColor = "unset";
                    publishCompetition.style.color = "var(--body-col)";
                    form.classList.add("published");
                });
            };
        }
        publishCompetition.classList.add("publishCompetition");
        publishCompetition.classList.add("competitionControl");
        header.appendChild(publishCompetition);
        /*let saveCompetition = document.createElement("p");
        saveCompetition.innerText = "Save";
        saveCompetition.classList.add("saveCompetition");
        saveCompetition.classList.add("competitionControl");
        saveCompetition.onclick = function(event) {
            event.stopPropagation();
            thisComp.saveCompetition();
        };

        header.appendChild(saveCompetition);

        let viewCompetition = document.createElement("p");
        viewCompetition.innerText = "View";
        viewCompetition.classList.add("viewCompetition");
        viewCompetition.classList.add("competitionControl");
        if(!thisComp.published) viewCompetition.style.display = "none";
        else viewCompetition.onclick = function(event){event.stopPropagation();window.location.href = "/console/competitions?cid="+thisComp.cid;};
        this.dom.viewCompetition = viewCompetition;
        header.appendChild(viewCompetition);*/
        var body = document.createElement("div");
        body.classList.add("comp_edit");
        form.appendChild(body);
        this.dom.comp_edit = body;
        /* OPEN *
        let view_competition = document.createElement("p");
        view_competition.innerText = "Jump to competition";
        view_competition.onclick = function() {
            if(thisComp.cid.length == 0) addErrorBox(thisComp.dom.comp_edit, "Save the competition to view it.");
            else window.location.href = "/uil?cid="+thisComp.cid;
        };
        view_competition.classList.add("viewCompetition");
        makeFull(view_competition);
        body.appendChild(view_competition);
        /* CLOSE */
        /* OPEN */
        var name_and_save = document.createElement("div");
        makeFull(name_and_save);
        body.appendChild(name_and_save);
        var h2_name = document.createElement("h3");
        h2_name.innerText = "Name";
        name_and_save.appendChild(h2_name);
        var input_name = document.createElement("input");
        input_name.classList.add("name");
        input_name.name = "name";
        input_name.type = "text";
        input_name.value = this.name;
        input_name.placeholder = "New Competition";
        input_name.dataset.originalName = this.name;
        name_and_save.appendChild(input_name);
        this.dom.compName = input_name;
        /* CLOSE */
        /* OPEN */
        var isPublic_header = document.createElement("div");
        makeHalf(isPublic_header);
        body.appendChild(isPublic_header);
        var h2_isPublic_header = document.createElement("h3");
        h2_isPublic_header.innerHTML = "<b>Is Public</b>";
        isPublic_header.appendChild(h2_isPublic_header);
        /* CLOSE */
        /* OPEN */
        var isPublic_toggle = document.createElement("div");
        makeHalf(isPublic_toggle);
        body.appendChild(isPublic_toggle);
        var isPublic_toggle_input = document.createElement("input");
        isPublic_toggle_input.classList.add("checkbox");
        isPublic_toggle_input.type = "checkbox";
        isPublic_toggle_input.name = "isPublic";
        isPublic_toggle_input.onclick = function () {
            thisComp.isPublic = isPublic_toggle_input.checked;
        };
        if (this.isPublic)
            isPublic_toggle_input.checked = true;
        isPublic_toggle.appendChild(isPublic_toggle_input);
        this.dom.compPublic = isPublic_toggle_input;
        /* CLOSE */
        /* OPEN
        let page_text_header = document.createElement("div");
        makeFull(page_text_header);
        body.appendChild(page_text_header);

        let h2_page_text = document.createElement("h2");
        h2_page_text.innerHTML = "<b>Page Text</b>";
        page_text_header.appendChild(h2_page_text);
        /* CLOSE */
        /* OPEN */
        var whatItIs = document.createElement("div");
        whatItIs.innerHTML = "Description";
        makeFull(whatItIs);
        body.appendChild(whatItIs);
        var whatItIs_textarea = document.createElement("textarea");
        whatItIs_textarea.value = whatItIsText;
        whatItIs_textarea.classList.add("pageTextTextarea");
        whatItIs_textarea.maxLength = 3000;
        whatItIs.appendChild(whatItIs_textarea);
        this.dom.description = whatItIs_textarea;
        /* CLOSE */
        /* OPEN
        let rules = document.createElement("div");
        rules.innerHTML = "Rules";
        makeFull(rules);
        body.appendChild(rules);

        let rules_textarea = document.createElement("textarea");
        rules_textarea.value = rulesText;
        rules_textarea.classList.add("pageTextTextarea");
        rules_textarea.maxLength = 3000;
        rules.appendChild(rules_textarea);
        this.dom.rules = rules_textarea;
        /* CLOSE */
        /* OPEN */
        var written_header = document.createElement("div");
        makeHalf(written_header);
        body.appendChild(written_header);
        var h2_written_exists = document.createElement("h3");
        h2_written_exists.innerHTML = "<b>Written Test</b>";
        written_header.appendChild(h2_written_exists);
        /* CLOSE */
        /* OPEN */
        var written_toggle = document.createElement("div");
        makeHalf(written_toggle);
        body.appendChild(written_toggle);
        var written_toggle_input = document.createElement("input");
        written_toggle_input.classList.add("checkbox");
        written_toggle_input.type = "checkbox";
        written_toggle_input.name = "writtenExists";
        written_toggle_input.onclick = function () { thisComp.toggleWrittenTest(); };
        if (this.writtenExists)
            written_toggle_input.checked = true;
        written_toggle.appendChild(written_toggle_input);
        // @ts-ignore
        if (this.writtenExists)
            written_toggle.checked = true;
        /* CLOSE */
        body.appendChild(getWrittenSection());
        /* OPEN */
        var h2_handsOn_toggle = document.createElement("div");
        makeHalf(h2_handsOn_toggle);
        h2_handsOn_toggle.innerHTML = "<h3><b>Hands-On (for UIL CS)</b></h3>";
        body.appendChild(h2_handsOn_toggle);
        /* CLOSE */
        /* OPEN */
        var handsOn_toggle = document.createElement("div");
        makeHalf(handsOn_toggle);
        body.appendChild(handsOn_toggle);
        var checkbox_handsOn_toggle = document.createElement("input");
        checkbox_handsOn_toggle.type = "checkbox";
        checkbox_handsOn_toggle.name = "handsOnExists";
        checkbox_handsOn_toggle.classList.add("checkbox");
        checkbox_handsOn_toggle.onclick = function () { thisComp.toggleHandsOnTest(); };
        if (this.handsOnExists)
            checkbox_handsOn_toggle.checked = true;
        handsOn_toggle.appendChild(checkbox_handsOn_toggle);
        /* CLOSE */
        body.appendChild(getHandsOnSection());
        return form;
    };
    return Competition;
}());
/***
 * Loads in the competitions from the database.
 * */
function loadCompetitions() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) { // If an error occurred
                var responses = JSON.parse(xhr.responseText);
                if (responses.length > 0) {
                    dom.class_competitions.dataset.hasCompetitions = "true";
                    var first = true;
                    for (var _i = 0, responses_1 = responses; _i < responses_1.length; _i++) {
                        var competition = responses_1[_i];
                        var obj = new Competition(competition["cid"], competition["published"], competition["isPublic"], competition["name"], competition["written"], competition["handsOn"]);
                        var handsOnProblemsList = [];
                        if (competition["handsOn"] != null)
                            handsOnProblemsList = competition["handsOn"]["problems"];
                        var compDom = obj.getDOM(handsOnProblemsList, competition["description"], competition["rules"]);
                        if (first) {
                            visible_competition_edit_dom = obj.dom.comp_edit;
                        }
                        dom.class_competitions.appendChild(compDom);
                    }
                }
            }
        }
    };
    xhr.open('GET', "/console/profile?action=getCompetitions", true);
    xhr.send(null);
    return false;
}
/***
 * Shows a competition's edit html
 */
var visible_competition_edit_dom;
function toggleEditCompetition(competition) {
    var newCompEdit = competition.dom.comp_edit;
    if (visible_competition_edit_dom != null) {
        visible_competition_edit_dom.style.display = "none";
        var controls = visible_competition_edit_dom.parentNode.getElementsByClassName("competition_controls")[0].childNodes;
        for (var i = 0, j = controls.length; i < j; i++) {
            var element = controls[i];
            if (!element.classList.contains("competition_edit")) {
                element.style.display = "none";
            }
        }
    }
    if (visible_competition_edit_dom != newCompEdit) {
        visible_competition_edit_dom = newCompEdit;
        visible_competition_edit_dom.style.display = "grid";
        var controls = visible_competition_edit_dom.parentNode.getElementsByClassName("competition_controls")[0].childNodes;
        for (var i = 0, j = controls.length; i < j; i++) {
            var element = controls[i];
            element.style.display = "block";
        }
    }
    else {
        visible_competition_edit_dom = null;
    }
}
/***
 * Creates a new competition and opens it up for editing
 */
function createNewCompetition() {
    if (competitions.length <= 0) {
        dom.class_competitions.innerHTML = "";
    }
    var competition = new Competition("", false, false, "New Competition", false, false);
    dom.class_competitions.appendChild(competition.getDOM([], "", ""));
    toggleEditCompetition(competition);
    showClassComps(document.getElementById("showClassComps"));
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
