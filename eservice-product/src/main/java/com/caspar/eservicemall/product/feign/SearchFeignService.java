package com.caspar.eservicemall.product.feign;

import com.caspar.eservicemall.common.to.es.SkuEsModel;
import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("eservice-search")
public interface SearchFeignService {
    /**
     * 上架商品
     * @param skuEsModels
     * @return
     */
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
