package com.caspar.eservicemall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.caspar.eservicemall.order.entity.OmsOrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;
import com.rabbitmq.client.Channel;
import com.caspar.eservicemall.order.dao.OmsOrderItemDao;
import com.caspar.eservicemall.order.entity.OmsOrderItemEntity;
import com.caspar.eservicemall.order.service.OmsOrderItemService;

//@RabbitListener(queues = {"hello-java-queue"})
@Service("omsOrderItemService")
public class OmsOrderItemServiceImpl extends ServiceImpl<OmsOrderItemDao, OmsOrderItemEntity> implements OmsOrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderItemEntity> page = this.page(
                new Query<OmsOrderItemEntity>().getPage(params),
                new QueryWrapper<OmsOrderItemEntity>()
        );

        return new PageUtils(page);
    }
    /**
     * queues：声明需要监听的队列
     * channel：当前传输数据的通道
     * 获取实际消息内容有两种方式：
     *  方式一：在方法参数列表中直接声明出来
     *  方式二：从请求体中取出消息的二进制形式，然后通过JSON反序列化即可
     */
    //@RabbitListener(queues = {"hello-java-queue"})
    //@RabbitHandler
    public void revieveMessage(Message message, OmsOrderReturnReasonEntity entity, Channel channel) {
        // 请求体，序列化存储（本例中已使用Jackson2JsonMessageConverter序列化器作JSON序列化存储）
        byte[] body = message.getBody();
        // 请求头
        MessageProperties messageProperties = message.getMessageProperties();
        // JSON反序列得到消息内容对象
        OmsOrderReturnReasonEntity reason = JSONObject.parseObject(body, OmsOrderReturnReasonEntity.class);
        System.out.println("接受到的消息对象" + message);
        System.out.println("接受到的消息内容" + reason);
        System.out.println("接受到的消息内容" + entity);
    }

    //@RabbitHandler
    public void revieveMessage(Message message, OmsOrderEntity entity, Channel channel) {
        // 请求体，序列化存储（本例中已使用Jackson2JsonMessageConverter序列化器作JSON序列化存储）
        byte[] body = message.getBody();
        // 请求头
        MessageProperties properties = message.getMessageProperties();
        // channel内按顺序自增的long类型消息标签
        long deliveryTag = properties.getDeliveryTag();
        // JSON反序列得到消息内容对象
        OmsOrderEntity reason = JSONObject.parseObject(body, OmsOrderEntity.class);
        System.out.println("接受到的消息对象" + message);
        System.out.println("接受到的消息内容" + reason);
        System.out.println("接受到的消息内容" + entity);
        try {
            if (deliveryTag == 2) {
                // 手动确认，消息会从unacked中删除，total数量减1
                // boolean multiple：是否批量签收
                channel.basicAck(deliveryTag, false);
            } else {
                // 手动拒签
                // boolean multiple：是否批量拒签
                // boolean requeue：当前拒签消息是否发回服务器重新入队
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IOException e) {
            // 网络中断
            e.printStackTrace();
        }
    }
}