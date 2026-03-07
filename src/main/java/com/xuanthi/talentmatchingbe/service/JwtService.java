package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    // TỐI ƯU: Cache lại Key sau lần giải mã đầu tiên để không tốn CPU thực hiện lại
    private SecretKey cachedSignInKey;

    private static final long EXPIRATION_TIME = 86400000; // 24 giờ tính bằng mili giây

    public String generateToken(User user) {
        // TỐI ƯU: Sử dụng HashMap thay vì Map.of để tránh sập app khi Role bị null
        Map<String, Object> extraClaims = new HashMap<>();

        // Kiểm tra an toàn: Nếu user chưa có role (do login OAuth2 lần đầu), gán mặc định là CANDIDATE
        String roleName = (user.getRole() != null) ? user.getRole().name() : "CANDIDATE";
        extraClaims.put("role", roleName);

        // Thêm các thông tin cần thiết khác vào Token
        extraClaims.put("userId", user.getId());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // TỐI ƯU: Sử dụng bộ Parser được cấu hình sẵn thay vì tạo mới mỗi lần (nếu cần cực nhanh)
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey getSignInKey() {
        // TỐI ƯU: Lazy Initialization giúp tránh giải mã Base64 cho mỗi Request
        if (cachedSignInKey == null) {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            cachedSignInKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return cachedSignInKey;
    }
}