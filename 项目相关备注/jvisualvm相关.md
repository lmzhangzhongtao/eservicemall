

## jvisualvm能干什么
 监控内存泄露，跟踪垃圾回收，执行时内存、cpu分析，线程分析 ....

![线程分析各区截图.png](java visualvm 线程分析各区.png)

 运行： 正在运行的
 休眠： sleep
 等待:  wait
 驻留:  线程澉里面的空闲线程
 监视： 阻塞的线程，正在等待锁。

#### 进入jvisualvm

 打开控制台命令行，直接输入命令jvisualvm

#### 安装插件 visual gc
