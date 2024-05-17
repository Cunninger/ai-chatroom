package cn.yjxxclub.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.concurrent.CountDownLatch;

/**
 * Author: Starry.Teng
 * Email: tengxing7452@163.com
 * Date: 17-9-13
 * Time: 下午4:53
 * Describe: redis 订阅 发布
 */
@Configuration
public class RedisPubAndSubConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        //    解释  一个Redis消息监听器容器，它接收从RedisConnectionFactory接收的消息，并将其转发给MessageListenerAdapter以进行处理。
        container.setConnectionFactory(connectionFactory);
        //    解释  设置连接工厂
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
        //  解释  添加一个消息监听器，用于处理指定主题的消息。

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
        //默认的方法：handleMessage
    }// 解释  适配器通过反射调用receiveMessage

    @Bean
    Receiver receiver(CountDownLatch latch) {
        return new Receiver(latch);
    }// 解释  一个接收者，接收到消息后，递减锁存器的计数，如果计数到达零，则释放所有等待的线程。

    @Bean
    CountDownLatch latch() {
        return new CountDownLatch(1);
    }// 解释  一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。

}
