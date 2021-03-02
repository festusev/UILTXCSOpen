///<reference path="../websocket.ts"/>
const config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        class_competitions : "class_competitions",
        selectJudgeCnt : "selectJudgeCnt",
        selectJudgeList : "selectJudgeList"
    },
    COMPETITION: {
        mcOptions : ["a","b","c","d","e"]
    },
    SOCKET_FUNCTIONS: {
        "loadJudges" : function(response: {thisUID: number, judges: {[uid: string]: string}}) {
            let fragment = document.createDocumentFragment();
            for(let judgeUID in response.judges) {
                if(response.thisUID == parseInt(judgeUID)) continue;

                let li = document.createElement("li");
                li.classList.add("judge");
                li.innerText = response.judges[judgeUID];
                li.onclick = function() {
                    if(pageState.editingComp) pageState.editingComp.addJudge([parseInt(judgeUID), response.judges[judgeUID]]);
                    closeSelectJudge();
                };
                fragment.appendChild(li);
            }

            dom.selectJudgeList.innerHTML = "";
            dom.selectJudgeList.appendChild(fragment);
        }
    },
    TEMPLATES: {
        cs : {
            writtenExists: true,
            handsOnExists: true,

            alternateExists: true,
            numNonAlts: 3,

            written: {
                numProblems : 40,
                correctPoints: 6,
                incorrectPoints: -2,
                instructions: "You have 45 minutes to complete the 40 question written portion. You will be quized on computer science principles, syntax, and more.",
                time: 45
            },

            handsOn: {
                maxPoints: 60,
                incorrectPenalty: -5,
                numProblems: 12,
                time: 120
            }
        },
        numbersense : {
            writtenExists: true,
            handsOnExists: false,

            alternateExists: false,
            numNonAlts: 3,

            written: {
                numProblems : 80,
                correctPoints: 1,
                incorrectPoints: 0,
                instructions: "You have 10 minutes to complete this 80 question mental math test covering all high school mathematics courses.",
                time: 10
            },

            handsOn: {
                maxPoints: 60,
                incorrectPenalty: -5,
                numProblems: 1,
                time: 120
            }
        },
        calculatorapplications : {
            writtenExists: true,
            handsOnExists: false,

            alternateExists: false,
            numNonAlts: 3,

            written: {
                numProblems : 70,
                correctPoints: 1,
                incorrectPoints: 0,
                instructions: "You have 30 minutes to complete this 70 question mathematics test. You may use a handheld calculator.",
                time: 30
            },

            handsOn: {
                maxPoints: 60,
                incorrectPenalty: -5,
                numProblems: 1,
                time: 120
            }
        },
        mathematics : {
            writtenExists: true,
            handsOnExists: false,

            alternateExists: false,
            numNonAlts: 3,

            written: {
                numProblems : 60,
                correctPoints: 1,
                incorrectPoints: 0,
                instructions: "You have 40 minutes to complete this 60 question mathematics test, testing knowledge from algebra 1 to elementary calculus.",
                time: 40
            },

            handsOn: {
                maxPoints: 60,
                incorrectPenalty: -5,
                numProblems: 1,
                time: 120
            }
        }
    }
};


let pageState = {
    editingComp : null  // The competition we are currently editing
};


/***
 * Helps interfacing with static elements (are not deleted *
 */
let dom = {
    cached : {},    // DOM objects that have already been accessed
    getHelper(id): HTMLElement {
        if(this.cached[id] == null) this.cached[id] = document.getElementById(id);
        return this.cached[id];
    },
    get class_competitions(){return this.getHelper(config.IDs.class_competitions)},
    get selectJudgeCnt(){return this.getHelper(config.IDs.selectJudgeCnt);},
    get selectJudgeList(){return this.getHelper(config.IDs.selectJudgeList)}
};

(function() {
    getWebSocket(window.location.host + "/console/sockets/uil_list", config.SOCKET_FUNCTIONS);
})();

function showPublic(nav:HTMLElement) {
    let nodes = document.getElementById("nav").getElementsByTagName("p");
    for(let i=0;i<nodes.length;i++) {
        nodes[i].classList.remove("selected");
    }
    nav.classList.add("selected");
    document.getElementById("public_competitions").style.display = "block";
    try {
        document.getElementById("class_competitions").style.display = "none";
    } catch(e) {}

    try {
        document.getElementById("upcoming_competitions").style.display = "none";
    } catch(e) {}
}

function showClassComps(nav:HTMLElement) {
    let nodes = document.getElementById("nav").getElementsByTagName("p");
    for(let i=0;i<nodes.length;i++) {
        nodes[i].classList.remove("selected");
    }
    if(nav == null) document.getElementById("showClassComps").classList.add("selected");
    else nav.classList.add("selected");
    document.getElementById("public_competitions").style.display = "none";
    try {
        document.getElementById("class_competitions").style.display = "block";
    } catch(e) {}
    try {
        document.getElementById("upcoming_competitions").style.display = "none";
    } catch(e) {}
}

/***
 * When the teacher clicks the edit icon from a mini html, switch to the corresponding one in their class and open it for editing.
 */
function editCompFromMini(cid) {
    if(cid >= 0) return;
    for(let competition of competitions) {
        if(competition.cid === cid) {
            showClassComps(null);
            toggleEditCompetition(competition);
        }
    }
}
function addPreventDefault(element:HTMLElement) {
    element.onclick = function(event) {
        event.preventDefault();
    }
}
function showUpcomingComps(nav:HTMLElement) {
    let nodes = document.getElementById("nav").getElementsByTagName("p");
    for(let i=0;i<nodes.length;i++) {
        nodes[i].classList.remove("selected");
    }
    nav.classList.add("selected");
    document.getElementById("public_competitions").style.display = "none";
    try {
        document.getElementById("class_competitions").style.display = "none";
    } catch(e) {}
    try {
        document.getElementById("upcoming_competitions").style.display = "block";
    } catch(e) {}
}

interface Template {
    writtenExists: boolean;
    handsOnExists: boolean;

    alternateExists: boolean;
    numNonAlts: number;

    written:{
        numProblems:number,
        correctPoints:number,
        incorrectPoints:number,
        instructions:string,
        time:number
    };

    handsOn:{
        maxPoints:number,
        incorrectPenalty:number,
        numProblems:number,
        time:number
    }
}

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
    hasInput: boolean;
    hasOutput: boolean;
    /* If this problem has been saved to the server, oldIndex stores the index of this problem in the server.
    *  If it has not been saved, it is set to -1, and when it is saved it will be overwritten to be the index
    *  at the time of saving */
    oldIndex : number = -1;
}

