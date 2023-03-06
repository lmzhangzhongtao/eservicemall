package com.caspar.eservicemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

