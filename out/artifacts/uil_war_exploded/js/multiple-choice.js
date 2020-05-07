var beginWarning;
document.addEventListener("DOMContentLoaded", function(event) {
    beginWarning = document.getElementById("beginWarning");
});
function begin() {
    startTimer();
    beginWarning.style.display = "none";

    // Tell the server that they've started
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'multiple-choice', true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send('started=' + window.cntdwnLoaded);
    return false;
}
function submitFinal(){
    // First we turn the form into a serialized char array of length 40
    var answers = "[";
    for(var i=1; i<=40; i++) {
        var tmp = document.querySelector('input[name="'+i+'"]:checked');
        if(tmp != null) answers+=tmp.value;
        else answers+='z';  // The character if they skipped it
        if(i<40) answers +=",";
    }
    answers+="]";
    console.log("answers: " + answers);
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if (xhr.readyState === 4) {
            if (xhr.status == 200) { // If an error occurred
                const response = JSON.parse(xhr.responseText);
                if(Object.keys(response).includes("scored")) {
                    addScoredBox(response["scored"]);
                } else {
                    addErrorBox(response["error"]);
                }
            } else {    // A server error occurred. Show an error message
                addErrorBox("Whoops! A server error occurred. Contact an admin if the problem continues.");
            }
        }
    }
    xhr.open('POST', "multiple-choice", true);
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhr.send("answers=" + answers);
    return false;
}
function addErrorBox(error){
    document.getElementById("warningHeader").innerText  = "ERROR: " + error;
    document.getElementById("warningSubtitle").innerText = "Write down your answers just in case.";
    var goBackBtn = document.getElementById("goBackBtn");
    goBackBtn.href="console";
    goBackBtn.innerText = "Go Home"
    window.removeEventListener("onbeforeunload", beforeUnload);

    var beginBtn = document.getElementById("beginBtn");
    beginBtn.innerText = "Try Again";
    beginBtn.onclick = submitFinal;

    beginWarning.style.display = "block";
}
function addScoredBox(scored) {
    var header = document.getElementById("warningHeader");
    header.innerText  = "Scoring Report";
    header.style.color = "var(--success-col);";
    document.getElementById("warningSubtitle").innerText = scored;
    var goBackBtn = document.getElementById("goBackBtn");
    goBackBtn.href="console";
    goBackBtn.innerText = "Go Home";
    document.getElementById("beginBtn").style.display = "none";

    window.removeEventListener("onbeforeunload", beforeUnload);
    beginWarning.style.display = "block";
}
function forceSubmit() {
    document.getElementById("warningHeader").innerText  = "Times Up!";
    document.getElementById("warningSubtitle").innerText = "Submitting now.";

    var submitBtn = document.getElementById("beginBtn");
    submitBtn.style.display = "none";

    var goBackBtn = document.getElementById("goBackBtn");
    goBackBtn.style.display = "none";

    beginWarning.style.display = "block";
    submitFinal();
}
function submit() {
    document.getElementById("warningHeader").innerText  = "Are you sure you want to submit2?";
    document.getElementById("warningSubtitle").innerText = "Be sure to double check your work!";

    var submitBtn = document.getElementById("beginBtn");
    submitBtn.innerText = "Submit";
    submitBtn.onclick=function(){submitFinal()};

    var goBackBtn = document.getElementById("goBackBtn");
    goBackBtn.innerText = "Not yet";
    goBackBtn.removeAttribute("href");
    goBackBtn.style.cursor="pointer";
    goBackBtn.onclick = function(){document.getElementById('beginWarning').style.display = 'none';};

    beginWarning.style.display = "block";
}
function beforeUnload(){
    return true;
}