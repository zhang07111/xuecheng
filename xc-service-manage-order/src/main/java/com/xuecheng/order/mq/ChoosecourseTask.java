package com.xuecheng.order.mq;


import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.ScriptAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


@Component
public class ChoosecourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChoosecourseTask.class);

    @Autowired
    TaskService taskService;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourse(XcTask xcTask) {
        if (xcTask != null) {
            taskService.finishTask(xcTask.getId());
        }
    }

    //定时发送加选课任务
    @Scheduled(cron = "0 0/1 * * * *")
    public void sendChoosecourseTask() {
        //得到一分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE, -1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findXcTaskList(time, 1000);
        System.out.println(taskList);
        //调用service发布消息,将增加选课任务发送给mq
        for (XcTask xcTask : taskList) {
            //取任务
            if (taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0) {
                String mqRoutingkey = xcTask.getMqRoutingkey();//routingKey
                String mqExchange = xcTask.getMqExchange();//发送的交换机
                taskService.publish(xcTask, mqExchange, mqRoutingkey);
            }
        }
    }

    //定义任务调式策略
//    @Scheduled(cron = "0/3 * * * * *")
    public void task1() {
        LOGGER.info("==============测试定时任务开始1================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("=======测试定时任务结束1================");
    }

    //    @Scheduled(fixedDelay = 3000)
    public void task2() {
        LOGGER.info("=====测试定时任务开始2================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("==============测试定时任务结束2================");
    }

}
