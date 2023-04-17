package com.caspar.eservicemall.search.vo;


import com.caspar.eservicemall.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {
    //查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 分页信息
     */
    private Integer pageNum;//当前页码
    private Long total;//总计录数
    private Integer totalPages;//总页码
    private List<Integer> pageNavs;

    private List<BrandVo> brands;//当前查询到的结果，所有涉及到的品牌

    private List<AttrVo> attrs;//当前查询到的结果，所有涉及到的属性

    private List<CatalogVo> catalogs;//当前查询到的结果，所有涉及到的分类

    //============================以上是返回给页面的所有信息=============================

    //面包屑 导航数据
    private List<NavVo> navs = new ArrayList<>();

    private List<Long> attrIds = new ArrayList<>();


    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }
    @Data
    public static class BrandVo{
        private Long brandId;//品牌id
        private String brandName;//品牌名称
        private String brandImg;//品牌照片
    }

    @Data
    public static class AttrVo{
        private Long attrId;//属性id
        private String attrName;//属性名称
        private List<String> attrValue;//属性值
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;//分类id
        private String catalogName;//分类名称
    }
}
