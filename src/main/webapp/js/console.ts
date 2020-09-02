const config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        fname: "fname",
        lname:"lname",
        school: "school",
        oldPassword : "oldPassword",
        newPassword : "newPassword",
        delUserPassword : "delUserPassword",
        profile_link : "profile_link",
        Profile : "Profile",
        Class : "Class",
        competitions : "Competitions",
        competitionList : "competitionList"
    },
    COMPETITION: {
        mcOptions : ["a","b","c","d","e"]
    }
}

/***
 * Helps interfacing with static elements (are not deleted *
 */
let dom = {
    cached : {},    // DOM objects that have already been accessed
    getHelper(id): HTMLElement {
        if(this.cached[id] == null) this.cached[id] = document.getElementById(id);
        return this.cached[id];
    },
    get fname(){return this.getHelper(config.IDs.fname)},
    get lname(){return this.getHelper(config.IDs.lname)},
    get school(){return this.getHelper(config.IDs.school)},
    get oldPassword(){return this.getHelper(config.IDs.oldPassword)},
    get newPassword(){return this.getHelper(config.IDs.newPassword)},
    get delUserPassword(){return this.getHelper(config.IDs.delUserPassword)},
    get profileLink(){return this.getHelper(config.IDs.profile_link)},
    get profile(){return this.getHelper(config.IDs.Profile)},
    get class(){return this.getHelper(config.IDs.Class)},
    get competitions(){return this.getHelper(config.IDs.competitions)},
    get competitionList(){return this.getHelper(config.IDs.competitionList)}
};

let pageState = {
    selectedNav : null,    /* The dom of the nav <li> object that is currently selected */
};

enum WrittenType {
    MC,
    SAQ
}

class WrittenProblem {
    answer:string;
    type:WrittenType;

    constructor(answer:string, type:WrittenType) {
        this.answer = answer;
        this.type = type;
    }
}

class HandsOnProblem {
    dom : {
        name: HTMLInputElement,
        input: HTMLInputElement,
        output: HTMLInputElement
    } = {
        name: null,
        input: null,
        output: null
    };
    /* If this problem has been saved to the server, oldIndex stores the index of this problem in the server.
    *  If it has not been saved, it is set to -1, and when it is saved it will be overwritten to be the index
    *  at the time of saving */
    oldIndex : number = -1;
}

let competitions: Competition[] = [];

class Competition {
    cid : string;
    name: string;

    isPublic: boolean;
    writtenExists: boolean;
    handsOnExists: boolean;

    written: {
        opens: string,
        key: WrittenProblem[],
        correctPoints: number,
        incorrectPoints: number,
        instructions: string,
        testLink: string,
        answersLink: string,
        time: number
    } = {
        opens: "",
        key: [],
        correctPoints:0,
        incorrectPoints:0,
        instructions:"",
        testLink:"",
        answersLink:"",
        time:0
    };

    handsOn: {
        opens: string,
        maxPoints: number,
        incorrectPenalty: number,
        problemMap: HandsOnProblem[],
        studentPacketLink: string,
        judgePacketLink: string,
        time: number
    } = {
        opens:"",
        maxPoints:0,
        incorrectPenalty:0,
        problemMap:[],
        studentPacketLink:"",
        judgePacketLink:"",
        time:0
    };

    dom: {
        form: HTMLElement,
        comp_head: HTMLDivElement,
        comp_edit: HTMLDivElement,
        compName : HTMLInputElement,
        compPublic : HTMLInputElement,

        description : HTMLTextAreaElement,
        rules : HTMLTextAreaElement,

        writtenSection: HTMLSpanElement,
        writtenOpen: HTMLInputElement,
        writtenTime : HTMLInputElement,
        writtenKey: HTMLElement,
        writtenAnswerList: HTMLElement,
        writtenInstructionCnt: HTMLElement,
        writtenInstructions: HTMLTextAreaElement,
        writtenTestLink : HTMLInputElement,
        writtenPointsPerCorrect : HTMLInputElement,
        writtenPointsPerIncorrect : HTMLInputElement,

        handsOnSection: HTMLSpanElement,
        handsOnStart : HTMLInputElement,
        handsOnTime : HTMLInputElement,
        handsOnProblems: HTMLElement,
        handsOnStudentPacket : HTMLInputElement,
        handsOnMaxPoints : HTMLInputElement,
        handsOnIncorrectPenalty : HTMLInputElement
    } = {
        form: null,
        comp_head: null,
        comp_edit: null,
        compName : null,
        compPublic : null,

        description : null,
        rules : null,

        writtenSection: null,
        writtenOpen: null,
        writtenTime : null,
        writtenKey: null,
        writtenAnswerList: null,
        writtenInstructionCnt: null,
        writtenInstructions: null,
        writtenTestLink : null,
        writtenPointsPerCorrect : null,
        writtenPointsPerIncorrect : null,

        handsOnSection: null,
        handsOnStart : null,
        handsOnTime : null,
        handsOnProblems: null,
        handsOnStudentPacket : null,
        handsOnMaxPoints : null,
        handsOnIncorrectPenalty : null
    };

