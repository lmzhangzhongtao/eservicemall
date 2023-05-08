package com.caspar.eservicemall.order.dao;

import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-02-27 01:02:23
 */
@Mapper
public interface OmsOrderDao extends BaseMapper<OmsOrderEntity> {

    /**
     * 修改订单状态
     * @param orderSn   订单号
     * @param code      订单状态
     * @param payType   支付类型
     */
    void updateOrderStatus(@Param("orderSn") String orderSn, @Param("code") Integer code, @Param("payType") Integer payType);
}
