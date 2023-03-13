package com.caspar.eservicemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.caspar.eservicemall.product.entity.ProductAttrValueEntity;
import com.caspar.eservicemall.product.service.ProductAttrValueService;
import com.caspar.eservicemall.product.vo.AttrRespVo;
import com.caspar.eservicemall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.caspar.eservicemall.product.entity.AttrEntity;
import com.caspar.eservicemall.product.service.AttrService;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.R;



/**
 * 商品属性
 *
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-03-05 10:45:01
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    /**
     * 查出商品的规格属性
     * @param spuId
     * @param
     * @param
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId")Long spuId){
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);
        return R.ok().put("data",entities);
    }

    //分页查询
    //product/attr/base/list/{catelogId}

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params, @PathVariable("catelogId") Long catelogId, @PathVariable("attrType")String type){
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,type);
        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
 //   @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
  //  @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
	//	AttrEntity attr = attrService.getById(attrId);
        AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		//attrService.save(attr);
        attrService.saveAtrr(attr);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
  //  @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		//attrService.updateById(attr);
        attrService.updateAttr(attr);

        return R.ok();
    }
    /**
     * 修改
     */
    @PostMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId")Long spuId ,@RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
  //  @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
