package com.caspar.eservicemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.to.order.WareSkuLockTO;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.ware.entity.WareSkuEntity;
import com.caspar.eservicemall.ware.vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:51:38
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    Boolean orderLockStock(WareSkuLockTO lockTO);
}

