
window.onload = connect;
window.onclose = disconnect;
var stompClient = null;

function connect() {
    var socket = new SockJS('/infotex');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/test/notifications', function (notification) {
        	dispatchNotification(JSON.parse(notification.body));
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendValue() {
    stompClient.send("/factorize/do", {}, JSON.stringify({'action': "add", 'value': $("#value").val()}));
    document.getElementById("input-form").reset();
}
function removeValue(value) {
    stompClient.send("/factorize/do", {}, JSON.stringify({'action': "remove", 'value': value}));
	
}

function dispatchNotification(ntf) {
	
    var taskId = ntf.task_id; // 
	var body = ntf.content;
	if (ntf.type === "error") {
    	test_onError(taskId, body);
	} else if (ntf.type == "success") {
		test_onOk(taskId, body);
	}
}

function test_onError(taskId, message) {
	console.log("Error with task " + taskId + "\nMessage: "+ message.text | "<no message>");
}

function test_onOk(taskId, message) {
	var status = message.status;
	if (status === "added") {
		addTask(taskId, "not ready yet...");
	} 
	else if (status == "removed") {
		removeTask(taskId);
	}
	else if (status == "done") {
		setResult(taskId, stringifyFactors(message.result||[]));
	}
	console.log("Task " + taskId + " has been succesfully " + status);
}

function getTaskId(taskId) {
	return "task" + taskId;
}
function getTaskRow(taskId) {
	return document.getElementById(getTaskId());
}

function stringifyFactors(factors) {
	var ret = "";
	for(var i in factors) {
		var factor = factors[i];
		if (ret.length > 0)
		   ret+=' * ';
		ret += factor.f;
		if (factor.p > 1)
			ret += '^' + factor.p;
	}
	return ret.length == 0 ? "not ready yet...." : ret;
}

function createRow(taskId) {
    var t = document.getElementById("results");
    var tr = t.insertRow();
    tr.id = getTaskId(taskId); 
    var td = tr.insertCell(); // button
    td.onclick = function() { t.removeChild(tr); removeValue(taskId); }
    td.innerHTML = "<button class='editbtn'>remove</button>";
    td = tr.insertCell();
    td.innerHTML = taskId; // value
    td = tr.insertCell(); // result
    return tr;
}

function getRow(taskId, createIfNone) {
	var tr = document.getElementById(getTaskId(taskId));
	if (tr == null && createIfNone)
		tr = createRow(taskId);
	return tr;
}

function addTask(taskId, message) {
	if (getRow(taskId, false) == null) {
	    var tr = getRow(taskId, true);
	    tr.cells[2].innerHTML = message;
	}
}

function removeTask(taskId) {
	var tr = getRow(taskId, false);
	if (tr != null) {
		tr.parentNode.removeChild(tr);
	}
} 
function setResult(taskId, message) {
    var tr = getRow(taskId, true);
    if (tr != null)
    	tr.cells[2].innerHTML = message;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#send" ).click(function() { sendValue(); });
});

