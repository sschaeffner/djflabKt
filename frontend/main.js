let webSocket = new WebSocket("http://10.100.1.50:8080/api/ws");

function setup() {
    webSocket.onmessage = (event) => {
        console.log(event.data);

        debugLogRx(event.data);

        if (event.data === "welcome to DJFlab") {
            return;
        }

        let notification = JSON.parse(event.data);
        update(notification);
    };
}

function debugLogRx(msg) {
    try {
        const node = document.createElement("pre");
        node.innerText = msg;
        document.getElementById("debug-log-rx").prepend(node);
    } catch (e) {}
}

function debugLogTx(msg) {
    try {
        const node = document.createElement("pre");
        node.innerText = msg;
        document.getElementById("debug-log-tx").prepend(node);
    } catch (e) {}
}

function send(msg) {
    webSocket.send(msg);
    debugLogTx(msg);
}

function update(notification) {
    console.debug("notification: ", notification);

    let rooms = notification["rooms"];
    console.debug("rooms: ", rooms);

    for (let i = 1; i <= 4; i++) {
        let volume = document.getElementById(`room${i}-volume`);
        let volumeSlider = document.getElementById(`room${i}-volume-slider`);
        let source = document.getElementById(`room${i}-source`);

        volume.innerText = rooms[`${i}`]["volumePercent"];
        volumeSlider.value = rooms[`${i}`]["volumePercent"];
        source.innerText = toSourceName(rooms[`${i}`]["sourceId"]);
    }
}

function onSlider(roomNr, value) {
    const msg = JSON.stringify({
        "type": "xyz.schaeffner.djflab.web.SetVolumeCommand",
        "roomId": roomNr,
        "percent": value
    });

    send(msg);
}

function onNextSource(roomNr) {
    const msg = JSON.stringify({
        "type": "xyz.schaeffner.djflab.web.NextSourceCommand",
        "roomId": roomNr
    });

    send(msg);
}

function toSourceName(sourceId) {
    switch (sourceId) {
        case "1" | 1:
            return "SPOTIFY"
        case "2" | 2:
            return "AIRPLAY"
        case "3" | 3:
            return "AUX"
        default:
            console.error("Unknown sourceId: ", sourceId);
    }
}