package com.caspar.eservicemall.search.feign;

import com.caspar.eservicemall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("eservice-product")
public interface ProductFeignService {
    /**
     * 信息 通过属性id查询属性信息
     */
    @RequestMapping("/product/attr/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId);

    @RequestMapping("/product/brand/info")
    //@RequiresPermissions("product:brand:info")
    public R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);
}
