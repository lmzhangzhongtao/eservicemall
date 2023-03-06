package com.caspar.eservicemall.product.dao;

import com.caspar.eservicemall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
