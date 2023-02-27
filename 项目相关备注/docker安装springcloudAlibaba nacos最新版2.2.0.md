

## 单机本地配置
##  1.1 拉取nacos镜像
docker pull nacos/nacos-server


## 1.2 启动nacos镜像
执行以下命令，以单机模式启动nacos镜像。
docker run -d --name nacos -p 8848:8848 -e PREFER_HOST_MODE=hostname -e MODE=standalone nacos/nacos-server

## 1.3 验证nacos是否启动成功
docker ps -a

##  1.4 访问nacos管理页面
至此，nacos服务已经成功启动，通过访问地址:http:XXXX//:8848/nacos打开管理端页面。默认账号：nacos，密码：nacos。

  
### 1.5 虚拟机开放的8848端口，外部访问不到
解决方法
1：先通过systemctl status firewalld命令查看系统的防火墙是否开启
2:如果没有开启，就先systemctl start firewalld开启防火墙
3:firewall-cmd --permanent --list-port查看防火墙端口开通情况，这里会返回所有已开通端口
4:然后firewall-cmd --permanent --zone=public --add-port=8848/tcp永久开放所需的端口
5: systemctl restart firewalld重启防火墙
再次查看端口状态，会发现已经是开放状态了


### 1.6 nacos升级后需开放多两个端口以便支持gRpc的通信
当nacos客户端升级为2.x版本后，新增了gRPC的通信方式，新增了两个端口。这两个端口在nacos原先的端口上(默认8848)，进行一定偏移量自动生成.。

端口   与主端口的偏移量                        描述
9848    1000          客户端gRPC请求服务端端口，用于客户端向服务端发起连接和请求
9849    1001           服务端gRPC请求服务端端口，用于服务间同步等

因为是用的docker安装的nacos，所以启动的时候，只把宿主机的8848端口和容器的8848端口映射了，
所以客户端像服务端发起连接请求的时候会报9948找不到，只要同时把9949，和9849端口也同时暴露出去就行了

删掉容器，重新启动容器命令如下

 //  sudo docker run -d --name nacos  --restart=always   -p 8848:8848  -p 9848:9848 -p 9849:9849 -e PREFER_HOST_MODE=hostname -e MODE=standalone nacos/nacos-server
sudo docker run --name nacos -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --privileged=true --restart=always -e MODE=standalone -e PREFER_HOST_MODE=hostname nacos/nacos-server
————————————————
