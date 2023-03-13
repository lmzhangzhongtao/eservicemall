package com.caspar.eservicemall.product.vo;


import com.caspar.eservicemall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrsVo {
    /**
     * $column.comments
     */
    private Long attrGroupId;
    /**
     * $column.comments
     */
    private String attrGroupName;
    /**
     * $column.comments
     */
    private Integer sort;
    /**
     * $column.comments
     */
    private String descript;
    /**
     * $column.comments
     */
    private String icon;
    /**
     * $column.comments
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
