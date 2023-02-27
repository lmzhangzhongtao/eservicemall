package com.caspar.eservicemall.coupon.dao;

import com.caspar.eservicemall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:38:27
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
