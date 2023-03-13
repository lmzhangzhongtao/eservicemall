package com.caspar.eservicemall.ware.vo;

import lombok.Data;

import javax.naming.ldap.PagedResultsControl;

@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
