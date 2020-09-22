const config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        Class : "class"
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
        }
    }
};

let ws: WebSocket;
(function() {
    ws = new WebSocket("ws://" + window.location.host + "/profilesocket");

    ws.onmessage = function(evt) {
        try {
            let msg: { action: string } = JSON.parse(evt.data);
            config.SOCKET_FUNCTIONS[msg.action](msg);
        } catch (e) {}
    };
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
    get class(){return this.getHelper(config.IDs.Class)}
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
                    let template = document.createElement('template');
                    template.innerHTML = response["html"];
                    console.log("join class");
                    let child = template.content.firstChild;
                    console.log(child);
                    dom.class.replaceWith(child);
                    dom.cached.class = child;
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
 * @param element
 * @param uid
 */
function kickStudent(element:HTMLSpanElement, uid: number) {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=kickStudent&uid=" + uid);

    document.getElementById("studentList").removeChild(element.parentNode);
}

function leaveClass() {
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=leaveClass");
    // showJoinClass();
}


function showJoinClass() {
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