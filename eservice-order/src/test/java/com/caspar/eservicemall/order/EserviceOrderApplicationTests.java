package com.caspar.eservicemall.order;

import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.caspar.eservicemall.order.service.OmsOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

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

}
