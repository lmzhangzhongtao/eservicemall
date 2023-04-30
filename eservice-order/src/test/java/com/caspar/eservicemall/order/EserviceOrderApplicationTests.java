package com.caspar.eservicemall.order;

import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.caspar.eservicemall.order.entity.OmsOrderReturnReasonEntity;
import com.caspar.eservicemall.order.service.OmsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.core.DirectExchange;
import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
class EserviceOrderApplicationTests {

	@Autowired
	OmsOrderService orderService;
	@Test
	void contextLoads() {

		OmsOrderEntity orderEntity=new OmsOrderEntity();
//		orderEntity.setCreateTime(new Date());
//		orderService.save(orderEntity);
//		System.out.println("保存成功");

		orderEntity.setId(1l);
		orderEntity.setBillContent("bill content");
		orderService.updateById(orderEntity);
		System.out.println("更新成功");

	}

	@Autowired
	RabbitTemplate rabbitTemplate;
	@Autowired
	AmqpAdmin amqpAdmin;
    @Test
    void createExchange() {
        // 创建交换机
        // String name, boolean durable, boolean autoDelete
		DirectExchange exchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange创建[{}]成功", "hello-java-exchange");
    }

	    @Test
    void createQueue() {
        // 创建队列
        // String name, boolean durable, boolean exclusive, boolean autoDelete
        // exclusive：是否排他，true：只有一个连接可以使用此队列，其他连接无法连上此队列
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue创建[{}]成功", "hello-java-queue");
    }
	    @Test
    void createBinding() {
        // 创建绑定，交换机绑定目的地
        // String destination：目的地name
        // DestinationType destinationType：目的地类型【queue或exchange（路由）】
        // String exchange：待绑定交换机
        // String routingKey：路由键
        Binding bind = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,"hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(bind);
        log.info("Binding创建[{}]成功", "hello-java-binding");
    }
//
    @Test
    void sendMsg() {
        // 消息对象，可以是任意类型，类必须实现serializable，消息会以序列化的方式写入流中（如果使用JSON序列化器，则不需要类实现Serializable）
        OmsOrderReturnReasonEntity message = new OmsOrderReturnReasonEntity();// 退货原因
        message.setId(1L);
        message.setCreateTime(new Date());
        message.setName("哈哈");
        // 消息ID
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", message, correlationData);
    }
}
