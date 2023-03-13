package com.caspar.eservicemall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.ware.feign.ProductFeignService;
import com.caspar.eservicemall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;

import com.caspar.eservicemall.ware.dao.WareSkuDao;
import com.caspar.eservicemall.ware.entity.WareSkuEntity;
import com.caspar.eservicemall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id",skuId);
        }
        String wareId = (String)params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //查询当前sku的总库存量 库存量-锁定库存量
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count==null?false:count>0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //判断如果还没有这个库存记录则新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId).
                eq("ware_id",wareId));
        if (entities==null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            // TODO: 2020/9/6 远程查询sku的名字，如果失败，整个事务无需回滚
            //1.自己catch异常
            // TODO: 2020/9/6 还可以用什么办法让异常出现以后不会滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>)info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String)data.get("skuName"));
                }
            }catch (Exception e){}
            wareSkuDao.insert(skuEntity);
        }else {
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

}