let competitions: Competition[] = [];

class Competition {
    published : boolean;
    cid : string;
    name: string;

    isPublic: boolean;
    writtenExists: boolean;
    handsOnExists: boolean;

    judges: [number, string][]; // The first element is the uid of the teacher, the second is the name of the teacher

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
        comp_head_name: HTMLSpanElement,
        comp_edit: HTMLDivElement,
        compName : HTMLInputElement,
        compPublic : HTMLInputElement,
        // viewCompetition : HTMLElement,
        errorSuccessBox: HTMLElement,   // The box for errors and successes

        judges_list : HTMLElement,

        controls: HTMLElement,
        controlsEdit: HTMLDivElement,
        controlsOpen: HTMLDivElement,
        controlsSave: HTMLDivElement,
        controlsDelete: HTMLDivElement,

        description : HTMLTextAreaElement,
        rules : HTMLTextAreaElement,

        altExists : HTMLInputElement,
        numNonAlts : HTMLInputElement,

        writtenSection: HTMLSpanElement,
        writtenOpen: HTMLInputElement,
        writtenTime : HTMLInputElement,
        writtenKey: HTMLElement,
        writtenAnswerList: HTMLElement,
        writtenInstructionCnt: HTMLTextAreaElement,
        writtenTestLink : HTMLInputElement,
        writtenPointsPerCorrect : HTMLInputElement,
        writtenPointsPerIncorrect : HTMLInputElement,
        writtenCheckbox : HTMLInputElement,