    constructor(cid: string, isPublic: boolean, name:string, writtenObj:any, handsOnObj:any) {
        competitions.push(this);

        this.cid = cid;
        this.isPublic = isPublic;
        this.name = name;
        this.writtenExists = !!writtenObj;
        this.handsOnExists = !!handsOnObj;

        if(this.writtenExists) {   /* written exists */
            this.written.opens = writtenObj.opens;

            /* Add in key */
            let answer:any[];  /* First element is the answer, second is the type. type=0 means MC, type=1 means SAQ */
            for(answer of writtenObj.answers) {
                let type:WrittenType = WrittenType.MC;
                if(parseInt(answer[1])===1) type = WrittenType.SAQ;
                this.written.key.push(new WrittenProblem(answer[0], type));
            }

            this.written.correctPoints = writtenObj.correctPoints;
            this.written.incorrectPoints = writtenObj.incorrectPoints;
            this.written.instructions = writtenObj.instructions;
            this.written.testLink = writtenObj.testLink;
            this.written.answersLink = writtenObj.answersLink;
            this.written.time = writtenObj.time;
        }

        if(this.handsOnExists) {    /* HandsOn exists */
            this.handsOn.opens = handsOnObj.opens;
            this.handsOn.maxPoints = handsOnObj.maxPoints;
            this.handsOn.incorrectPenalty = handsOnObj.incorrectPenalty;
            this.handsOn.studentPacketLink = handsOnObj.studentPacketLink;
            this.handsOn.judgePacketLink = handsOnObj.judgePacketLink;
            this.handsOn.time = handsOnObj.time;
        }
    }

    toggleWrittenKey() {
        if(this.dom.writtenKey.style.display !== "block") {
            this.dom.writtenKey.style.display = "block";
            if(this.dom.writtenInstructionCnt.style.display === "block") {
                this.dom.writtenInstructionCnt.style.display = "none";
            }
        } else {
            this.dom.writtenKey.style.display = "none";
        }
    }

    toggleHandsOnProblems() {
        if(this.dom.handsOnProblems.style.display !== "block")
            this.dom.handsOnProblems.style.display = "block";
        else
            this.dom.handsOnProblems.style.display = "none";
    }

    toggleWrittenTest() {
        if(this.writtenExists) {   /* It is now unchecked so show the div */
            this.dom.writtenSection.style.display = "none";
        } else {
            this.dom.writtenSection.style.display = "grid";
        }
        this.writtenExists = !this.writtenExists;
    }

    toggleHandsOnTest() {
        if(this.handsOnExists) {   /* It is now unchecked so show the div */
            this.dom.handsOnSection.style.display = "none";
        } else {
            this.dom.handsOnSection.style.display = "grid";
        }
        this.handsOnExists = !this.handsOnExists;
    }

    toggleWrittenInstructions() {
        if(this.dom.writtenInstructionCnt.style.display === "none") {
            this.dom.writtenInstructionCnt.style.display = "block";
            if(this.dom.writtenKey.style.display === "block") { // Written key is showing
                this.dom.writtenKey.style.display = "none";
            }
        } else {
            this.dom.writtenInstructionCnt.style.display = "none";
            this.written.instructions = this.dom.writtenInstructions.value;
        }
    }

    delete() {
        if(this.cid.length !== 0) {  /* This competition has already been saved */
            let xhr: XMLHttpRequest = new XMLHttpRequest();
            xhr.open('POST', "/profile", true);
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            xhr.send("action=deleteCompetition&cid=" + this.cid);
        }
        dom.competitionList.removeChild(this.dom.form);
        competitions.splice(competitions.indexOf(this),1);

        if(dom.competitionList.childNodes.length <= 0) {
            dom.competitionList.dataset.hasCompetitions = "false";
            dom.competitionList.innerHTML = "Click 'New' to create a competition.";
        }
    }

