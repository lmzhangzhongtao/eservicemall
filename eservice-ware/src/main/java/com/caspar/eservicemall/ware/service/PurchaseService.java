package com.caspar.eservicemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.ware.entity.PurchaseEntity;
import com.caspar.eservicemall.ware.vo.MergeVo;
import com.caspar.eservicemall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:51:38
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void received(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}

