package cn.yjxxclub.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.CountDownLatch;

/**
 * Author: Starry.Teng
 * Email: tengxing7452@163.com
 * Date: 17-9-13
 * Time: 下午2:02
 * Describe: Recceiver
 */
public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private CountDownLatch latch;
    //  解释   一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    //  解释  一个简单的messaging模板，它允许应用程序通过其消息代理向任意目的地发送消息。




    public Receiver(CountDownLatch latch) {
        this.latch = latch;
    }

    public void receiveMessage(String message) {
        LOGGER.info("Received <" + message + ">");
        messagingTemplate.convertAndSend("/topic/greetings",new Greeting(message));
        //   解释 通过convertAndSendToUser 向用户发送信息,
        latch.countDown();//    解释 递减锁存器的计数，如果计数到达零，则释放所有等待的线程。
    }
}
