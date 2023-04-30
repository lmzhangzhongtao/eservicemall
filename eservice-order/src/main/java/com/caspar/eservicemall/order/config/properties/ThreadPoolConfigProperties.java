package com.caspar.eservicemall.order.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/***
 * 使用Lombok, 需要@Data注解在配置处理器之前运行，即在注解顺序中应该在@ConfigurationProperties之前。否则不会有自定义配置文件的属性提示。
 * **/
@Data
@ConfigurationProperties(prefix = "eservicemall.thread")

public class ThreadPoolConfigProperties {

    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
