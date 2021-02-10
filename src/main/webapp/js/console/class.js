///<reference path="../websocket.ts"/>
var config = {
    TEXT: {
        server_error: "Whoops! A server error occurred. Contact an admin if the problem continues."
    },
    IDs: {
        Class: "class"
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
    get class() { return this.getHelper(config.IDs.Class); }
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
                    var template = document.createElement('template');
                    template.innerHTML = response["html"];
                    console.log("join class");
                    var child = template.content.firstChild;
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
function kickStudent(element, uid) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=kickStudent&uid=" + uid);
    document.getElementById("studentList").removeChild(element.parentNode);
}
function leaveClass() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', "/console/class", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("action=leaveClass");
    showJoinClass();
}
function showJoinClass() {
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
