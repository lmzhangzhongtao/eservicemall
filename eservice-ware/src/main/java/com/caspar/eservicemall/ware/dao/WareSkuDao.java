package com.caspar.eservicemall.ware.dao;

import com.caspar.eservicemall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 商品库存
 * 
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:51:38
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
    /**
     * 采购成功，商品入库
     */
    Long getSkuStock(Long skuId);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 查询商品库存充足的仓库
     * @param skuIds 商品项ID集合
     * @return
     */
    List<WareSkuEntity> selectListHasSkuStock(@Param("skuIds") Set<Long> skuIds);

    /**
     * 锁定库存
     * @param skuId 商品项ID
     * @param wareId 仓库ID
     * @param count 待锁定库存数
     * @return 1成功  0失败
     */
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);

    /**
     * 解锁库存
     * @param skuId
     * @param wareId
     *  @param count
     */
    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);
}
