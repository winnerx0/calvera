package com.winnerx0.calvera.auth.internal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
class JwtUtils {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String extractSubject(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token){

        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token){
        return extractClaim(token, (claims) -> claims.get("role", String.class));
    }

    public String extractTokenType(String token){
        return extractClaim(token, (claims) -> claims.get("type", String.class));
    }

    public boolean isTokenValid(String token, Long id){
        return extractSubject(token).equals(id) && extractExpiration(token).after(new Date());
    }

    public String generateAccessToken(Long id){
        return buildToken(id, Date.from(Instant.now().plus(Duration.ofMinutes(15))), Map.of("role", "USER", "type", "access"));
    }

    public String generateRefreshToken(Long id){
        return buildToken(id, Date.from(Instant.now().plus(Duration.ofDays(30))), Map.of("role", "USER", "type", "refresh"));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims = extractClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Long id, Date expiration, Map<String, String> claims){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(Long.toString(id))
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

}
