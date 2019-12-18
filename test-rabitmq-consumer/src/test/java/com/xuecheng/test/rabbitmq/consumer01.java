package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

public class consumer01 {
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
        try {
            connection = connectionFactory.newConnection();
            //创建会话通道
            Channel channel = connection.createChannel();

            //声明队列
            channel.queueDeclare(QUEUE, true, false, false, null);

            //监听队列
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id,mq在 标识消息的id  可用于确认消息已接收
                    long tag = envelope.getDeliveryTag();

                    String message = new String(body, "utf-8");
                    System.out.println("message :" + message);
                }
            };

            channel.basicConsume(QUEUE, true, defaultConsumer);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

}