        handsOnSection: HTMLSpanElement,
        handsOnStart : HTMLInputElement,
        handsOnTime : HTMLInputElement,
        handsOnProblems: HTMLElement,
        handsOnStudentPacket : HTMLInputElement,
        handsOnMaxPoints : HTMLInputElement,
        handsOnIncorrectPenalty : HTMLInputElement,
        handsOnCheckbox: HTMLInputElement,
        list_handsOn_changeproblems : HTMLElement
    } = {
        form: null,
        comp_head: null,
        comp_head_name: null,
        comp_edit: null,
        compName : null,
        compPublic : null,
        // viewCompetition : null,
        errorSuccessBox: null,

        controls: null,
        controlsEdit: null,
        controlsOpen: null,
        controlsSave: null,
        controlsDelete: null,

        description : null,
        rules : null,

        altExists : null,
        numNonAlts : null,

        writtenSection: null,
        writtenOpen: null,
        writtenTime : null,
        writtenKey: null,
        writtenAnswerList: null,
        writtenInstructionCnt: null,
        writtenTestLink : null,
        writtenPointsPerCorrect : null,
        writtenPointsPerIncorrect : null,
        writtenCheckbox : null,

        handsOnSection: null,
        handsOnStart : null,
        handsOnTime : null,
        handsOnProblems: null,
        handsOnStudentPacket : null,
        handsOnMaxPoints : null,
        handsOnIncorrectPenalty : null,
        handsOnCheckbox : null,
        list_handsOn_changeproblems : null
    };

    constructor(cid: string, published: boolean, isPublic: boolean, name:string, judges:[number, string][], writtenObj:any, handsOnObj:any) {
        competitions.push(this);

        this.cid = cid;
        this.published = published;
        this.isPublic = isPublic;
        this.name = name;
        this.judges = judges;
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

    toggleWrittenKey(h2:HTMLHeadElement) {
        if(this.dom.writtenKey.style.display !== "block") {
            this.dom.writtenKey.style.display = "block";
            h2.innerHTML = "Answers<span style='float:right;font-weight:bold;'>-</span>";
        } else {
            this.dom.writtenKey.style.display = "none";
            h2.innerHTML = "Answers<span style='float:right;font-weight:bold;'>+</span>";
        }
    }

    toggleHandsOnProblems(h2:HTMLHeadElement) {
        if(this.dom.handsOnProblems.style.display !== "block") {
            this.dom.handsOnProblems.style.display = "block";
            h2.innerHTML = "Problems<span style='float:right;font-weight:bold;'>-</span>";
        } else {
            this.dom.handsOnProblems.style.display = "none";
            h2.innerHTML = "Problems<span style='float:right;font-weight:bold;'>+</span>";
        }
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

    toggleWrittenInstructions(h2:HTMLHeadElement) {
        if(this.dom.writtenInstructionCnt.style.display === "none") {
            this.dom.writtenInstructionCnt.style.display = "block";
            h2.innerHTML = "Instructions<span style='float:right;font-weight:bold'>-</span>"
        } else {
            this.dom.writtenInstructionCnt.style.display = "none";
            this.written.instructions = this.dom.writtenInstructionCnt.value;
            h2.innerHTML = "Instructions<span style='float:right;font-weight:bold'>+</span>"
        }
    }

    delete() {
        if(this.cid.length !== 0) {  /* This competition has already been saved */
            let xhr: XMLHttpRequest = new XMLHttpRequest();
            xhr.open('POST', "/console/competitions", true);
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            xhr.send("action=deleteCompetition&op_cid=" + this.cid);
        }
        dom.class_competitions.removeChild(this.dom.form);
        competitions.splice(competitions.indexOf(this),1);

        if(competitions.length <= 0) {
            dom.class_competitions.innerHTML = "Click 'New' to create a competition.";
        }
    }

    applyTemplate(template: Template) {
        this.dom.numNonAlts.value = "" + template.numNonAlts;

        if(template.handsOnExists != this.handsOnExists) {
            this.toggleHandsOnTest();
            this.dom.handsOnCheckbox.checked = this.handsOnExists;
        }
        if(template.writtenExists != this.writtenExists) {
            this.toggleWrittenTest();
            this.dom.writtenCheckbox.checked = this.writtenExists;
        }

        if(template.handsOnExists) this.dom.altExists.checked = template.alternateExists;

        this.handsOn.incorrectPenalty = template.handsOn.incorrectPenalty;
        this.dom.handsOnIncorrectPenalty.value = "" + template.handsOn.incorrectPenalty;

        this.handsOn.maxPoints = template.handsOn.maxPoints;
        this.dom.handsOnMaxPoints.value = "" + template.handsOn.maxPoints;

        this.handsOn.time = template.handsOn.time;
        this.dom.handsOnTime.value = "" + template.handsOn.time;

        if(this.handsOn.problemMap.length < template.handsOn.numProblems) {
            for(let i=this.handsOn.problemMap.length+1,j=template.handsOn.numProblems;i<=j;i++) {
                this.addHandsOnProblem(-1, "", false, false);
            }
        }

        this.written.instructions = template.written.instructions;
        this.dom.writtenInstructionCnt.value = template.written.instructions;   // writtenInstructionCnt is a textarea

        this.written.correctPoints = template.written.correctPoints;
        this.dom.writtenPointsPerCorrect.value = "" + template.written.correctPoints;

        this.written.incorrectPoints = template.written.incorrectPoints;
        this.dom.writtenPointsPerIncorrect.value = "" + template.written.incorrectPoints;

        this.written.time = template.written.time;
        this.dom.writtenTime.value = "" + template.written.time;

        if(this.written.key.length < template.written.numProblems) {
            for(let i=this.written.key.length+1,j=template.written.numProblems;i<=j;i++) {
                this.addWrittenQuestion(null);
            }
        }
    }

    getFormData(): FormData {
        /* First, set the name and open date in the title */
        this.dom.comp_head_name.innerHTML =  this.dom.compName.value;
        let thisComp:Competition = this;  // So that it can be referenced in other functions

        /*let deleteCompetition = document.createElement("p");
        deleteCompetition.innerText = "Delete";
        deleteCompetition.classList.add("deleteCompetition");
        deleteCompetition.onclick = function() {
            thisComp.delete();
        };
        this.dom.comp_head.appendChild(deleteCompetition);*/

        let formData:FormData = new FormData();

        formData.append("op_cid", this.cid);    // The CID we are operating on
        formData.append("name", this.dom.compName.value);
        formData.append("isPublic", ""+this.dom.compPublic.checked);
        formData.append("numNonAlts", ""+this.dom.numNonAlts.value);

        formData.append("description", this.dom.description.value);

        let judgeUIDs: number[] = [];
        for(let judgeUID of this.judges) {
            judgeUIDs.push(judgeUID[0]);
        }
        formData.append("judges", JSON.stringify(judgeUIDs))
        formData.append("writtenExists", ""+this.writtenExists);

        if(this.writtenExists) {
            formData.append("mcOpens", this.dom.writtenOpen.value);
            formData.append("mcTime", ""+this.dom.writtenTime.value);
            formData.append("mcCorrectPoints", ""+this.dom.writtenPointsPerCorrect.value);
            formData.append("mcIncorrectPoints", ""+this.dom.writtenPointsPerIncorrect.value);
            formData.append("mcInstructions", this.dom.writtenInstructionCnt.value);
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
            formData.append("alternateExists", ""+this.dom.altExists.checked);

            let problems:Array<[string, boolean, boolean]> = [];
            let problemIndices:number[] = [];   /* A list like [1, 2, -1, 9, 5], corresponding to a name in the problems array */
            for(let i=0,j=this.handsOn.problemMap.length;i<j;i++) {
                let problem:HandsOnProblem = this.handsOn.problemMap[i];
                problems.push([problem.dom.name.value, problem.hasInput, problem.hasOutput]);
                problemIndices.push(problem.oldIndex);
                problem.oldIndex = i; /* Now that the problem has been saved, we set it to be its current index */

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
        return formData;
    }

    saveCompetition():boolean {
        // Remove the error box
        try {this.dom.comp_edit.removeChild(document.getElementById("ERROR"));}
        catch (e){}

        let formData:FormData = this.getFormData();
        let thisComp = this;

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
                        thisComp.dom.errorSuccessBox = addSuccessBox(thisComp.dom.comp_edit, response["success"], thisComp.dom.errorSuccessBox);
                        if(thisComp.cid.length === 0 && response["cid"] != null) thisComp.cid = response["cid"];
                        // thisComp.dom.viewCompetition.onclick = function(){window.location.href = "/console/competitions?cid="+thisComp.cid;};
                    } else if(response["error"] != null) {    // An error occurred
                        thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, response["error"], thisComp.dom.errorSuccessBox);
                    } else {
                        thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error, thisComp.dom.errorSuccessBox);
                    }
                } else {
                    thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error, thisComp.dom.errorSuccessBox);
                }
            }
        };
        xhr.open('POST', "/console/competitions", true);
        xhr.send(formData);
        return false;
    }

    unPublishCompetition(callback:Function) {
        try {this.dom.comp_edit.removeChild(document.getElementById("ERROR"));}
        catch (e){}

        this.published = false;

        let thisComp:Competition = this;
        let formData = new FormData();
        formData.append("action", "unPublishCompetition");
        formData.append("cid", this.cid);
        let xhr:XMLHttpRequest = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    let response = JSON.parse(xhr.responseText);
                    if(response["success"] != null) {
                        thisComp.dom.errorSuccessBox = addSuccessBox(thisComp.dom.comp_edit, response["success"], thisComp.dom.errorSuccessBox);
                        callback();
                    } else {
                        thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error, thisComp.dom.errorSuccessBox);
                    }
                }
            }
        };
        xhr.open('POST', "/console/competitions", true);
        xhr.send(formData);
        return false;
    }

    publishCompetition(callback:Function, errorCallback?:Function) {
        // Remove the error box
        try {this.dom.comp_edit.removeChild(document.getElementById("ERROR"));}
        catch (e){}

        let formData:FormData = this.getFormData();
        let thisComp = this;

        formData.append("action", "publishCompetition");
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
                        thisComp.dom.errorSuccessBox = addSuccessBox(thisComp.dom.comp_edit, response["success"], thisComp.dom.errorSuccessBox);
                        if(thisComp.cid.length === 0 && response["cid"] != null) thisComp.cid = response["cid"];
                        // thisComp.dom.comp_head.onclick = function(event){event.stopPropagation();window.location.href = "/console/competitions?cid="+thisComp.cid;};

                        let openComp:HTMLElement = thisComp.getOpenCompetition();

                        // Hide the openComp control if the controls are currently closed
                        if(visible_competition_edit_dom != thisComp.dom.comp_edit) openComp.style.display = "none";

                        thisComp.dom.controls.insertBefore(thisComp.getOpenCompetition(), thisComp.dom.controlsSave);
                        thisComp.published = true;
                        callback();
                    } else if(response["error"] != null) {    // An error occurred
                        if(errorCallback) errorCallback();
                        thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, response["error"], thisComp.dom.errorSuccessBox);
                    } else {
                        if(errorCallback) errorCallback();
                        thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error, thisComp.dom.errorSuccessBox);
                    }
                } else {
                    if(errorCallback) errorCallback();
                    thisComp.dom.errorSuccessBox = addErrorBox(thisComp.dom.comp_edit, config.TEXT.server_error, thisComp.dom.errorSuccessBox);
                }
            }
        };
        xhr.open('POST', "/console/competitions", true);
        xhr.send(formData);
    }

    addWrittenQuestion(writtenProblem:WrittenProblem) {
        if(this.written.key.length >= 240) return;   // No more dthan 240 questions.

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
        newInput.placeholder = "Answer";
        newInput.maxLength = 40;
        newInput.oninput = function() {
            let newValue:string = newInput.value;
            writtenProblem.answer = newValue;

            if(newValue.length>0 && !config.COMPETITION.mcOptions.includes(newValue)) {  /* Switch to SAQ */
                writtenProblem.type = WrittenType.SAQ;
                newP.selectedIndex = 1;
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
            } else if(newInput.value.length === 0 || config.COMPETITION.mcOptions.includes(newInput.value)) { /* It must be a valid multiple choice option */
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
    /*if(thisComp.handsOn.problemMap.length >= 50) return;
    let index:number = thisComp.handsOn.problemMap.length;
    let problem:HandsOnProblem = new HandsOnProblem();
    problem.oldIndex = probIndex;
    thisComp.handsOn.problemMap.push(problem);

    let li = document.createElement("li");
    li.dataset.probNum = "" + index;

    let input_name = document.createElement("input");
    input_name.type="text";
    input_name.placeholder = "Problem Name";
    input_name.maxLength = 20;
    input_name.classList.add("handsOn_probName");
    li.appendChild(input_name);
    problem.dom.name = input_name;

    /* Each input has the actual input field that takes in the file but is hidden to the user, as well
    *  as an alias button that serves as the proxy between the user and the input field. This is to prevent
    *  weird file input styling. */
    /*let input_in_hidden = document.createElement("input");
    input_in_hidden.type = "file";
    input_in_hidden.style.display = "none";
    li.appendChild(input_in_hidden);
    problem.dom.input = input_in_hidden;
    input_in_hidden.onchange = function() {
        problem.hasInput = true;
        input_in_proxy.innerText = "Change";
    };

    let input_in_proxy = document.createElement("button");
    input_in_proxy.classList.add("handsOn_probIn");
    input_in_proxy.innerText = "Input";
    li.appendChild(input_in_proxy);
    input_in_proxy.onclick = function(){input_in_hidden.click();};

    let input_out_hidden = document.createElement("input");
    input_out_hidden.type = "file";
    input_out_hidden.style.display = "none";
    li.appendChild(input_out_hidden);
    problem.dom.output = input_out_hidden;
    input_out_hidden.onchange = function(){
        problem.hasOutput = true;
        input_out_proxy.innerText = "Change";
    };

    let input_out_proxy = document.createElement("button");
    input_out_proxy.classList.add("handsOn_probOut");
    input_out_proxy.innerText = "Output";
    input_out_proxy.onclick = function(){input_out_hidden.click();};
    li.appendChild(input_out_proxy);

    if(handsOnProblemMap.length > index) {
    input_name.value = handsOnProblemMap[index][0];
    problem.hasInput = handsOnProblemMap[index][1];
    problem.hasOutput = handsOnProblemMap[index][2];

    if(problem.hasInput) {
    input_in_proxy.innerText = "Change";
}
if(problem.hasOutput) {
    input_out_proxy.innerText = "Change";
}
} else {
    input_name.value = "";
    problem.hasInput = false;
    problem.hasOutput = false;
}

let delQuestion = document.createElement("img");
delQuestion.src = "/res/close.svg";
delQuestion.classList.add("deleteProblem");
delQuestion.onclick = function() {
    thisComp.handsOn.problemMap.splice(thisComp.handsOn.problemMap.indexOf(problem), 1);
    list_handsOn_changeproblems.removeChild(li);
};
li.appendChild(delQuestion);

list_handsOn_changeproblems.appendChild(li);
*/

    addHandsOnProblem(probIndex: number, name: string, hasInputFile: boolean, hasOutputFile: boolean): void {
        let thisComp:Competition = this;

        if(this.handsOn.problemMap.length >= 24) return;    // Don't let them have more than 24 hands on problems
        let index:number = this.handsOn.problemMap.length;
        let problem:HandsOnProblem = new HandsOnProblem();
        problem.oldIndex = probIndex;
        this.handsOn.problemMap.push(problem);

        let li = document.createElement("li");
        li.dataset.probNum = "" + index;

        let input_name = document.createElement("input");
        input_name.type="text";
        input_name.placeholder = "Problem Name";
        input_name.maxLength = 20;
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
        input_in_hidden.onchange = function() {
            problem.hasInput = true;
            input_in_proxy.innerText = "Change";
        };

        let input_in_proxy = document.createElement("button");
        input_in_proxy.classList.add("handsOn_probIn");
        input_in_proxy.innerText = "Input";
        li.appendChild(input_in_proxy);
        input_in_proxy.onclick = function(){input_in_hidden.click();};

        let input_out_hidden = document.createElement("input");
        input_out_hidden.type = "file";
        input_out_hidden.style.display = "none";
        li.appendChild(input_out_hidden);
        problem.dom.output = input_out_hidden;
        input_out_hidden.onchange = function(){
            problem.hasOutput = true;
            input_out_proxy.innerText = "Change";
        };

        let input_out_proxy = document.createElement("button");
        input_out_proxy.classList.add("handsOn_probOut");
        input_out_proxy.innerText = "Output";
        input_out_proxy.onclick = function(){input_out_hidden.click();};
        li.appendChild(input_out_proxy);

        input_name.value = name;
        problem.hasInput = hasInputFile;
        problem.hasOutput = hasOutputFile;

        if(problem.hasInput) {
            input_in_proxy.innerText = "Change";
        }
        if(problem.hasOutput) {
            input_out_proxy.innerText = "Change";
        }

        let delQuestion = document.createElement("img");
        delQuestion.src = "/res/close.svg";
        delQuestion.classList.add("deleteProblem");
        delQuestion.onclick = function() {
            thisComp.handsOn.problemMap.splice(thisComp.handsOn.problemMap.indexOf(problem), 1);
            thisComp.dom.list_handsOn_changeproblems.removeChild(li);
        };
        li.appendChild(delQuestion);

        thisComp.dom.list_handsOn_changeproblems.appendChild(li);
    }


    getOpenCompetition(): HTMLElement {
        let thisComp:Competition = this;
        let controls_open = document.createElement("div");
        controls_open.classList.add("tooltip-cnt");
        controls_open.innerHTML = "<img src='/res/console/open.svg'/><p class='tooltip'>Open</p>";
        controls_open.onclick = function() {
            if(thisComp.published) window.location.href = "/console/competitions?cid="+thisComp.cid;
        };
        this.dom.controlsOpen = controls_open;
        return controls_open;
    }

    deleteJudge(judge: [number, string], li: HTMLLIElement) {
        for(let i=0,j=this.judges.length;i<j;i++) {
            if(this.judges[i][0] == judge[0]) {
                this.judges.splice(i, 1);
                this.dom.judges_list.removeChild(li);
                return;
            }
        }
    }

    addJudge(judge: [number, string]) {
        for(let existingJudge of this.judges) {
            if(existingJudge[0] == judge[0]) return;    // Don't add in a judge if they are already assigned to this competition
        }
        // console.log(this.judges);

        this.judges.push(judge);
        this.dom.judges_list.appendChild(this.getJudgeDOM(judge));
    }

    getJudgeDOM(judge: [number, string]): HTMLLIElement {    // First element is the uid, second is the username
        let thisComp: Competition = this;

        let li = document.createElement("li");
        li.classList.add("judge");
        li.innerText = judge[1];

        let del = document.createElement("img");
        del.src = "/res/console/delete.svg";
        del.onclick = function() {
            thisComp.deleteJudge(judge, li);
        };
        li.appendChild(del);
        return li;
    }

    getDOM(handsOnProblemMap:[string, boolean, boolean][], whatItIsText:string, rulesText:string,numNonAlts: number,alternateExists:boolean):HTMLElement {
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
            makeFull(written_open);
            written_section.appendChild(written_open);

            let h2_written_open = document.createElement("h3");
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
            makeFull(written_length);
            written_section.appendChild(written_length);

            let h2_written_length = document.createElement("h3");
            h2_written_length.innerText = "Length";
            written_length.appendChild(h2_written_length);

            let input_written_length = document.createElement("input");
            input_written_length.name = "mcTime";
            input_written_length.min = "1";
            input_written_length.max = "16777215";
            /*input_written_length.oninput = function() {
                numberInputCheckMaxValue(input_written_length, 1, 16777215);
            };*/
            input_written_length.type = "number";
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
            written_answers.onclick = function() {thisComp.toggleWrittenKey(h2_written_answers);};
            written_section.appendChild(written_answers);
            written_answers.style.cursor = "pointer";

            let h2_written_answers = document.createElement("h3");
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
            written_instructions.onclick = function(){thisComp.toggleWrittenInstructions(h2_written_instructions);};
            written_section.appendChild(written_instructions);
            written_instructions.style.cursor = "pointer";

            let h2_written_instructions = document.createElement("h3");
            h2_written_instructions.innerHTML = "Instructions<span style='float:right;font-weight:bold'>+</span>";
            written_instructions.appendChild(h2_written_instructions);
            /* CLOSE */

            /* OPEN */
            /*let written_instructions_change = document.createElement("div");
            makeFull(written_instructions_change);
            written_instructions_change.classList.add("competitionSubgroup");
            written_instructions_change.classList.add("changeWrittenInstructions");
            written_instructions_change.style.display = "none";
            // written_instructions_change.innerHTML = "<p>Instructions</p>";
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
            makeFull(textarea_written_instructions_change);
            textarea_written_instructions_change.style.display = "none";
            textarea_written_instructions_change.maxLength = 255;
            textarea_written_instructions_change.oninput = function() {
                inputMaxLength(textarea_written_instructions_change);
            };
            written_section.appendChild(textarea_written_instructions_change);
            textarea_written_instructions_change.value = thisComp.written.instructions;
            thisComp.dom.writtenInstructionCnt = textarea_written_instructions_change;
            /* CLOSE */

            /* OPEN */
            let written_testlink = document.createElement("div");
            makeFull(written_testlink);
            written_section.appendChild(written_testlink);

            let h2_written_testlink = document.createElement("h3");
            h2_written_testlink.innerText = "Written Test Link";
            written_testlink.appendChild(h2_written_testlink);

            let input_written_testLink = document.createElement("input");
            input_written_testLink.name = "mcTestLink";
            input_written_testLink.type = "url";
            input_written_testLink.placeholder = "https://what_the_student_sees.com";
            input_written_testLink.value = thisComp.written.testLink?thisComp.written.testLink:"";
            input_written_testLink.maxLength = 255;
            input_written_testLink.oninput = function() {
                inputMaxLength(input_written_testLink);
            };
            written_testlink.appendChild(input_written_testLink);
            thisComp.dom.writtenTestLink = input_written_testLink;
            /* CLOSE */

            /* OPEN */
            let written_ppc = document.createElement("div");
            makeHalf(written_ppc);
            written_section.appendChild(written_ppc);

            let h2_written_ppc = document.createElement("h3");
            h2_written_ppc.innerText = "Points Per Correct Problem";
            written_ppc.appendChild(h2_written_ppc);

            let input_written_ppc:HTMLInputElement = document.createElement("input");
            input_written_ppc.name = "mcCorrectPoints";
            input_written_ppc.type = "number";
            input_written_ppc.placeholder = "6";
            input_written_ppc.min = "0";
            input_written_ppc.max = "127";
            input_written_ppc.step = "1";
            /*input_written_ppc.oninput = function() {
                numberInputCheckMaxValue(input_written_ppc, 0, 127);
            };*/
            input_written_ppc.value = ""+thisComp.written.correctPoints?""+thisComp.written.correctPoints:"";
            written_ppc.appendChild(input_written_ppc);
            thisComp.dom.writtenPointsPerCorrect = input_written_ppc;
            /* CLOSE */

            /* OPEN */
            let written_ppi = document.createElement("div");
            makeHalf(written_ppi);
            written_section.appendChild(written_ppi);

            let h2_written_ppi = document.createElement("h3");
            h2_written_ppi.innerText = "Points Per Incorrect Problem";
            written_ppi.appendChild(h2_written_ppi);

            let input_written_ppi = document.createElement("input");
            input_written_ppi.name = "mcIncorrectPoints";
            input_written_ppi.type = "number";
            input_written_ppi.placeholder = "-2";
            input_written_ppi.max = "0";
            input_written_ppi.min = "-128";
            input_written_ppi.step = "1";
            /*input_written_ppi.oninput = function() {
                numberInputCheckMaxValue(input_written_ppi, -128, 0);
            };*/
            input_written_ppi.value = ""+thisComp.written.incorrectPoints?""+thisComp.written.incorrectPoints:"";
            written_ppi.appendChild(input_written_ppi);
            thisComp.dom.writtenPointsPerIncorrect = input_written_ppi;
            /* CLOSE */

            return written_section;
        }
        function getHandsOnSection():HTMLElement {
            let handsOn_section = document.createElement("span");
            handsOn_section.classList.add("handsOnSection");
            if(!thisComp.handsOnExists) handsOn_section.style.display = "none";
            written_toggle.appendChild(handsOn_section);
            thisComp.dom.handsOnSection = handsOn_section;

            /* OPEN */
            let alternateExists_header = document.createElement("div");
            makeHalf(alternateExists_header);
            handsOn_section.appendChild(alternateExists_header);

            let h2_alternateExists_header = document.createElement("h3");
            h2_alternateExists_header.innerHTML = "Allow Written Specialist";
            alternateExists_header.appendChild(h2_alternateExists_header);
            /* CLOSE */

            /* OPEN */
            let alternateExists_toggle = document.createElement("div");
            handsOn_section.appendChild(alternateExists_toggle);

            let alternateExists_toggle_input = document.createElement("input");
            alternateExists_toggle_input.classList.add("checkbox");
            alternateExists_toggle_input.type = "checkbox";
            alternateExists_toggle_input.name = "altExists";
            alternateExists_toggle_input.onclick = function(){
                thisComp.isPublic = alternateExists_toggle_input.checked;
            };
            if(alternateExists) alternateExists_toggle_input.checked = true;
            alternateExists_toggle.appendChild(alternateExists_toggle_input);
            thisComp.dom.altExists = alternateExists_toggle_input;
            /* CLOSE */

            /* OPEN */
            let handsOn_start = document.createElement("div");
            makeFull(handsOn_start);
            handsOn_start.innerHTML = "<h3>Start</h3>";
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
            makeFull(handsOn_length);
            handsOn_length.innerHTML = "<h3>Length</h3>";
            handsOn_section.appendChild(handsOn_length);

            let input_handsOn_length = document.createElement("input");
            input_handsOn_length.name = "frqTime";
            input_handsOn_length.type = "number";
            input_handsOn_length.classList.add("length");
            input_handsOn_length.value = ""+thisComp.handsOn.time?""+thisComp.handsOn.time:"120";
            input_handsOn_length.min = "1";
            input_handsOn_length.max = "16777215";
            /*input_handsOn_length.oninput = function() {
                numberInputCheckMaxValue(input_handsOn_length, 1, 16777215);
            };*/
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
            handsOn_problems.onclick = function(){thisComp.toggleHandsOnProblems(handsOn_problems);};
            handsOn_problems.innerHTML = "Problems<span style='float:right;font-weight:bold;'>+</span>";
            handsOn_problems.style.cursor = "pointer";
            handsOn_section.appendChild(handsOn_problems);
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

            let list_handsOn_changeproblems = document.createElement("ol");
            list_handsOn_changeproblems.classList.add("handsOnProblemList");
            thisComp.dom.list_handsOn_changeproblems = list_handsOn_changeproblems;
            handsOn_changeproblems.appendChild(list_handsOn_changeproblems);
            if(thisComp.handsOnExists) {
                thisComp.handsOn.problemMap.length = 0;
                for(let i=0, j=handsOnProblemMap.length;i<j;i++) {
                    thisComp.addHandsOnProblem(i, handsOnProblemMap[i][0], handsOnProblemMap[i][1], handsOnProblemMap[i][2]);
                }
            }

            let add_handsOn_changeproblems = document.createElement("button");
            add_handsOn_changeproblems.innerText = "Add";
            add_handsOn_changeproblems.onclick = function() {
                thisComp.addHandsOnProblem(-1, "", false, false);
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
            input_handsOn_studentlink.maxLength = 255;
            input_handsOn_studentlink.oninput = function() {
                inputMaxLength(input_handsOn_studentlink);
            };
            handsOn_studentlink.appendChild(input_handsOn_studentlink);
            thisComp.dom.handsOnStudentPacket = input_handsOn_studentlink;
            /* CLOSE */

            /* OPEN */
            let handsOn_maxPoints = document.createElement("div");
            makeHalf(handsOn_maxPoints);
            handsOn_maxPoints.innerHTML = "<h3>Maximum Points</h3>";
            handsOn_section.appendChild(handsOn_maxPoints);

            let input_handsOn_maxPoints = document.createElement("input");
            input_handsOn_maxPoints.type = "number";
            input_handsOn_maxPoints.name = "frqMaxPoints";
            input_handsOn_maxPoints.value = ""+thisComp.handsOn.maxPoints?""+thisComp.handsOn.maxPoints:"60";
            input_handsOn_maxPoints.min = "0";
            input_handsOn_maxPoints.max = "127";
            input_handsOn_maxPoints.step = "1";
            /*input_handsOn_maxPoints.oninput = function() {
                numberInputCheckMaxValue(input_handsOn_maxPoints, 0, 127);
            };*/
            handsOn_maxPoints.appendChild(input_handsOn_maxPoints);
            thisComp.dom.handsOnMaxPoints = input_handsOn_maxPoints;
            /* CLOSE */

            /* OPEN */
            let handsOn_incorrectPenalty = document.createElement("div");
            makeHalf(handsOn_incorrectPenalty);
            handsOn_incorrectPenalty.innerHTML = "<h3>Incorrect Submission Penalty</h3>";
            handsOn_section.appendChild(handsOn_incorrectPenalty);

            let input_handsOn_incorrectPenalty:HTMLInputElement = document.createElement("input");
            input_handsOn_incorrectPenalty.name = "frqIncorrectPenalty";
            input_handsOn_incorrectPenalty.type = "number";
            input_handsOn_incorrectPenalty.value = ""+thisComp.handsOn.incorrectPenalty?""+thisComp.handsOn.incorrectPenalty:"-5";
            input_handsOn_incorrectPenalty.min = "-128";
            input_handsOn_incorrectPenalty.max = "0";
            input_handsOn_incorrectPenalty.step = "1";
            input_handsOn_incorrectPenalty.placeholder = "-5";
            /*input_handsOn_incorrectPenalty.oninput = function() {
                numberInputCheckMaxValue(input_handsOn_incorrectPenalty, -128, 0);
            };*/

            handsOn_incorrectPenalty.appendChild(input_handsOn_incorrectPenalty);
            thisComp.dom.handsOnIncorrectPenalty = input_handsOn_incorrectPenalty;
            /* CLOSE */

            return handsOn_section;
        }

        let thisComp:Competition = this;    /* used because event handlers override the "this" variable */

        let form = document.createElement("div");
        form.classList.add("competition");
        this.dom.form = form;

        let header = document.createElement("div");
        header.onclick = function(event){
            event.stopPropagation();
            toggleEditCompetition(thisComp);
            // if(thisComp.published) window.location.href = "/console/competitions?cid="+thisComp.cid;
        };
        header.classList.add("comp_head");
        form.appendChild(header);
        this.dom.comp_head = header;

        let header_name = document.createElement("span");
        header_name.innerText = this.name;
        this.dom.comp_head_name = header_name;
        header.appendChild(header_name);

        /* OPEN */
        let controls = document.createElement("div");
        controls.classList.add("competition_controls");
        controls.onclick = function(event) {
            event.stopPropagation();
        };
        this.dom.controls = controls;
        header.appendChild(controls);

        let controls_edit = document.createElement("div");
        controls_edit.classList.add("tooltip-cnt");
        controls_edit.classList.add("competition_edit");
        controls_edit.onclick = function() {
            toggleEditCompetition(thisComp);
        };
        controls_edit.innerHTML = "<img src='/res/console/edit.svg'/><p class='tooltip'>Edit</p>";
        this.dom.controlsEdit = controls_edit;
        controls.appendChild(controls_edit);

        if(this.published) {
            let temp = this.getOpenCompetition();
            temp.style.display = "none";
            controls.appendChild(temp);
        }

        let controls_save = document.createElement("div");
        controls_save.classList.add('tooltip-cnt');
        controls_save.onclick = function() {
            event.stopPropagation();
            thisComp.saveCompetition();
        };
        controls_save.style.display = "none";
        controls_save.innerHTML = "<img src='/res/console/save.svg'/><p class='tooltip'>Save</p>";
        this.dom.controlsSave = controls_save;
        controls.appendChild(controls_save);

        let controls_delete = document.createElement("div");
        controls_delete.classList.add("tooltip-cnt");
        controls_delete.innerHTML = "<img src='/res/console/delete.svg'/><p class='tooltip'>Delete</p>";
        controls_delete.onclick = function(event) {
            event.stopPropagation();
            thisComp.delete();
        };
        controls_delete.style.display = "none";
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

        let publishCompetition = document.createElement("p");
        if(this.published) {
            publishCompetition.innerText = "Published";
            publishCompetition.style.backgroundColor = "unset";
            publishCompetition.style.color = "var(--body-col)";
            form.classList.add("published");
        } else {
            publishCompetition.innerText = "Publish";

            let blockPublication: boolean = false;  // Whether or not this is currently publishing, so block the publication
            publishCompetition.onclick = function(event) {
                event.stopPropagation();
                if(blockPublication) return;
                blockPublication = true;

                thisComp.publishCompetition(function() {
                    publishCompetition.onclick = null;
                    publishCompetition.innerHTML = "Published";
                    publishCompetition.style.backgroundColor = "unset";
                    publishCompetition.style.color = "var(--body-col)";
                    form.classList.add("published");
                }, function() { // The callback if there is an error
                    blockPublication = false;
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

        let body = document.createElement("div");
        body.classList.add("comp_edit");
        body.style.display = "none";
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
        let name_and_save = document.createElement("div");
        makeFull(name_and_save);
        body.appendChild(name_and_save);

        let h2_name = document.createElement("h3");
        h2_name.innerText = "Name";
        name_and_save.appendChild(h2_name);

        let input_name = document.createElement("input");
        input_name.classList.add("name");
        input_name.name = "name";
        input_name.type = "text";
        input_name.value = this.name;
        input_name.placeholder = "New Competition";
        input_name.dataset.originalName = this.name;
        input_name.maxLength = 50;
        input_name.oninput = function() {
            inputMaxLength(input_name);
        };
        name_and_save.appendChild(input_name);
        this.dom.compName = input_name;
        /* CLOSE */

        /* OPEN */
        let isPublic_header = document.createElement("div");
        makeHalf(isPublic_header);
        body.appendChild(isPublic_header);

        let h2_isPublic_header = document.createElement("h3");
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

        /* OPEN */
        let numNonAlts_header = document.createElement("div");
        makeHalf(numNonAlts_header);
        body.appendChild(numNonAlts_header);

        let h2_numNonAlts_header = document.createElement("h3");
        h2_numNonAlts_header.innerHTML = "Team Size (excluding alts)";
        numNonAlts_header.appendChild(h2_numNonAlts_header);
        /* CLOSE */

        /* OPEN */
        let numNonAlts_header_input = document.createElement("div");
        makeHalf(numNonAlts_header_input);
        body.appendChild(numNonAlts_header_input);

        let numNonAlts_input = document.createElement("input");
        numNonAlts_input.name = "numNonAlts";
        numNonAlts_input.type = "number";
        numNonAlts_input.value = ""+numNonAlts;
        numNonAlts_input.min = "1";
        numNonAlts_input.max = "127";
        /*numNonAlts_input.oninput = function() {
            numberInputCheckMaxValue(numNonAlts_input, 1, 127);
        };*/

        numNonAlts_header_input.appendChild(numNonAlts_input);
        this.dom.numNonAlts = numNonAlts_input;
        /* CLOSE */

        /* OPEN */
        let template = document.createElement("div");
        makeFull(template);
        template.classList.add("template");
        body.appendChild(template);

        let h2_template = document.createElement("h3");
        h2_template.innerHTML = "Template";
        template.appendChild(h2_template);

        let cs_template = document.createElement("div");
        cs_template.innerHTML = "Computer<br>Science";
        cs_template.classList.add("template_option");
        cs_template.onclick = function(){thisComp.applyTemplate(config.TEMPLATES.cs)};
        template.appendChild(cs_template);

        let numbersense_template = document.createElement("div");
        numbersense_template.innerHTML = "Number<br>Sense";
        numbersense_template.classList.add("template_option");
        numbersense_template.onclick = function(){thisComp.applyTemplate(config.TEMPLATES.numbersense)};
        template.appendChild(numbersense_template);

        let calcapp_template = document.createElement("div");
        calcapp_template.innerHTML = "Calculator<br>Applications";
        calcapp_template.classList.add("template_option");
        calcapp_template.onclick = function(){thisComp.applyTemplate(config.TEMPLATES.calculatorapplications)};
        template.appendChild(calcapp_template);

        let math_template = document.createElement("div");
        math_template.innerText = "Mathematics";
        math_template.classList.add("template_option");
        math_template.onclick = function(){thisComp.applyTemplate(config.TEMPLATES.mathematics)};
        template.appendChild(math_template);
        /* CLOSE */

        /* OPEN */
        let whatItIs = document.createElement("div");
        whatItIs.innerHTML = "Description";
        makeFull(whatItIs);
        body.appendChild(whatItIs);

        let whatItIs_textarea = document.createElement("textarea");
        whatItIs_textarea.value = whatItIsText;
        whatItIs_textarea.classList.add("pageTextTextarea");
        whatItIs_textarea.maxLength = 65535;
        whatItIs_textarea.oninput = function() {
            inputMaxLength(whatItIs_textarea);
        };
        whatItIs.appendChild(whatItIs_textarea);
        this.dom.description = whatItIs_textarea;
        /* CLOSE */

        /* OPEN */
        let judgesDIV = document.createElement("div");
        judgesDIV.innerHTML = "Judges";
        makeFull(judgesDIV);
        body.appendChild(judgesDIV);

        let judges_list = document.createElement("ul");
        let judges_fragment = document.createDocumentFragment();
        for(let judge of this.judges) {
            judges_fragment.appendChild(this.getJudgeDOM(judge));
        }
        judges_list.appendChild(judges_fragment);
        judgesDIV.appendChild(judges_list);
        this.dom.judges_list = judges_list;

        let addJudgeButton = document.createElement("button");
        addJudgeButton.classList.add("addJudge");
        addJudgeButton.innerText = "+";
        addJudgeButton.onclick = showSelectJudge;
        judgesDIV.appendChild(addJudgeButton);
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

        let h2_written_exists = document.createElement("h3");
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
        this.dom.writtenCheckbox = written_toggle_input;

        // @ts-ignore
        if(this.writtenExists) written_toggle.checked = true;
        /* CLOSE */

        body.appendChild(getWrittenSection());

        /* OPEN */
        let h2_handsOn_toggle = document.createElement("div");
        makeHalf(h2_handsOn_toggle);
        h2_handsOn_toggle.innerHTML = "<h3><b>Hands-On (for UIL CS)</b></h3>";
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
        this.dom.handsOnCheckbox = checkbox_handsOn_toggle;
        /* CLOSE */

        body.appendChild(getHandsOnSection());

        return form;
    }
}

function showSelectJudge() {
    dom.selectJudgeCnt.style.display = "block";
}

function closeSelectJudge() {
    dom.selectJudgeCnt.style.display = "none";
}

async function requestLoadJudges() {
    while (ws == null || ws.readyState === 0) {
        await new Promise(r => setTimeout(r, 100));
    }

    ws.send("[\"loadJudges\"]");
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

                if(responses.length>0) {
                    for(let competition of responses) {
                        let obj:Competition = new Competition(competition["cid"], competition["published"],competition["isPublic"],
                            competition["name"], competition["judges"], competition["written"], competition["handsOn"]);
                        let handsOnProblemsList = [];
                        if(competition["handsOn"] != null) handsOnProblemsList = competition["handsOn"]["problems"];
                        let compDom:HTMLElement = obj.getDOM(handsOnProblemsList, competition["description"], competition["rules"],
                            competition["numNonAlts"], competition["alternateExists"]);

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
let visible_competition_edit_dom: HTMLElement;
function toggleEditCompetition(competition:Competition) {
    pageState.editingComp = competition;
    let newCompEdit:HTMLElement = competition.dom.comp_edit;
    if(visible_competition_edit_dom != null) {
        visible_competition_edit_dom.style.display = "none";
        let controls = visible_competition_edit_dom.parentNode.getElementsByClassName("competition_controls")[0].childNodes;
        for(let i=0,j=controls.length;i<j;i++) {
            let element = <HTMLElement>controls[i];
            if(!element.classList.contains("competition_edit")) {
                element.style.display = "none";
            }
        }

        if(competition.dom.errorSuccessBox != null) competition.dom.errorSuccessBox.style.display = "none";
    }

    if(visible_competition_edit_dom != newCompEdit) {
        visible_competition_edit_dom = newCompEdit;
        visible_competition_edit_dom.style.display = "grid";
        let controls = visible_competition_edit_dom.parentNode.getElementsByClassName("competition_controls")[0].childNodes;
        for(let i=0,j=controls.length;i<j;i++) {
            let element = <HTMLElement>controls[i];
            element.style.display = "block";
        }
    } else {
        visible_competition_edit_dom = null;
    }
}

/***
 * Creates a new competition and opens it up for editing
 */
function createNewCompetition():void {
    if(competitions.length <= 0) {
        dom.class_competitions.innerHTML = "";
    }

    let competition:Competition = new Competition("", false,false, "New Competition",[], false, false);

    dom.class_competitions.insertBefore(competition.getDOM([], "", "", 3, false), dom.class_competitions.firstChild);
    toggleEditCompetition(competition);
    showClassComps(document.getElementById("showClassComps"));
}

function addErrorBox(parentBox:HTMLElement, error:string, errorBox?:HTMLElement): HTMLElement{
    if(!errorBox) {
        errorBox = document.createElement("div");
        errorBox.classList.add("error");
        errorBox.innerText = error;
        parentBox.insertAdjacentElement('afterbegin', errorBox);
        return errorBox;
    }
    else {
        errorBox.style.display = "unset";
        errorBox.innerHTML = error;
        errorBox.className = "error";
        return errorBox;
    }
}
function addSuccessBox(parentBox:HTMLElement, success: string, successBox?:HTMLElement): HTMLElement {
    if(!successBox) {
        successBox = document.createElement("div");
        successBox.classList.add("success");
        successBox.innerText = success;
        parentBox.insertAdjacentElement('afterbegin', successBox);
        return successBox;
    } else {
        successBox.style.display = "unset";
        successBox.innerHTML = success;
        successBox.className = "success";
        return successBox;
    }
}

function inputMaxLength(element: {value: string, maxLength: number}) {
    if(element.value.length > element.maxLength) {
        element.value = element.value.slice(0, element.maxLength);
    }
}


/*function numberInputCheckMaxValue(element: {value: string}, min: number, max:number) {
    let valueS: string = element.value.replace(/\W/g,'');    // Remove whitespace
    if(element.value == "") return; // Don't mess with it if the input box is empty

    let value: number = parseInt(element.value);
    if()
    let updateValue: boolean = false;   // If we should update the value

    if(value > max) {
        value = max;
    } else if(value < min) {
        value = min;
    }

    element.value = ""+value;
}*/

document.addEventListener("DOMContentLoaded", function(event) {
    let controls = document.getElementsByClassName("mini_comp_controls");
    for(let i=0,j=controls.length;i<j;i++) {
        let element:HTMLElement = <HTMLElement>controls.item(i);
        element.onclick = function(event) {
            event.preventDefault();
        };
        let cid = element.dataset.id;

        let edit = <HTMLElement>element.getElementsByClassName("competition_edit")[0];
        edit.onclick = function(event) {
            for(let i=0;i<competitions.length;i++) {
                if(competitions[i].cid == cid) {
                    showClassComps(null);
                    toggleEditCompetition(competitions[i]);
                }
            }
        };
    }
});
