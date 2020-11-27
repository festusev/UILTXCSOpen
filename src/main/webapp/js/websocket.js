var ws;
function getWebSocket(url, socket_functions) {
    if (location.protocol !== "https:")
        ws = new WebSocket("ws://" + url);
    else
        ws = new WebSocket("wss://" + url);
    ws.onmessage = function (evt) {
        try {
            var msg = JSON.parse(evt.data);
            console.log(msg);
            console.log(socket_functions);
            socket_functions[msg.action](msg);
        }
        catch (e) { }
    };
    ws.onclose = function (e) {
        setTimeout(function () {
            getWebSocket(url, socket_functions);
        }, 1000);
    };
    ws.onerror = function (e) {
        ws.close();
        setTimeout(function () {
            getWebSocket(url, socket_functions);
        }, 1000);
    };
}