    saveCompetition():boolean {
        // Remove the error box
        try {this.dom.comp_edit.removeChild(document.getElementById("ERROR"));}
        catch (e){}

        /* First, set the name and open date in the title */
        this.dom.comp_head.innerHTML =  this.dom.compName.value;
        let deleteCompetition = document.createElement("p");
        deleteCompetition.innerText = "Delete";
        deleteCompetition.classList.add("deleteCompetition");
        deleteCompetition.onclick = function() {
            thisComp.delete();
        };
        this.dom.comp_head.appendChild(deleteCompetition);

        let formData:FormData = new FormData();
        let thisComp:Competition = this;  // So that it can be referenced in other functions

        formData.append("cid", this.cid);
        formData.append("name", this.dom.compName.value);
        formData.append("isPublic", ""+this.dom.compPublic.checked);
        formData.append("description", this.dom.description.value);
        formData.append("writtenExists", ""+this.writtenExists);
        if(this.writtenExists) {
            formData.append("mcOpens", this.dom.writtenOpen.value);
            formData.append("mcTime", ""+this.dom.writtenTime.value);
            formData.append("mcCorrectPoints", ""+this.dom.writtenPointsPerCorrect.value);
            formData.append("mcIncorrectPoints", ""+this.dom.writtenPointsPerIncorrect.value);
            formData.append("mcInstructions", this.dom.writtenInstructions.value);
            formData.append("mcTestLink", this.dom.writtenTestLink.value);
            formData.append("mcAnswersLink", "");   // TODO: Add this
            let answers:string[][] = [];
            for(let answer of this.written.key) {
                answers.push([answer.answer, ""+answer.type]);
            }
            formData.append("mcAnswers",JSON.stringify(answers))
        }

        formData.append("handsOnExists", "" + this.handsOnExists);
        if(this.handsOnExists) {
            formData.append("frqOpens", this.dom.handsOnStart.value);
            formData.append("frqTime", ""+this.dom.handsOnTime.value);
            formData.append("frqMaxPoints", ""+this.dom.handsOnMaxPoints.value);
            formData.append("frqIncorrectPenalty", ""+this.dom.handsOnIncorrectPenalty.value);
            formData.append("frqStudentPacket", this.dom.handsOnStudentPacket.value);
            formData.append("frqJudgePacket", "");  // TODO: Add this

            let problems:string[] = [];
            let problemIndices:number[] = [];   /* A list like [1, 2, -1, 9, 5], corresponding to a name in the problems array */
            for(let i=0,j=this.handsOn.problemMap.length;i<j;i++) {
                let problem:HandsOnProblem = this.handsOn.problemMap[i];
                let probName: string = problem.dom.name.value;
                problems.push(probName);
                problemIndices.push(problem.oldIndex);
                problem.oldIndex = i+1; /* Now that the problem has been saved, we set it to be its current index */

                console.log("Input file length="+problem.dom.input.files.length+", output file length="+problem.dom.output.files.length);
                if(problem.dom.input.files.length > 0) {
                    formData.append("fi:"+i, problem.dom.input.files[0]);
                }
                if(problem.dom.output.files.length > 0) {
                    formData.append("fo:"+i, problem.dom.output.files[0]);
                }
            }
            formData.append("frqProblemMap", JSON.stringify(problems));
            formData.append("frqIndices", JSON.stringify(problemIndices));
        }
        formData.append("action", "saveCompetition");
        let xhr:XMLHttpRequest = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    let response = JSON.parse(xhr.responseText);
                    if(response["success"] != null) {
                        for(let problem of thisComp.handsOn.problemMap) {
                            problem.dom.input.value = "";
                            problem.dom.output.value = "";
                        }
                        addSuccessBox(thisComp.dom.comp_edit, response["success"]);
                        if(thisComp.cid.length === 0 && response["cid"] != null) thisComp.cid = response["cid"];
                    } else if(response["error"] != null) {    // An error occurred
                        addErrorBox(thisComp.dom.comp_edit, response["error"]);
                    } else {
                        addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                    }
                } else {
                    addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error);
                }
            }
        };
        xhr.open('POST', "/profile", true);
        xhr.send(formData);
        return false;
    }

    addWrittenQuestion(writtenProblem:WrittenProblem) {
        if(writtenProblem == null) {    // This question doesn't exist
            writtenProblem =new WrittenProblem("a", WrittenType.MC);
            this.written.key.push(writtenProblem);
        }

        let thisComp:Competition = this;
        let newLi:HTMLElement = document.createElement("li");

        let newP:HTMLSelectElement;

        let newInput:HTMLInputElement = document.createElement("input");
        newInput.type = "text";
        newInput.value = writtenProblem.answer;
        newInput.onchange = function() {
            let newValue:string = newInput.value;
            writtenProblem.answer = newValue;
            if(!config.COMPETITION.mcOptions.includes(newValue)) {   /* It is now an SAQ */
                newP.innerText = "SAQ";
                writtenProblem.type = WrittenType.SAQ;
            }
        };
        newLi.appendChild(newInput);


        newP = document.createElement("select");
        let mcSelected = "";
        let saqSelected = "selected";
        if(writtenProblem.type === WrittenType.MC) {
            mcSelected = "selected";
            saqSelected = "";
        }
        newP.innerHTML = "<option "+mcSelected+">MC</option><option "+saqSelected+">SAQ</option>"
        newP.onchange = function(){
            if(writtenProblem.type === WrittenType.MC) {  /* Switch to SAQ */
                writtenProblem.type = WrittenType.SAQ;
            } else if(config.COMPETITION.mcOptions.includes(newInput.value)) { /* It must be a valid multiple choice option */
                writtenProblem.type = WrittenType.MC;
            } else {
                newP.selectedIndex = 1;
            }
        };
        newP.classList.add('writtenAnswerType');
        newLi.appendChild(newP);

        let delQuestion = document.createElement("img");
        delQuestion.src = "/res/close.svg";
        delQuestion.classList.add("deleteProblem");
        delQuestion.onclick = function() {
            thisComp.written.key.splice(thisComp.written.key.indexOf(writtenProblem), 1);
            thisComp.dom.writtenAnswerList.removeChild(newLi);
        };
        newLi.appendChild(delQuestion);

        this.dom.writtenAnswerList.appendChild(newLi);
        newLi.focus();
    }

    getDOM(handsOnProblemNames:string[], whatItIsText:string, rulesText:string, ):HTMLElement {
        function makeHalf(element:HTMLElement) {
            element.classList.add("profile_cmpnt");
            element.classList.add("half");
        }

        function makeFull(element:HTMLElement) {
            element.classList.add("profile_cmpnt");
            element.classList.add("full");
        }

        function getWrittenSection():HTMLElement {
            let written_section = document.createElement("span");
            written_section.classList.add("writtenSection");
            thisComp.dom.writtenSection = written_section;
            if(!thisComp.writtenExists) written_section.style.display = "none";
            thisComp.dom.writtenSection = written_section;

            /* OPEN */
            let written_open = document.createElement("div");
            makeHalf(written_open);
            written_section.appendChild(written_open);

            let h2_written_open = document.createElement("h2");
            h2_written_open.innerText = "Start";
            written_open.appendChild(h2_written_open);

            let input_written_open = document.createElement("input");
            input_written_open.type = "datetime-local";
            // input_written_open.value = (thisComp.written.opens?thisComp.written.opens:"");
            input_written_open.name = "mcOpens";
            input_written_open.classList.add("start");
            written_open.appendChild(input_written_open);
            // thisComp.dom.writtenOpen = input_written_open;
            let curDate:Date = new Date();
            let defaultDate:Date = curDate;
            if(thisComp.written.opens) defaultDate = new Date(thisComp.written.opens);
            let minDate = curDate;
            if(defaultDate < curDate) minDate = defaultDate;
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
            let written_length = document.createElement("div");
            makeHalf(written_length);
            written_section.appendChild(written_length);

            let h2_written_length = document.createElement("h2");
            h2_written_length.innerText = "Length";
            written_length.appendChild(h2_written_length);

            let input_written_length = document.createElement("input");
            input_written_length.name = "mcTime";
            input_written_length.type = "text";
            input_written_length.value = (""+thisComp.written.time?""+thisComp.written.time:"45");
            input_written_length.classList.add("length");
            written_length.appendChild(input_written_length);
            thisComp.dom.writtenTime = input_written_length;

            let span_written_length = document.createElement("span");
            span_written_length.classList.add("unit");
            span_written_length.innerText = "min";
            written_length.appendChild(span_written_length);
            /* CLOSE */

            /* OPEN */
            let written_answers = document.createElement("div");
            makeFull(written_answers);
            written_answers.onclick = function() {thisComp.toggleWrittenKey();};
            written_section.appendChild(written_answers);
            written_answers.style.cursor = "pointer";

            let h2_written_answers = document.createElement("h2");
            h2_written_answers.innerHTML = "Answers<span style='float:right;font-weight:bold;'>+</span>";
            written_answers.appendChild(h2_written_answers);
            /* CLOSE */

            /* OPEN */
            let written_key = document.createElement("div");
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

            let list_written_key = document.createElement("ol");
            list_written_key.classList.add("writtenSectionAnswerList");
            written_key.appendChild(list_written_key);
            thisComp.dom.writtenAnswerList = list_written_key;

            if(thisComp.writtenExists) {
                for(let i=0,j=thisComp.written.key.length;i<j;i++) {
                    thisComp.addWrittenQuestion(thisComp.written.key[i]);
                }
            }

            let add_written_key = document.createElement("button");
            add_written_key.classList.add("addWrittenQuestion");
            add_written_key.onclick = function(){thisComp.addWrittenQuestion(null);};
            add_written_key.innerText = "Add";
            written_key.appendChild(add_written_key);
            /* CLOSE */

            /* OPEN */
            let written_instructions = document.createElement("div");
            makeFull(written_instructions);
            written_instructions.onclick = function(){thisComp.toggleWrittenInstructions();};
            written_section.appendChild(written_instructions);
            written_instructions.style.cursor = "pointer";

            let h2_written_instructions = document.createElement("h2");
            h2_written_instructions.innerHTML = "Instructions<span style='float:right;font-weight:bold'>+</span>";
            written_instructions.appendChild(h2_written_instructions);
            /* CLOSE */

            /* OPEN */
            let written_instructions_change = document.createElement("div");
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

            let textarea_written_instructions_change = document.createElement("textarea");
            textarea_written_instructions_change.classList.add("writtenSectionInstructions");
            textarea_written_instructions_change.maxLength = 144;
            written_instructions_change.appendChild(textarea_written_instructions_change);
            textarea_written_instructions_change.value = thisComp.written.instructions;
            thisComp.dom.writtenInstructions = textarea_written_instructions_change;
            /* CLOSE */

            /* OPEN */
            let written_testlink = document.createElement("div");
            makeFull(written_testlink);
            written_section.appendChild(written_testlink);

            let h2_written_testlink = document.createElement("h2");
            h2_written_testlink.innerText = "Text Link";
            written_testlink.appendChild(h2_written_testlink);

            let input_written_testLink = document.createElement("input");
            input_written_testLink.name = "mcTestLink";
            input_written_testLink.type = "url";
            input_written_testLink.placeholder = "https://what_the_student_sees.com";
            input_written_testLink.value = thisComp.written.testLink?thisComp.written.testLink:"";
            written_testlink.appendChild(input_written_testLink);
            thisComp.dom.writtenTestLink = input_written_testLink;
            /* CLOSE */

            /* OPEN */
            let written_ppc = document.createElement("div");
            makeHalf(written_ppc);
            written_section.appendChild(written_ppc);

            let h2_written_ppc = document.createElement("h2");
            h2_written_ppc.innerText = "Points Per Correct";
            written_ppc.appendChild(h2_written_ppc);

            let input_written_ppc = document.createElement("input");
            input_written_ppc.name = "mcCorrectPoints";
            input_written_ppc.type = "number";
            input_written_ppc.placeholder = "6";
            input_written_ppc.value = ""+thisComp.written.correctPoints?""+thisComp.written.correctPoints:"";
            written_ppc.appendChild(input_written_ppc);
            thisComp.dom.writtenPointsPerCorrect = input_written_ppc;
            /* CLOSE */

            /* OPEN */
            let written_ppi = document.createElement("div");
            makeHalf(written_ppi);
            written_section.appendChild(written_ppi);

            let h2_written_ppi = document.createElement("h2");
            h2_written_ppi.innerText = "Points per Incorrect";
            written_ppi.appendChild(h2_written_ppi);

            let input_written_ppi = document.createElement("input");
            input_written_ppi.name = "mcIncorrectPoints";
            input_written_ppi.type = "number";
            input_written_ppi.value = ""+thisComp.written.incorrectPoints?""+thisComp.written.incorrectPoints:"";
            input_written_ppi.placeholder = "2";
            written_ppi.appendChild(input_written_ppi);
            thisComp.dom.writtenPointsPerIncorrect = input_written_ppi;
            /* CLOSE */

            return written_section;
        }
        function getHandsOnSection():HTMLElement {
            function addProblem(probIndex: number): void {
                let index:number = thisComp.handsOn.problemMap.length;
                let problem:HandsOnProblem = new HandsOnProblem();
                problem.oldIndex = probIndex;
                thisComp.handsOn.problemMap.push(problem);

                let li = document.createElement("li");
                li.dataset.probNum = "" + index;

                let input_name = document.createElement("input");
                input_name.type="text";
                if(handsOnProblemNames.length > index) input_name.value = handsOnProblemNames[index];
                else input_name.value = "";
                input_name.classList.add("handsOn_probName");
                li.appendChild(input_name);
                problem.dom.name = input_name;

                /* Each input has the actual input field that takes in the file but is hidden to the user, as well
                *  as an alias button that serves as the proxy between the user and the input field. This is to prevent
                *  weird file input styling. */
                let input_in_hidden = document.createElement("input");
                input_in_hidden.type = "file";
                input_in_hidden.style.display = "none";
                li.appendChild(input_in_hidden);
                problem.dom.input = input_in_hidden;

                let input_in_proxy = document.createElement("button");
                input_in_proxy.classList.add("handsOn_probIn");
                input_in_proxy.innerText = "Input File";
                li.appendChild(input_in_proxy);
                input_in_proxy.onclick = function(){input_in_hidden.click();};

                let input_out_hidden = document.createElement("input");
                input_out_hidden.type = "file";
                input_out_hidden.style.display = "none";
                li.appendChild(input_out_hidden);
                problem.dom.output = input_out_hidden;

                let input_out_proxy = document.createElement("button");
                input_out_proxy.classList.add("handsOn_probOut");
                input_out_proxy.innerText = "Output File";
                input_out_proxy.onclick = function(){input_out_hidden.click();};
                li.appendChild(input_out_proxy);

                let delQuestion = document.createElement("img");
                delQuestion.src = "/res/close.svg";
                delQuestion.classList.add("deleteProblem");
                delQuestion.onclick = function() {
                    thisComp.handsOn.problemMap.splice(thisComp.handsOn.problemMap.indexOf(problem), 1);
                    list_handsOn_changeproblems.removeChild(li);
                };
                li.appendChild(delQuestion);

                list_handsOn_changeproblems.appendChild(li);
            }

            let handsOn_section = document.createElement("span");
            handsOn_section.classList.add("handsOnSection");
            if(!thisComp.handsOnExists) handsOn_section.style.display = "none";
            written_toggle.appendChild(handsOn_section);
            thisComp.dom.handsOnSection = handsOn_section;

            /* OPEN */
            let handsOn_start = document.createElement("div");
            makeHalf(handsOn_start);
            handsOn_start.innerHTML = "<h2>Start</h2>";
            handsOn_section.appendChild(handsOn_start);

            let input_handsOn_start = document.createElement("input");
            input_handsOn_start.name = "frqOpens";
            input_handsOn_start.type = "datetime-local";
            input_handsOn_start.classList.add("start");
            handsOn_start.appendChild(input_handsOn_start);
            // thisComp.dom.handsOnStart = input_handsOn_start;

            let curDate:Date = new Date();
            let defaultDate:Date = curDate;
            if(thisComp.handsOn.opens) defaultDate = new Date(thisComp.handsOn.opens);
            let minDate = curDate;
            if(defaultDate < curDate) minDate = defaultDate;
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
            let handsOn_length = document.createElement("div");
            makeHalf(handsOn_length);
            handsOn_length.innerHTML = "<h2>Length</h2>";
            handsOn_section.appendChild(handsOn_length);

            let input_handsOn_length = document.createElement("input");
            input_handsOn_length.name = "frqTime";
            input_handsOn_length.type = "number";
            input_handsOn_length.classList.add("length");
            input_handsOn_length.value = ""+thisComp.handsOn.time?""+thisComp.handsOn.time:"120";
            handsOn_length.appendChild(input_handsOn_length);
            thisComp.dom.handsOnTime = input_handsOn_length;

            let unit_handsOn_length = document.createElement("span");
            unit_handsOn_length.classList.add("unit");
            unit_handsOn_length.innerText = "min";
            handsOn_length.appendChild(unit_handsOn_length);
            /* CLOSE */

            /* OPEN */
            let handsOn_problems = document.createElement("div");
            makeFull(handsOn_problems);
            handsOn_problems.innerHTML = "<h2>Problems</h2>";
            handsOn_section.appendChild(handsOn_problems);

            let button_handsOn_problems = document.createElement("button");
            button_handsOn_problems.name = "frqProblems";
            button_handsOn_problems.type = "button";
            button_handsOn_problems.onclick = function(){thisComp.toggleHandsOnProblems();};
            button_handsOn_problems.innerText = "Change Problems";
            handsOn_problems.appendChild(button_handsOn_problems);
            /* CLOSE */

            /* OPEN */
            let handsOn_changeproblems = document.createElement("div");
            makeFull(handsOn_changeproblems);
            handsOn_changeproblems.classList.add("handsOnSectionProblemCnt");
            handsOn_changeproblems.classList.add("competitionSubgroup");
            handsOn_changeproblems.style.display = "none";
            handsOn_changeproblems.innerHTML = "<p>Problems</p>";
            handsOn_section.appendChild(handsOn_changeproblems);
            thisComp.dom.handsOnProblems = handsOn_changeproblems;

            let close_handsOn_changeproblems = document.createElement("p");
            close_handsOn_changeproblems.onclick = function() {thisComp.toggleHandsOnProblems();};
            close_handsOn_changeproblems.innerText = "Save";
            close_handsOn_changeproblems.classList.add("save");
            handsOn_changeproblems.appendChild(close_handsOn_changeproblems);

            let list_handsOn_changeproblems = document.createElement("ol");
            list_handsOn_changeproblems.classList.add("handsOnProblemList");
            if(thisComp.handsOnExists) {
                thisComp.handsOn.problemMap.length = 0;
                for(let i=0, j=handsOnProblemNames.length;i<j;i++) {
                    addProblem(i);
                }
            }
            handsOn_changeproblems.appendChild(list_handsOn_changeproblems);

            let add_handsOn_changeproblems = document.createElement("button");
            add_handsOn_changeproblems.innerText = "Add";
            add_handsOn_changeproblems.onclick = function() {
                addProblem(-1);
            };
            handsOn_changeproblems.appendChild(add_handsOn_changeproblems);
            /* CLOSE */

            /* OPEN */
            let handsOn_studentlink = document.createElement("div");
            makeFull(handsOn_studentlink);
            handsOn_studentlink.innerText = "Student Packet Link";
            handsOn_section.appendChild(handsOn_studentlink);

            let input_handsOn_studentlink = document.createElement("input");
            input_handsOn_studentlink.name = "frqStudentPacket";
            input_handsOn_studentlink.type = "url";
            input_handsOn_studentlink.placeholder = "https://what_the_student_sees.com";
            input_handsOn_studentlink.value = thisComp.handsOn.studentPacketLink?thisComp.handsOn.studentPacketLink:"";
            handsOn_studentlink.appendChild(input_handsOn_studentlink);
            thisComp.dom.handsOnStudentPacket = input_handsOn_studentlink;
            /* CLOSE */

            /* OPEN */
            let handsOn_maxPoints = document.createElement("div");
            makeHalf(handsOn_maxPoints);
            handsOn_maxPoints.innerHTML = "<h2>Maximum Points</h2>";
            handsOn_section.appendChild(handsOn_maxPoints);

            let input_handsOn_maxPoints = document.createElement("input");
            input_handsOn_maxPoints.type = "number";
            input_handsOn_maxPoints.name = "frqMaxPoints";
            input_handsOn_maxPoints.value = ""+thisComp.handsOn.maxPoints?""+thisComp.handsOn.maxPoints:"60";
            handsOn_maxPoints.appendChild(input_handsOn_maxPoints);
            thisComp.dom.handsOnMaxPoints = input_handsOn_maxPoints;
            /* CLOSE */

            /* OPEN */
            let handsOn_incorrectPenalty = document.createElement("div");
            makeHalf(handsOn_incorrectPenalty);
            handsOn_incorrectPenalty.innerHTML = "<h2>Incorrect Penalty</h2>";
            handsOn_section.appendChild(handsOn_incorrectPenalty);

            let input_handsOn_incorrectPenalty = document.createElement("input");
            input_handsOn_incorrectPenalty.name = "frqIncorrectPenalty";
            input_handsOn_incorrectPenalty.type = "number";
            input_handsOn_incorrectPenalty.value = ""+thisComp.handsOn.incorrectPenalty?""+thisComp.handsOn.incorrectPenalty:"5";
            handsOn_incorrectPenalty.appendChild(input_handsOn_incorrectPenalty);
            thisComp.dom.handsOnIncorrectPenalty = input_handsOn_incorrectPenalty;
            /* CLOSE */

            return handsOn_section;
        }

        let thisComp:Competition = this;    /* used because event handlers override the "this" variable */

        let form = document.createElement("div");
        form.classList.add("comp");
        this.dom.form = form;

        let header = document.createElement("div");
        header.onclick = function(){toggleEditCompetition(thisComp);};
        header.innerHTML = this.name;
        header.classList.add("comp_head");
        form.appendChild(header);
        this.dom.comp_head = header;

        let deleteCompetition = document.createElement("p");
        deleteCompetition.innerText = "Delete";
        deleteCompetition.classList.add("deleteCompetition");
        deleteCompetition.onclick = function() {
            thisComp.delete();
        };
        header.appendChild(deleteCompetition);

        let body = document.createElement("div");
        body.classList.add("comp_edit");
        form.appendChild(body);
        this.dom.comp_edit = body;

        /* OPEN */
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
        let name_and_save = document.createElement("div");
        makeFull(name_and_save);
        body.appendChild(name_and_save);

        let h2_name = document.createElement("h2");
        h2_name.innerText = "Name";
        name_and_save.appendChild(h2_name);

        let button_save = document.createElement("button");
        button_save.innerText = "Save";
        button_save.type = "submit";
        button_save.classList.add("save");
        button_save.onclick = function(){thisComp.saveCompetition();};
        name_and_save.appendChild(button_save);

        let input_name = document.createElement("input");
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
        let isPublic_header = document.createElement("div");
        makeHalf(isPublic_header);
        body.appendChild(isPublic_header);

        let h2_isPublic_header = document.createElement("h2");
        h2_isPublic_header.innerHTML = "<b>Is Public</b>";
        isPublic_header.appendChild(h2_isPublic_header);
        /* CLOSE */

        /* OPEN */
        let isPublic_toggle = document.createElement("div");
        makeHalf(isPublic_toggle);
        body.appendChild(isPublic_toggle);

        let isPublic_toggle_input = document.createElement("input");
        isPublic_toggle_input.classList.add("checkbox");
        isPublic_toggle_input.type = "checkbox";
        isPublic_toggle_input.name = "isPublic";
        isPublic_toggle_input.onclick = function(){
            thisComp.isPublic = isPublic_toggle_input.checked;
        };
        if(this.isPublic) isPublic_toggle_input.checked = true;
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
        let whatItIs = document.createElement("div");
        whatItIs.innerHTML = "Description";
        makeFull(whatItIs);
        body.appendChild(whatItIs);

        let whatItIs_textarea = document.createElement("textarea");
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
        let written_header = document.createElement("div");
        makeHalf(written_header);
        body.appendChild(written_header);

        let h2_written_exists = document.createElement("h2");
        h2_written_exists.innerHTML = "<b>Written Test</b>";
        written_header.appendChild(h2_written_exists);
        /* CLOSE */

        /* OPEN */
        let written_toggle = document.createElement("div");
        makeHalf(written_toggle);
        body.appendChild(written_toggle);

        let written_toggle_input = document.createElement("input");
        written_toggle_input.classList.add("checkbox");
        written_toggle_input.type = "checkbox";
        written_toggle_input.name = "writtenExists";
        written_toggle_input.onclick = function(){thisComp.toggleWrittenTest();};
        if(this.writtenExists) written_toggle_input.checked = true;
        written_toggle.appendChild(written_toggle_input);

        // @ts-ignore
        if(this.writtenExists) written_toggle.checked = true;
        /* CLOSE */

        body.appendChild(getWrittenSection());

        /* OPEN */
        let h2_handsOn_toggle = document.createElement("div");
        makeHalf(h2_handsOn_toggle);
        h2_handsOn_toggle.innerHTML = "<h2><b>Hands-On Programming</b></h2>";
        body.appendChild(h2_handsOn_toggle);
        /* CLOSE */

        /* OPEN */
        let handsOn_toggle = document.createElement("div");
        makeHalf(handsOn_toggle);
        body.appendChild(handsOn_toggle);

        let checkbox_handsOn_toggle = document.createElement("input");
        checkbox_handsOn_toggle.type = "checkbox";
        checkbox_handsOn_toggle.name = "handsOnExists";
        checkbox_handsOn_toggle.classList.add("checkbox");
        checkbox_handsOn_toggle.onclick = function(){thisComp.toggleHandsOnTest();};
        if(this.handsOnExists) checkbox_handsOn_toggle.checked = true;
        handsOn_toggle.appendChild(checkbox_handsOn_toggle);
        /* CLOSE */

        body.appendChild(getHandsOnSection());

        return form;
    }
}

