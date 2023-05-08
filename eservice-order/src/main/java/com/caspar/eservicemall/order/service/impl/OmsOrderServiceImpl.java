package com.caspar.eservicemall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.caspar.eservicemall.common.constant.ObjectConstant;
import com.caspar.eservicemall.common.constant.order.OrderConstant;
import com.caspar.eservicemall.common.entity.order.OmsCommonOrderEntity;
import com.caspar.eservicemall.common.entity.order.OmsCommonOrderItemEntity;
import com.caspar.eservicemall.common.exception.NoStockException;
import com.caspar.eservicemall.common.exception.VerifyPriceException;
import com.caspar.eservicemall.common.to.order.OrderCreateTO;
import com.caspar.eservicemall.common.to.order.OrderTO;
import com.caspar.eservicemall.common.to.order.SpuInfoTO;
import com.caspar.eservicemall.common.to.order.WareSkuLockTO;
import com.caspar.eservicemall.common.to.ware.SkuHasStockTO;
import com.caspar.eservicemall.common.utils.R;
import com.caspar.eservicemall.common.vo.MemberResponseVo;
import com.caspar.eservicemall.order.entity.OmsOrderEntity;
import com.caspar.eservicemall.order.entity.OmsOrderItemEntity;
import com.caspar.eservicemall.order.entity.OmsPaymentInfoEntity;
import com.caspar.eservicemall.order.feign.CartFeignService;
import com.caspar.eservicemall.order.feign.MemberFeignService;
import com.caspar.eservicemall.order.feign.ProductFeignService;
import com.caspar.eservicemall.order.feign.WmsFeignService;
import com.caspar.eservicemall.order.interceptor.LoginUserInterceptor;
import com.caspar.eservicemall.order.service.OmsPaymentInfoService;
import com.caspar.eservicemall.order.util.TokenUtil;
import com.caspar.eservicemall.common.vo.order.*;
//import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import com.caspar.eservicemall.order.service.OmsOrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service("omsOrderService")
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderDao, OmsOrderEntity> implements OmsOrderService {
    // 提交订单共享提交数据
    private ThreadLocal<OrderSubmitVO> confirmVoThreadLocal = new ThreadLocal<>();
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OmsOrderItemServiceImpl orderItemService;
    @Autowired
    OmsPaymentInfoService paymentInfoService;
    @Autowired
    RabbitTemplate rabbitTemplate;
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

    /**
     * 创建订单
     * GlobalTransactional：seata分布式事务，不适合高并发场景（默认基于AT实现）
     *
     * @param orderSubmitVO 收货地址、发票信息、使用的优惠券、备注、应付总额、令牌
     */
   // @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO) throws Exception {
        SubmitOrderResponseVO result = new SubmitOrderResponseVO();// 返回值
        // 创建订单线程共享提交数据
        confirmVoThreadLocal.set(orderSubmitVO);
        // 1.生成订单实体对象（订单 + 订单项）
        OrderCreateTO order = createOrder();
        // 2.验价应付金额（允许0.01误差，前后端计算不一致）
        if (Math.abs(orderSubmitVO.getPayPrice().subtract(order.getPayPrice()).doubleValue()) >= 0.01) {
            // 验价不通过
            throw new VerifyPriceException();
        }
        // 验价成功
        // 3.保存订单
        saveOrder(order);
        // 4.库存锁定（wms_ware_sku）
        // 封装待锁定商品项TO
        WareSkuLockTO lockTO = new WareSkuLockTO();
        lockTO.setOrderSn(order.getOrder().getOrderSn());
        List<OrderItemVO> locks = order.getOrderItems().stream().map((item) -> {
            OrderItemVO lock = new OrderItemVO();
            lock.setSkuId(item.getSkuId());
            lock.setCount(item.getSkuQuantity());
            lock.setTitle(item.getSkuName());
            return lock;
        }).collect(Collectors.toList());
        lockTO.setLocks(locks);// 待锁定订单项
        //com.caspar.eservicemall.common.vo.ware
        R response = wmsFeignService.orderLockStock(lockTO);
        if (response.getCode() == 0) {
            // 锁定成功
            // TODO 5.远程扣减积分
            // 封装响应数据返回
            result.setOrder(order.getOrder());
            //System.out.println(10 / 0); // 模拟订单回滚，库存不会滚
            // 6.发送创建订单到延时队列
            rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
            return result;
        } else {
            // 锁定失败
            throw new NoStockException("");
        }
    }
    /**
     * 封装订单实体类对象
     * 订单 + 订单项
     */
    private OrderCreateTO createOrder() throws Exception {
        OrderCreateTO result = new OrderCreateTO();// 订单
        // 1.生成订单号
        String orderSn = IdWorker.getTimeId();
        // 2.生成订单实体对象
        OmsOrderEntity orderEntity = buildOrder(orderSn);
        // 3.生成订单项实体对象
        List<OmsOrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        // 4.汇总封装（封装订单价格[订单项价格之和]、封装订单积分、成长值[订单项积分、成长值之和]）
        summaryFillOrder(orderEntity, orderItemEntities);

        // 5.封装TO返回
        //将order应用的vo 转换成common应用的vo
        OmsCommonOrderEntity commonOrderEntity=new OmsCommonOrderEntity();
        BeanUtils.copyProperties(commonOrderEntity,orderEntity);
        //result.setOrder(orderEntity);
        result.setOrder(commonOrderEntity);
        //List<OmsCommonOrderItemEntity>
        //转换对象
        List<OmsCommonOrderItemEntity> commonOrderItemEntityList=new ArrayList<OmsCommonOrderItemEntity>();
        for (OmsOrderItemEntity orderItemEntity : orderItemEntities) {
            OmsCommonOrderItemEntity commonOrderItemEntity=new OmsCommonOrderItemEntity();
            BeanUtils.copyProperties(commonOrderItemEntity,orderItemEntity);
            commonOrderItemEntityList.add(commonOrderItemEntity);
        }
        result.setOrderItems(commonOrderItemEntityList);
       // result.setOrderItems(orderItemEntities);
        result.setFare(orderEntity.getFreightAmount());
        result.setPayPrice(orderEntity.getPayAmount());// 设置应付金额
        return result;
    }
    /**
     * 生成订单实体对象
     *
     * @param orderSn 订单号
     */
    private OmsOrderEntity buildOrder(String orderSn) {
        OmsOrderEntity orderEntity = new OmsOrderEntity();// 订单实体类
        // 1.封装会员ID
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();// 拦截器获取登录信息
        orderEntity.setMemberId(member.getId());
        // 2.封装订单号
        orderEntity.setOrderSn(orderSn);
        // 3.封装运费
        OrderSubmitVO orderSubmitVO = confirmVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVO.getAddrId());// 获取地址
        FareVO fareVO = fare.getData(new TypeReference<FareVO>() {
        });
        orderEntity.setFreightAmount(fareVO.getFare());
        // 4.封装收货地址信息
        orderEntity.setReceiverName(fareVO.getAddress().getName());// 收货人名字
        orderEntity.setReceiverPhone(fareVO.getAddress().getPhone());// 收货人电话
        orderEntity.setReceiverProvince(fareVO.getAddress().getProvince());// 省
        orderEntity.setReceiverCity(fareVO.getAddress().getCity());// 市
        orderEntity.setReceiverRegion(fareVO.getAddress().getRegion());// 区
        orderEntity.setReceiverDetailAddress(fareVO.getAddress().getDetailAddress());// 详细地址
        orderEntity.setReceiverPostCode(fareVO.getAddress().getPostCode());// 收货人邮编
        // 5.封装订单状态信息
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.CREATE_NEW.getCode());
        // 6.设置自动确认时间
        orderEntity.setAutoConfirmDay(OrderConstant.autoConfirmDay);// 7天
        // 7.设置未删除状态
        orderEntity.setDeleteStatus(ObjectConstant.BooleanIntEnum.NO.getCode());
        // 8.设置时间
        Date now = new Date();
        orderEntity.setCreateTime(now);
        orderEntity.setModifyTime(now);
        return orderEntity;
    }
    /**
     * 生成订单项实体对象
     * 购物车每项选中商品产生一个订单项
     */
    private List<OmsOrderItemEntity> buildOrderItems(String orderSn) throws Exception {
        // 封装订单项（最后确定的价格，不会再改变）
        List<OrderItemVO> currentCartItems = cartFeignService.getCurrentCartItems();// 获取当前用户购物车所有商品
        if (!CollectionUtils.isEmpty(currentCartItems)) {
            // 遍历购物车商品，循环构建每个订单项
            List<OmsOrderItemEntity> itemEntities = currentCartItems.stream()
                    .filter(cartItem -> cartItem.getCheck())
                    .map(cartItem -> buildOrderItem(orderSn, cartItem))
                    .collect(Collectors.toList());
            return itemEntities;
        } else {
            throw new Exception();
        }
    }
    /**
     * 生成单个订单项实体对象
     */
    private OmsOrderItemEntity buildOrderItem(String orderSn, OrderItemVO cartItem) {
        OmsOrderItemEntity itemEntity = new OmsOrderItemEntity();
        // 1.封装订单号
        itemEntity.setOrderSn(orderSn);
        // 2.封装SPU信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());// 查询SPU信息
        SpuInfoTO spuInfoTO = spuInfo.getData(new TypeReference<SpuInfoTO>() {
        });
        itemEntity.setSpuId(spuInfoTO.getId());
        itemEntity.setSpuName(spuInfoTO.getSpuName());
        itemEntity.setSpuBrand(spuInfoTO.getSpuName());
        itemEntity.setCategoryId(spuInfoTO.getCatalogId());
        // 3.封装SKU信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());// 商品sku图片
        itemEntity.setSkuPrice(cartItem.getPrice());// 这个是最新价格，购物车模块查询数据库得到
        itemEntity.setSkuQuantity(cartItem.getCount());// 当前商品数量
        String skuAttrsVals = String.join(";", cartItem.getSkuAttrValues());
        itemEntity.setSkuAttrsVals(skuAttrsVals);// 商品销售属性组合["颜色:星河银","版本:8GB+256GB"]
        // 4.优惠信息【不做】

        // 5.积分信息
        int num = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue();// 分值=单价*数量
        itemEntity.setGiftGrowth(num);// 成长值
        itemEntity.setGiftIntegration(num);// 积分

        // 6.价格信息
        itemEntity.setPromotionAmount(BigDecimal.ZERO);// 促销金额
        itemEntity.setCouponAmount(BigDecimal.ZERO);// 优惠券金额
        itemEntity.setIntegrationAmount(BigDecimal.ZERO);// 积分优惠金额
        BigDecimal realAmount = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()))
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);// 实际金额，减去所有优惠金额
        return itemEntity;
    }
    /**
     * 汇总封装订单
     * 1.计算订单总金额
     * 2.汇总积分、成长值
     * 3.汇总应付总额 = 订单总金额 + 运费
     *
     * @param orderEntity       订单
     * @param orderItemEntities 订单项
     */
    private void summaryFillOrder(OmsOrderEntity orderEntity, List<OmsOrderItemEntity> orderItemEntities) {
        // 1.订单总额、促销总金额、优惠券总金额、积分优惠总金额
        BigDecimal total = new BigDecimal(0);
        BigDecimal coupon = new BigDecimal(0);
        BigDecimal promotion = new BigDecimal(0);
        BigDecimal integration = new BigDecimal(0);
        // 2.积分、成长值
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;
        for (OmsOrderItemEntity itemEntity : orderItemEntities) {
            total = total.add(itemEntity.getRealAmount());// 订单总额
            coupon = coupon.add(itemEntity.getCouponAmount());// 促销总金额
            promotion = promotion.add(itemEntity.getPromotionAmount());// 优惠券总金额
            integration = integration.add(itemEntity.getIntegrationAmount());// 积分优惠总金额
            giftIntegration = giftIntegration + itemEntity.getGiftIntegration();// 积分
            giftGrowth = giftGrowth + itemEntity.getGiftGrowth();// 成长值
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setIntegration(giftIntegration);// 积分
        orderEntity.setGrowth(giftGrowth);// 成长值

        // 3.应付总额
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));// 订单总额 +　运费
    }

    /**
     * 保存订单
     * 将封装生成的订单对象 + 订单项对象持久化到DB
     *
     * @param order
     */
    private void saveOrder(OrderCreateTO order) throws InvocationTargetException, IllegalAccessException {
        // 1.持久化订单对象
        OmsCommonOrderEntity commonOrderEntity = order.getOrder();
        //转换
        OmsOrderEntity orderEntity=new OmsOrderEntity();
        BeanUtils.copyProperties(orderEntity,commonOrderEntity);
        save(orderEntity);
        // 2.持久化订单项对象
        List<OmsCommonOrderItemEntity> itemEntities = order.getOrderItems();
        if (CollectionUtils.isEmpty(itemEntities)) {
            return;
        }
        List<OmsOrderItemEntity> orderItemEntityList=new ArrayList<OmsOrderItemEntity>();
        for (OmsCommonOrderItemEntity commonOrderItemEntity : itemEntities) {
            OmsOrderItemEntity orderItemEntity=new OmsOrderItemEntity();
            BeanUtils.copyProperties(orderItemEntity,commonOrderItemEntity);
            orderItemEntityList.add(orderItemEntity);
        }
        orderItemService.saveBatch(orderItemEntityList);
    }
    /**
     * 获取订单详情
     */
    @Override
    public OmsOrderEntity getOrderByOrderSn(String orderSn) {
        return getOne(new QueryWrapper<OmsOrderEntity>().eq("order_sn", orderSn));
    }
    /**
     * 关闭订单
     */
    @Override
    public void closeOrder(OmsCommonOrderEntity order) {
       // OmsOrderEntity _order = getById(order.getId());
        OmsOrderEntity _order=getOrderByOrderSn(order.getOrderSn());
        if (OrderConstant.OrderStatusEnum.CREATE_NEW.getCode().equals(_order.getStatus())) {
            // 待付款状态允许关单
            OmsOrderEntity temp = new OmsOrderEntity();
            temp.setId(_order.getId());
            temp.setStatus(OrderConstant.OrderStatusEnum.CANCLED.getCode());
            updateById(temp);

            try {
                //需要保证消息一定会发送出去，消息可靠性
                // 发送消息给MQ
                OrderTO orderTO = new OrderTO();
                BeanUtils.copyProperties(orderTO, _order);
                //TODO 持久化消息到mq_message表中，并设置消息状态为3-已抵达（保存日志记录）
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTO);
            } catch (Exception e) {
                // TODO 消息未抵达Broker，修改mq_message消息状态为2-错误抵达
            }
        }
    }
    /**
     * 获取订单支付的详细信息
     *
     * @param orderSn 订单号
     */
    @Override
    public PayVO getOrderPay(String orderSn) {
        // 查询订单
        OmsOrderEntity order = this.getOrderByOrderSn(orderSn);
        // 查询所有订单项
        OmsOrderItemEntity item = orderItemService.list(new QueryWrapper<OmsOrderItemEntity>()
                .eq("order_sn", orderSn)).get(0);
        PayVO result = new PayVO();
        BigDecimal amount = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);// 总金额
        result.setTotal_amount(amount.toString());
        result.setOut_trade_no(orderSn);
        result.setSubject(item.getSkuName());
        result.setBody(item.getSkuAttrsVals());
        return result;
    }

    /**
     * 处理支付回调
     *
     * @param targetOrderStatus 目标状态
     */
    @Override
    public void handlePayResult(Integer targetOrderStatus, Integer payCode, OmsPaymentInfoEntity paymentInfo) {
        // 保存交易流水信息
        paymentInfoService.save(paymentInfo);
        // 修改订单状态
        if (OrderConstant.OrderStatusEnum.PAYED.getCode().equals(targetOrderStatus)) {
            // 支付成功状态
            String orderSn = paymentInfo.getOrderSn();
            baseMapper.updateOrderStatus(orderSn, targetOrderStatus, payCode);
        }
    }

    /**
     * 分页查询订单列表、订单详情
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        // 获取登录用户
        MemberResponseVo member = LoginUserInterceptor.loginUser.get();

        // 查询订单
        IPage<OmsOrderEntity> page = this.page(
                new Query<OmsOrderEntity>().getPage(params),
                new QueryWrapper<OmsOrderEntity>()
                        .eq("member_id", member.getId())
                        .orderByDesc("create_time"));
        // 查询订单项
        List<String> orderSns = page.getRecords().stream().map(order -> order.getOrderSn()).collect(Collectors.toList());
        Map<String, List<OmsOrderItemEntity>> itemMap = orderItemService.list(new QueryWrapper<OmsOrderItemEntity>()
                        .in("order_sn", orderSns))
                .stream().collect(Collectors.groupingBy(OmsOrderItemEntity::getOrderSn));

        // 遍历封装订单项
        page.getRecords().forEach(order -> order.setOrderItemEntityList(itemMap.get(order.getOrderSn())));

        return new PageUtils(page);
    }
}