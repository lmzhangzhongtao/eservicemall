
#### 实战目标：
在营销服务中编写一个接口，接着在用户服务中编写一个用户优惠券接口，在这个接口中我们将会进行远程调用营销服务中的接口并进行返回（openfeign完成远程调用）。


①在eservice-coupon服务中添加一个接口
`@RequestMapping("/member/list")
public R memberCoupons() {
CouponEntity couponEntity = new CouponEntity();
couponEntity.setCouponName("满100减10");
return R.ok().put("coupons", Arrays.asList(couponEntity));
}`

②eservice-member服务中编写远程调用接口

首先需要给eservice-member服务中集成openfeign并进行开启feign包扫描进行加强：

编写对应的feign接口：CouponFeignService

在启动器上添加自动包扫描：
@EnableFeignClients(basePackages = "com.caspar.eservicemall.member.feign") //开启feign客户端


最后就是在控制器上添加一个接口其中就包含远程调用：


`@Autowired
CouponFeignService couponFeignService;

@RequestMapping("/coupons")
public R test() {
//当前服务的用户
MemberEntity memberEntity = new MemberEntity();
memberEntity.setNickname("长路");
//远程调用获取优惠券
R membercoupons = couponFeignService.memberCoupons();
//响应用户与优惠券信息
return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
}
`