let loadClassInterval: number;
function createLoadClassInterval() {
    loadClassInterval = setInterval(loadClass, 30*1000);
}
document.onreadystatechange = () => {
    if(document.readyState === "complete") {
        pageState.selectedNav = dom.profileLink;
        loadCompetitions();
    }
};

/**
 * Loads the class from the server every 30 seconds in case a student has joined their class.
 */
function loadClass() {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response:object = JSON.parse(xhr.responseText);
                if(response["success"] != null) {
                    dom.class.innerHTML = response["classHTML"];
                }
            }
        }
    };
    xhr.open('POST', "/profile", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=getClass");
}

function asyncConnectHelper(url:string, params:string, teamBox:HTMLElement):boolean {
    addSuccessBox(teamBox, "Running...");
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status === 200) { // If an error occurred
                const response:object = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("reload")) window.location.href=response["reload"];
                else if(Object.keys(response).includes("success")) addSuccessBox(teamBox, "SUCCESS: " + response["success"]);
                else addErrorBox(teamBox, response["error"]);
            } else {    // A server error occurred. Show an error message
                addErrorBox(teamBox, config.TEXT.server_error);
            }
        }
    };
    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send(params);
    return false;
}

function saveChanges():boolean {
    addSuccessBox(dom.profile, "Saving...");
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status === 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                // @ts-ignore
                if(Object.keys(response).includes("reload")) {
                    window.location.href=response["reload"];
                // @ts-ignore
                } else if(Object.keys(response).includes("success")) {
                    addSuccessBox(dom.profile, "SUCCESS: " + response["success"]);
                } else {
                    addErrorBox(dom.profile, response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox(dom.profile, config.TEXT.server_error);
            }
        }
    };

    xhr.open('POST', "/profile", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    let schoolText = "";
    if(dom.school != null) schoolText = "&school="+dom.school.value;
    xhr.send("action=saveChanges&fname="+dom.fname.value+"&lname="+dom.lname.value+schoolText+"&oldPassword="+dom.oldPassword.value+"&newPassword="+dom.newPassword.value);
    dom.oldPassword.value = "";
    dom.newPassword.value = "";
    return false;
}

