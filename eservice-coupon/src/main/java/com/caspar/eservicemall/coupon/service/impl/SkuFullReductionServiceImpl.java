package com.caspar.eservicemall.coupon.service.impl;

import com.caspar.eservicemall.common.to.MemberPrice;
import com.caspar.eservicemall.common.to.SkuReductionTo;
import com.caspar.eservicemall.coupon.entity.MemberPriceEntity;
import com.caspar.eservicemall.coupon.entity.SkuLadderEntity;
import com.caspar.eservicemall.coupon.service.MemberPriceService;
import com.caspar.eservicemall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;

import com.caspar.eservicemall.coupon.dao.SkuFullReductionDao;
import com.caspar.eservicemall.coupon.entity.SkuFullReductionEntity;
import com.caspar.eservicemall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1.保存打折信息 Ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullCount()>0) {
            skuLadderService.save(skuLadderEntity);
        }
        //2.保存满减信息
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,reductionEntity);
        if (reductionEntity.getFullPrice().compareTo(new BigDecimal(0))==1) {
            this.save(reductionEntity);
        }
        //3.保存会员价格
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(item->{
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionTo.getSkuId());
            priceEntity.setMemberLevelId(item.getId());
            priceEntity.setMemberLevelName(item.getName());
            priceEntity.setMemberPrice(item.getPrice());
            priceEntity.setAddOther(1);
            return priceEntity;
        }).filter(item->{
            return item.getMemberPrice().compareTo(new BigDecimal(0))==1;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}