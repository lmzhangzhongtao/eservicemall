
https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/spring-cloud-alibaba-examples/nacos-example/nacos-discovery-example/readme-zh.md
 <dependency>
     <groupId>com.alibaba.cloud</groupId>
     <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
 </dependency>


一： 启动Nacos-server服务
本地启动nacos服务，下载nacos-server1.1.3：https://github.com/alibaba/nacos/releases?page=4

进入到bin目录，直接双击startup.cmd来进行启动：

nacos访问服务端口默认是在8848端口：http://localhost:8848/nacos/    默认账号：nacos，密码：nacos。



  docker容器启动服务
docker run -d --name nacos -p 8848:8848 -e PREFER_HOST_MODE=hostname -e MODE=standalone nacos/nacos-server

二：开启服务发现
在应用的 /src/main/resources/application.yaml配置文件中配置 Nacos Server 地址以及application.name也就是应用名称（才能够注册上）：

spring:
  application:
    name: gulimall-coupon   # 服务名
  cloud:
   nacos:
    discovery:
      server-addr: 127.0.0.1:8848


②在启动器上开启服务注册发现注解：@EnableDiscoveryClient //开启服务发现

