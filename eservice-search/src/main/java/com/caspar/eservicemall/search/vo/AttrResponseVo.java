package com.caspar.eservicemall.search.vo;

import lombok.Data;

@Data
public class AttrResponseVo {
    /**
     * $column.comments
     */
    private Long attrId;
    /**
     * $column.comments
     */
    private String attrName;
    /**
     * $column.comments
     */
    private Integer searchType;
    /**
     * $column.comments
     */
    private String icon;
    /**
     * $column.comments
     */
    private String valueSelect;
    /**
     * $column.comments
     */
    private Integer attrType;
    /**
     * $column.comments
     */
    private Long enable;
    /**
     * $column.comments
     */
    private Long catelogId;
    /**
     * $column.comments
     */
    private Integer showDesc;

    /**
     * 分组id
     */
    private Long attrGroupId;

    private String catelogName;//所属分类名字

    private String groupName;//所属分组名字

    private Long[] catelogPath;//所属分类完整路径
}
