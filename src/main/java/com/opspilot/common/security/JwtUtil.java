package com.opspilot.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;   // 签名密钥

    @Value("${jwt.expiration:86400000}")
    private long expiration;  // 有效期（默认24小时）

    // 生成 Token
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)              // 存用户名
                .claim("role", role)            // 存角色
                .issuedAt(new Date())           // 发证时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 到期时间
                .signWith(getKey())             // 签名
                .compact();                     // 打包成字符串
    }


    // 检验 Token 是否有效
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;  // 过期、伪造、格式错都算无效
        }
    }

     public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
