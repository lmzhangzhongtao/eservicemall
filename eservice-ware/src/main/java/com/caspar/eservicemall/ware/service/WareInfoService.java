package com.caspar.eservicemall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.ware.entity.WareInfoEntity;
import com.caspar.eservicemall.ware.vo.FareVO;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:51:38
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVO getFare(Long addrId);
}

