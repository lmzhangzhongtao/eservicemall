package com.caspar.eservicemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

