package com.caspar.eservicemall.order.listener;

import com.caspar.eservicemall.common.entity.order.OmsCommonOrderEntity;
import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.caspar.eservicemall.order.service.OmsOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 定时关单，监听死信队列
 * @Author: wanzenghui
 * @Date: 2022/1/3 17:24
 */
@Slf4j
@RabbitListener(queues = "order.release.order.queue")
@Component
public class OrderCloseListener {

    @Autowired
    OmsOrderService orderService;

    @RabbitHandler
    public void handleOrderRelease(OmsCommonOrderEntity order, Message message, Channel channel) throws IOException {
        log.debug("订单解锁，订单号：" + order.getOrderSn());
        try {
            orderService.closeOrder(order);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}