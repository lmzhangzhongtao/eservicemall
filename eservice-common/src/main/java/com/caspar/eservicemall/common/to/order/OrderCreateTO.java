package com.caspar.eservicemall.common.to.order;

import com.caspar.eservicemall.common.entity.order.OmsCommonOrderEntity;
import com.caspar.eservicemall.common.entity.order.OmsCommonOrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建的订单TO对象
 * 1、订单
 * 2、订单项
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: wan
 */
@Data
public class OrderCreateTO {
    private OmsCommonOrderEntity order;  // 订单
    private List<OmsCommonOrderItemEntity> orderItems; // 订单项
    /** 订单计算的应付价格 TODO 是否可删？**/
    private BigDecimal payPrice;
    /** 运费 TODO 是否可删？**/
    private BigDecimal fare;
}
