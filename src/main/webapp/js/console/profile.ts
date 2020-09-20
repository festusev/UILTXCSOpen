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
    },
    COMPETITION: {
        mcOptions : ["a","b","c","d","e"]
    },
    SOCKET_FUNCTIONS: {
    }
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
    get fname(){return this.getHelper(config.IDs.fname)},
    get lname(){return this.getHelper(config.IDs.lname)},
    get school(){return this.getHelper(config.IDs.school)},
    get oldPassword(){return this.getHelper(config.IDs.oldPassword)},
    get newPassword(){return this.getHelper(config.IDs.newPassword)},
    get delUserPassword(){return this.getHelper(config.IDs.delUserPassword)},
    get profileLink(){return this.getHelper(config.IDs.profile_link)},
    get profile(){return this.getHelper(config.IDs.Profile)}
};

let ws: WebSocket;
(function() {
    ws = new WebSocket("wss://" + window.location.host + "/profilesocket");

    ws.onmessage = function(evt) {
        try {
            let msg: { action: string } = JSON.parse(evt.data);
            config.SOCKET_FUNCTIONS[msg.action](msg);
        } catch (e) {}
    };
})();

function asyncConnectHelper(url:string, params:string, teamBox:HTMLElement):boolean {
    addSuccessBox(teamBox, "Running...");
    let xhr:XMLHttpRequest = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status === 200) { // If an error occurred
                const response:object = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("reload")) window.location.href=response["reload"];
                else if(Object.keys(response).includes("success")) addSuccessBox(teamBox, "" + response["success"]);
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
                    addSuccessBox(dom.profile, "" + response["success"]);
                } else {
                    addErrorBox(dom.profile, response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox(dom.profile, config.TEXT.server_error);
            }
        }
    };

    xhr.open('POST', "/console/profile", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    let schoolText = "";
    if(dom.school != null) schoolText = "&school="+dom.school.value;
    xhr.send("action=saveChanges&fname="+dom.fname.value+"&lname="+dom.lname.value+schoolText+"&oldPassword="+dom.oldPassword.value+"&newPassword="+dom.newPassword.value);
    dom.oldPassword.value = "";
    dom.newPassword.value = "";
    return false;
}

function hideDelUser():void {
    document.getElementById("delUserPasswordCnt").style.display = "none";
}

function delUser():boolean {
    asyncConnectHelper("/delete-user", "delUserPass="+dom.delUserPassword.value, dom.profile);
    dom.delUserPassword.value = "";
    hideDelUser();
    return false;
}


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