/***
 * Loads in the competitions from the database.
 * */
function loadCompetitions():boolean {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) { // If an error occurred
                const responses:string[] = JSON.parse(xhr.responseText);
                dom.competitions.innerHTML = "<p id='newCompetition' onclick='createNewCompetition()'>New</p>";

                let competitionList: HTMLDivElement = document.createElement("div");
                competitionList.id = "competitionList";
                if(responses.length>0) {
                    competitionList.dataset.hasCompetitions = "true";
                    let first: boolean = true;
                    for(let competition of responses) {
                        let obj:Competition = new Competition(competition["cid"], competition["isPublic"] === "true",
                            competition["name"], competition["written"], competition["handsOn"]);
                        let handsOnProblemsList = [];
                        if(competition["handsOn"] != null) handsOnProblemsList = competition["handsOn"]["problems"];
                        let dom:HTMLElement = obj.getDOM(handsOnProblemsList, competition["whatItIs"], competition["rules"]);
                        if(first) {
                            visible_competition_edit_dom = obj.dom.comp_edit;
                        }
                        competitionList.appendChild(dom);
                    }
                } else {
                    competitionList.innerHTML = "Click 'New' to create a competition.";
                    competitionList.dataset.hasCompetitions = "false";
                }
                dom.competitions.appendChild(competitionList);
            }
        }
    };
    xhr.open('GET', "/profile?action=getCompetitions", true);
    xhr.send(null);
    return false;
}

