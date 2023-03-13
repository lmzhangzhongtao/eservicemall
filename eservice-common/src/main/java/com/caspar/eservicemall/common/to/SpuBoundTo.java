package com.caspar.eservicemall.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;//购物积分
    private BigDecimal growBounds;//成长积分
}
