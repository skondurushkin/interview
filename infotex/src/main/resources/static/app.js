
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
		
	} 
	else if (status == "removed") {
		
	}
	else if (status == "started") {
		
	}
	else if (status == "done") {
		showResults(taskId, stringifyFactors(message.result));
	}
	console.log("Task " + taskId + " has been succesfully " + status);
}

function stringifyFactors(factors) {
	var ret = "";
	for(var i in factors) {
		var factor = factors[i];
		if (ret.length > 0)
		   ret+=' * ';
		if (factor.p == 1)
			ret += factor.f;
		else
			ret += factor.f + '^' + factor.p;
	}
	return ret;
}
function showResults(taskId, message) {

    var t = document.getElementById("results");
    var tr = t.insertRow();
    tr.id = taskId; 
    var td = tr.insertCell(); 
    td.innerHTML = taskId;
    td = tr.insertCell();
    td.innerHTML = message; 
}


$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#send" ).click(function() { sendValue(); });
});

