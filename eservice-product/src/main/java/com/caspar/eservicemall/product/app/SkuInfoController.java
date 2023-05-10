package com.caspar.eservicemall.product.app;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.caspar.eservicemall.product.entity.SkuInfoEntity;
import com.caspar.eservicemall.product.service.SkuInfoService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.R;



/**
 * sku信息
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
 //   @RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
      //  PageUtils page = skuInfoService.queryPage(params);
        PageUtils page = skuInfoService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
  //  @RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
  //  @RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }
    /**
     * 获取商品价格信息
     */
    @PostMapping("/info/sku/price")
    public Map<Long, BigDecimal> getPrice(@RequestBody Collection<Long> skuIds) {
        return skuInfoService.getByIds(skuIds).stream()
                .collect(Collectors.toMap(SkuInfoEntity::getSkuId, SkuInfoEntity::getPrice));
    }

    /**
     * 查询商品集合
     */
    @PostMapping("/infos")
    public R infos(@RequestBody List<Long> skuIds) {
        List<SkuInfoEntity> skuInfos = skuInfoService.getByIds(skuIds);
        return R.ok().setData(skuInfos);
    }
}
