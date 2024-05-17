var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    // .prop() 代表设置属性  例如：$("#connect").prop("disabled", connected); 代表设置id为connect的元素的disabled属性为connected
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket'); //构建一个SockJS对象
    stompClient = Stomp.over(socket); //用Stomp将SockJS进行协议封装
    stompClient.connect({}, function (frame) { //连接回调
        setConnected(true);
        console.log('Connected: ' + frame);
        /**  订阅了/topic/greetings 发送的消息,这里雨在控制器的 convertAndSendToUser 定义的地址保持一致, 
         *  这里多用了一个/user,并且这个user 是必须的,使用user 才会发送消息到指定的用户。 
         *  */
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });

    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    /*消息发送*/
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function sendMessage() {
    stompClient.send("/app/message", {}, JSON.stringify({'message': $("#mysend").val()}));
}
function sendRobot() {
    stompClient.send("/app/askRobot", {}, JSON.stringify({'message': $("#robot").val()}));
}
// $(function () {}) 代表页面加载完成后执行的方法
$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });// $("form") 代表表单元素  .on() 代表事件监听  'submit' 代表表单提交事件  function(e) {} 代表事件触发后执行的方法
    // $ 这是什么语法 这是jquery的语法，相当于document.getElementById  但是更加方便  用法：$(selector).action()
    // # 代表id选择器    . 代表class选择器    选择器的使用方法和css一样  例如：$("#connect") 代表id为connect的元素    $(".connect") 代表class为connect的元素
    $("#connect").click(function () {
        connect();
    });//   .click() 代表点击事件
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#send").click(function () {
        sendName();
    });
    $("#mybutton").click(function () {
        sendMessage();
    });
    $("#robotbutton").click(function (){
       sendRobot();
    });
});