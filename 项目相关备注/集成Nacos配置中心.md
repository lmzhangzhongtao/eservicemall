
# 1.1 引入依赖
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
## 1.2 创建一个 bootstrap.properties.
  spring.application.name=eservice-coupon
  spring.cloud.nacos.config.server-addr=192.168.56.10:8848
  注意：不能使用原来的application.yml作为配置文件，而是新建一个bootstrap.yml作为配置文件
  配置文件加载的优先级（由高到低）

bootstrap.properties ->bootstrap.yml -> application.properties -> application.yml


## 1.3 需要给配置中心默认添加一个叫数据集(Data Id) eservice-coupon.properties。默认规则 ，应用名.properties

# 1.4 给 eservice-coupon.properties 添加任何配置

# 1.5 动态获取配置
     @RefreshScope: 动态获取并刷新配置
     @Value("$(配置项的名)"): 获取到配置。
    如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的配置。