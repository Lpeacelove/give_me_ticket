package com.lxy.gmt_mono.common;

import com.lxy.gmt_mono.entity.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component // 声明这是一个组件
public class JwtUtils {

    // JWT的密钥，用于签名。在生产环境中，这应该是一个更复杂且保密的字符串，通常放在配置文件里。
    // 为了安全，密钥的长度最好足够长。
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // 密钥

    // JWT的过期时间，单位为毫秒。
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

    /**
     * 生成JWT令牌
     *
     * @param userDetails 用户
     * @return JWT令牌
     */
    public String generateToken (UserDetails userDetails) {
        // 创建一个Map对象，用于存储自定义的声明信息。
        // 后期可以存储用户的权限信息，例如角色、权限等。
        Map<String, Object> claims = new HashMap<>();
        // 向claims中添加用户ID
        if (userDetails instanceof LoginUser) {
            claims.put("userId", ((LoginUser) userDetails).getUserId());
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从JWT中解析出Claims（包含所有信息）
     * @param token  JWT
     * @return  Claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * 从JWT中解析出用户名
     * @param token  JWT
     * @return  用户名
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 判断JWT是否过期
     * @param token  JWT
     * @return  是否过期
     */
    public Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * 验证JWT是否有效
     * @param token  JWT
     * @param username  用户名
     * @return  验证结果
     */
    public boolean validateToken(String token, String username) {
        final String extractUsername = extractUsername(token);
        return (extractUsername.equals(username) && !isTokenExpired(token));
    }
}
