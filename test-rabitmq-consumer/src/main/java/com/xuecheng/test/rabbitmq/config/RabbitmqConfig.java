package com.xuecheng.test.rabbitmq.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String QUEUE_INFORM_EMAIL = "query_inform_email";
    public static final String QUEUE_INFORM_SMS = "query_inform_sms";
    public static final String EXCHANGE_ROUTING_INFORM = "exchange_routing_inform";
    public static final String ROUTINGKEY_EMAIL = "inform.#.email.#";
    public static final String ROUTINGKEY_SMS = "inform.#.sms.#";

    //声明交换机
    @Bean(EXCHANGE_ROUTING_INFORM)
    public Exchange EXCHANGE_ROUTING_INFORM() {
        //.durable(true) 持久化 mq重启之后交换机还在
        return ExchangeBuilder.topicExchange(EXCHANGE_ROUTING_INFORM).durable(true).build();

    }

    //声明队列
    @Bean(QUEUE_INFORM_EMAIL)
    public Queue QUEUE_INFORM_EMAIL() {
        return new Queue(QUEUE_INFORM_EMAIL);
    }

    @Bean(QUEUE_INFORM_SMS)
    public Queue QUEUE_INFORM_SMS() {
        return new Queue(QUEUE_INFORM_SMS);
    }

    //QUEUE_INFORM_EMAIL队列绑定交换机
    @Bean
    public Binding BINDING_QUEUE_INFORM_EMAIL(@Qualifier(QUEUE_INFORM_EMAIL) Queue queue,
                                              @Qualifier(EXCHANGE_ROUTING_INFORM) Exchange exchange
    ) {

        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_EMAIL).noargs();

    }

    //QUEUE_INFORM_SMS队列绑定交换机
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_INFORM_SMS) Queue queue,
                                            @Qualifier(EXCHANGE_ROUTING_INFORM) Exchange exchange
    ) {

        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_SMS).noargs();

    }


}