/***
 * Shows a competition's edit html
 */
let visible_competition_edit_dom;
function toggleEditCompetition(competition:Competition) {
    let newCompEdit:HTMLElement = competition.dom.comp_edit;
    if(visible_competition_edit_dom != null) visible_competition_edit_dom.style.display = "none";

    if(visible_competition_edit_dom != newCompEdit) {
        visible_competition_edit_dom = newCompEdit;
        visible_competition_edit_dom.style.display = "grid";
    } else {
        visible_competition_edit_dom = null;
    }
}

/***
 * Creates a new competition and opens it up for editing
 */
function createNewCompetition():void {
    let competition:Competition = new Competition("",false, "New Competition", false, false);

    if(dom.competitionList.dataset.hasCompetitions === "false") {
        dom.competitionList.dataset.hasCompetitions = "true";
        dom.competitionList.innerHTML = "";
    }

    dom.competitionList.appendChild(competition.getDOM([], "", ""));
    toggleEditCompetition(competition);
}

function hideDelUser():void {
    document.getElementById("delUserPasswordCnt").style.display = "none";
}

function delUser():boolean {
    asyncConnectHelper("delete-user", "delUserPass="+dom.delUserPassword.value, dom.profile);
    dom.delUserPassword.value = "";
    hideDelUser();
    return false;
}


