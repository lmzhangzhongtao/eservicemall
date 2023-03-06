package com.caspar.eservicemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

