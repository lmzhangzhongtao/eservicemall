package com.caspar.eservicemall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.caspar.eservicemall.common.to.ware.SkuHasStockTO;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.common.vo.MemberResponseVo;
import com.caspar.eservicemall.order.feign.CartFeignService;
import com.caspar.eservicemall.order.feign.MemberFeignService;
import com.caspar.eservicemall.order.feign.WmsFeignService;
import com.caspar.eservicemall.order.interceptor.LoginUserInterceptor;
import com.caspar.eservicemall.order.util.TokenUtil;
import com.caspar.eservicemall.order.vo.MemberAddressVO;
import com.caspar.eservicemall.order.vo.OrderConfirmVO;
import com.caspar.eservicemall.order.vo.OrderItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;

import com.caspar.eservicemall.order.dao.OmsOrderDao;
import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.caspar.eservicemall.order.service.OmsOrderService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service("omsOrderService")
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderDao, OmsOrderEntity> implements OmsOrderService {
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    WmsFeignService wmsFeignService;
    @Autowired
    TokenUtil tokenUtil;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderEntity> page = this.page(
                new Query<OmsOrderEntity>().getPage(params),
                new QueryWrapper<OmsOrderEntity>()
        );

        return new PageUtils(page);
    }
    /**
     * 获取结算页（confirm.html）VO数据
     */
    @Override
    public OrderConfirmVO getOrderConfirmData() throws Exception {
        OrderConfirmVO result = new OrderConfirmVO();
        // 获取当前用户
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();

        // 获取当前线程上下文环境, 将主线程的上下文环境同步给异步调用的其他线程，使得请求参数不丢失，以保证会话正常。
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 1.查询封装当前用户收货列表
            // 同步上下文环境器，解决异步无法从ThreadLocal获取RequestAttributes
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVO> address = memberFeignService.getAddress(member.getId());
            result.setMemberAddressVos(address);
        }, executor);
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 2.查询购物车所有选中的商品
            // 同步上下文环境器，解决异步无法从ThreadLocal获取RequestAttributes
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 请求头应该放入GULIMALLSESSION（feign请求会根据requestInterceptors构建请求头）
            List<OrderItemVO> items = cartFeignService.getCurrentCartItems();
            result.setItems(items);
        }, executor).thenRunAsync(() -> {
            // 3.批量查询库存（有货/无货）
            List<Long> skuIds = result.getItems().stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R skuHasStock = wmsFeignService.getSkuHasStock(skuIds);
            List<SkuHasStockTO> skuHasStocks = skuHasStock.getData(new TypeReference<List<SkuHasStockTO>>() {
            });
            Map<Long, Boolean> stocks = skuHasStocks.stream().collect(Collectors.toMap(key -> key.getSkuId(), val -> val.getHasStock()));
            result.setStocks(stocks);
        });
        // 4.查询用户积分
        Integer integration = member.getIntegration();// 积分
        result.setIntegration(integration);

        // 5.金额数据自动计算

        // 6.防重令牌
        String token = tokenUtil.createToken();
        result.setUniqueToken(token);

        // 阻塞等待所有异步任务返回
        CompletableFuture.allOf(addressFuture, cartFuture).get();

        return result;
    }

}