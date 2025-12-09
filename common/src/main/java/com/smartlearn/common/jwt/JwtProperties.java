package com.smartlearn.common.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "smartlearn.jwt")
public class JwtProperties {

    /**
     * 签名密钥，注意要够长（HS256 最好 >= 32 字节）
     */
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    private long expireMillis;
}
