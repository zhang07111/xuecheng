package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.print.attribute.standard.NumberUp;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.TimeoutException;


/**
 * 入门程序
 */
public class Producer01 {

    private static final String QUEUE = "linguy";

    public static void main(String[] args) {

        //通过连接工厂创建新的连接和mq连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);//端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机 一个mq服务可以设置多个虚拟机,每个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");
        //建立新连接
        Connection connection = null;
        Channel channel = null;
        try {
            connection= connectionFactory.newConnection();
            //创建会话通道
            channel = connection.createChannel();
            //声明队列
            channel.queueDeclare(QUEUE, true, false, false, null);
            //发送消息
            String message = "hello world 黑马程序员";
            channel.basicPublish("", QUEUE, null, message.getBytes());
            System.out.println("send to mq" + message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //先关闭连接
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
