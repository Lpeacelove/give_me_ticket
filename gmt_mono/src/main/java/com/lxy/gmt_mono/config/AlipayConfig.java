package com.lxy.gmt_mono.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay") // 读取application.yml中的alipay节点
public class AlipayConfig {
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;

    // 支付宝沙箱网关地址
    private final String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String format = "json"; // 返回数据格式
    private final String charset = "UTF-8"; // 编码格式
    private final String signType = "RSA2"; // 签名方式

    /**
     * 创建支付宝客户端实例, 将其作为一个Bean注入到Spring容器中
     * @return AlipayClient
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                this.serverUrl,
                this.appId,
                this.appPrivateKey,
                this.format,
                this.charset,
                this.alipayPublicKey,
                this.signType
        );
    }
}
