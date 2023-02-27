package com.caspar.eservicemall.coupon.dao;

import com.caspar.eservicemall.coupon.entity.CouponSpuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:38:26
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {
	
}
