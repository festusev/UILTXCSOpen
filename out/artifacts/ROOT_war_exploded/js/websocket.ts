let ws: WebSocket;
function getWebSocket(url:string, socket_functions: {[k:string]:Function}):void {
    if(location.protocol !== "https:") ws = new WebSocket("ws://" + url);
    else ws = new WebSocket("wss://" + url);

    ws.onmessage = function(evt) {
        try {
            let msg: { action: string } = JSON.parse(evt.data);
            // console.log(socket_functions);
            socket_functions[msg.action](msg);
        } catch (e) {}
    };

    ws.onclose = function(e) {
        setTimeout(function() {
            getWebSocket(url, socket_functions);
        }, 1000);
    };

    ws.onerror = function(e) {
        ws.close();
        /*setTimeout(function() {
            getWebSocket(url, socket_functions);
        }, 1000);*/
    }
}