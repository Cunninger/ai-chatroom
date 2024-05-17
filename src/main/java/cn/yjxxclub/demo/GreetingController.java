package cn.yjxxclub.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
@Controller
public class GreetingController {
    @Value("${chatbot.api.url}")
    private String chatbotApiUrl;
    @Value("${chatbot.api.authorization}")
    private String chatbotApiAuthorization;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/askRobot")
    @SendTo("/topic/greetings")
    public Greeting askRobot(String message) throws Exception {
        OkHttpClient client = new OkHttpClient();
   // 从{"message":"问题"}提取message对应的问题
        ObjectMapper mapper1 = new ObjectMapper();
        JsonNode jsonNode1 = mapper1.readTree(message);
        String tempMessage = jsonNode1.get("message").asText();


        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = new okhttp3.RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                String jsonTemplate = "{\n" +
                        "    \"model\": \"llama3-70b-8192\",\n" +
                        "    \"messages\": [\n" +
                        "        {\n" +
                        "            \"role\": \"user\",\n" +
                        "            \"content\": \"%s\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"max_tokens\": 4096,\n" +
                        "    \"stream\": false\n" +
                        "}";
                String jsonRequest = String.format(jsonTemplate, tempMessage);
                sink.writeUtf8(jsonRequest);
            }
        };

        Request request = new Request.Builder()
                .url(chatbotApiUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", chatbotApiAuthorization)
                .build();

        Response response = client.newCall(request).execute();
        // 解析响应数据
        String responseData = response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseData);
        String answer ="Chatgpt: " +jsonNode.get("choices").get(0).get("message").get("content").asText();


        // 将机器人的回答发送到/topic/message
        return new Greeting(answer);
    }


    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }

    @MessageMapping("/message")
    @SendTo("/topic/greetings")
    //接收/app/message发来的value，然后将value转发到/topic/greetings客户端
    public Greeting message(String message) throws Exception {
        return new Greeting(message);
    }

//    跟上述message方法等价
//    @MessageMapping("/message")
//    public Greeting message1(String name) throws Exception {
//        messagingTemplate.convertAndSend("/topic/greetings", new Greeting(name));
//        return null;
//    }


    /*
     * cron：指定cron表达式

     *zone:官方文档解释：A time zone for which the cron expression will be resolved。指定cron表达式运行的时区

     *fixedDelay：官方文档解释：An interval-based trigger where the interval is measured from the completion time of the previous task. The time unit value is measured in milliseconds.即表示从上一个任务完成开始到下一个任务开始的间隔，单位是毫秒。

     *fixedRate：官方文档解释：An interval-based trigger where the interval is measured from the start time of the previous task. The time unit value is measured in milliseconds.即从上一个任务开始到下一个任务开始的间隔，单位是毫秒。

     *initialDelay:官方文档解释:Number of milliseconds to delay before the first execution of a fixedRate() or fixedDelay() task.任务第一次被调用前的延时，单位毫秒
     * */

    /*
                CRON表达式    含义
    "0 0 12 * * ?"    每天中午十二点触发
    "0 15 10 ? * *"    每天早上10：15触发
    "0 15 10 * * ?"    每天早上10：15触发
    "0 15 10 * * ? *"    每天早上10：15触发
    "0 15 10 * * ? 2005"    2005年的每天早上10：15触发
    "0 * 14 * * ?"    每天从下午2点开始到2点59分每分钟一次触发
    "0 0/5 14 * * ?"    每天从下午2点开始到2：55分结束每5分钟一次触发
    "0 0/5 14,18 * * ?"    每天的下午2点至2：55和6点至6点55分两个时间段内每5分钟一次触发
    "0 0-5 14 * * ?"    每天14:00至14:05每分钟一次触发
    "0 10,44 14 ? 3 WED"    三月的每周三的14：10和14：44触发
    "0 15 10 ? * MON-FRI"    每个周一、周二、周三、周四、周五的10：15触发
    http://cron.qqe2.com/
    */
    @Scheduled(cron = "0/5 * *  * * ? ")   //每5秒执行一次
    public void time() {
        System.out.println("dd");
        messagingTemplate.convertAndSend("/topic/greetings", new Greeting(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
    }
}