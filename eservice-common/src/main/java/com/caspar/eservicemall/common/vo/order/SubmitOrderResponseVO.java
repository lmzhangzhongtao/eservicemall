package com.caspar.eservicemall.common.vo.order;

import com.caspar.eservicemall.common.entity.order.OmsCommonOrderEntity;
import lombok.Data;

/**
 * 提交订单返回结果
 * @author: wan
 */
@Data
public class SubmitOrderResponseVO {
    private OmsCommonOrderEntity order;
}