function addErrorBox(box:HTMLElement, error:string): void{
    let errorBox:HTMLElement = document.getElementById(box.id + "ERROR");
    if(!errorBox) {
        box.insertAdjacentHTML('afterbegin', "<div class='error' id='" + box.id + "ERROR'>ERROR: " + error + "</div>");
    }
    else {
        errorBox.innerHTML = "ERROR: " + error;
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


function navTo(element:HTMLElement) {
    if(pageState.selectedNav != null) {
        pageState.selectedNav.classList.remove("selected");
        document.getElementById(pageState.selectedNav.innerText).style.display = "none";
    }

    pageState.selectedNav = element;
    pageState.selectedNav.classList.add("selected");

    let dom = document.getElementById(pageState.selectedNav.innerText);
    if(pageState.selectedNav.innerText === "Competitions") dom.style.display = "block";
    else dom.style.display = "grid";
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
                    dom.class.innerHTML = response["html"];
                    createLoadClassInterval();
                }
            }
        }
    };
    xhr.open('POST', "/profile", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=joinClass&code=" + classCode_input.value);
}

/***
 * Element is the actual "<span>Kick</span>" element.
 * @param element
 * @param uid
 */
function kickStudent(element:HTMLSpanElement, uid: number) {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.open('POST', "/profile", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=kickStudent&uid=" + uid);

    document.getElementById("studentList").removeChild(element.parentNode);
}

function leaveClass() {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.open('POST', "/profile", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=leaveClass");
    showJoinClass();
    clearInterval(loadClassInterval);
}

function showJoinClass() {
    dom.class.innerHTML = "<div class='profile_cmpnt full'><h2>Join a Class</h2></div><div class='profile_cmpnt half'>Class Code:</div>";
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

