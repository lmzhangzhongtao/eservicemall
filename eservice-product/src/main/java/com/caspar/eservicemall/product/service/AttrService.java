package com.caspar.eservicemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.product.entity.AttrEntity;
import com.caspar.eservicemall.product.vo.AttrGroupRelationVo;
import com.caspar.eservicemall.product.vo.AttrRespVo;
import com.caspar.eservicemall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    public void saveAtrr(AttrVo attr);

    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    public AttrRespVo getAttrInfo(Long attrId);

    public void updateAttr(AttrVo attr);

    public List<AttrEntity> getRelationAttr(Long attrgroupId);

    public void deleteRelation(AttrGroupRelationVo[] vos);

    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params);
}

