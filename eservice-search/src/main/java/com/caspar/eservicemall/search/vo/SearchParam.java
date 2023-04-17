package com.caspar.eservicemall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面传递过来的所有查询条件
 */
@Data
public class SearchParam {

    private String keyword;//页面传递过来的全文检索匹配关键字
    private Long catalog3Id;//三级分类id

    /**
     * sort=saleCount_asc/desc  销量升序降序
     * sort=skuPrice_asc/desc   价格升序降序
     * sort=hotScore_asc/desc   热度评分
     */
    private String sort;//排序条件

    private Integer hasStock;//是否显示有货 0无库存 1有库存
    private String skuPrice;//价格区间查询 1_500/_500/500_
    private List<Long> brandId;//按照品牌进行传销，可以多选
    private List<String> attrs;//按照属性进行筛选

    private Integer pageNum = 1;//页码

    private String _queryString;//原生的所有查询条件
}
