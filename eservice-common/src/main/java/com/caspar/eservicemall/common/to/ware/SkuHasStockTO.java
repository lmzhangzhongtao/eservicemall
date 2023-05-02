package com.caspar.eservicemall.common.to.ware;

import lombok.Data;

@Data
public class SkuHasStockTO {
    private Long skuId;
    private Boolean hasStock;
}
