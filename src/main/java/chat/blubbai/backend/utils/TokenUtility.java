package chat.blubbai.backend.utils;

import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.model.RefreshToken;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class TokenUtility {

    /**
     * Generates a Refresh Token
     *
     * @param user User
     * @return refreshToken Token with expiration 14 days
     */
    public static RefreshToken generateRefreshToken(User user) {
        Date now = new Date();
        Key key = Keys.hmacShaKeyFor(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8));
        return new RefreshToken(
                null, // ID will be generated in prePersist
                user,
                Jwts.builder()
                .setSubject(user.getUsername())
                .claim("tokenType", "refresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 1209600000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact(),
                now.toInstant().plusMillis(1209600000), // 14 days
                null,
                false
        );

    }

    /**
     * Generates an Access Token
     *
     * @param user User
     * @return accessToken Token with expiration 10 minutes
     */
    public static AccessTokenDTO generateAccessToken(User user, boolean twoFactorCompleted) {
        Date now = new Date();
        Key key = Keys.hmacShaKeyFor(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8));
        return new AccessTokenDTO(Jwts.builder()
                .setSubject(user.getUsername())
                .claim("tokenType", "access")
                .claim("uId", user.getUUID())
                .claim("secretMethod", user.getSecretMethod())
                .claim("2fa_completed", twoFactorCompleted)
                .claim("mail_verified", user.isMailVerified())
                //.claim("role", user.getRole().getRId())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 600000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact());
    }

    public static AccessTokenDTO generateMailVerificationToken(User user) {
        Date now = new Date();
        Key key = Keys.hmacShaKeyFor(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8));
        return new AccessTokenDTO(Jwts.builder()
                .setSubject(user.getUsername())
                .claim("tokenType", "mail_verification")
                .claim("uId", user.getUUID())
                .claim("mail_verified", true)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 43200000)) // 12 hours
                .signWith(key, SignatureAlgorithm.HS512)
                .compact());
    }

    /**
     * Returns the Expiration Date of the AccessToken
     *
     * @param token Token
     * @return expirationDate
     */
    public static Date getExpirationDate(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .getExpiration();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Returns the Subject of the Token
     *
     * @param token Token
     * @return subject
     */
    public static String getSubject(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Returns the Role of the Token
     *
     * @param token Token
     * @return role
     */
    public static Integer getRole(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .get("role", Integer.class);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Returns the User of the Token
     *
     * @param token Token
     * @param userService UserService
     * @return user User
     */
    public static User getUser(AccessTokenDTO token, UserService userService) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody();
            String uIdString = claims.get("uId", String.class);
            UUID uId = UUID.fromString(uIdString);
            return userService.getUser(uId);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Returns the User of the Token from the Authorization Header
     *
     * @param header Authorization Header
     * @param userService UserService
     * @return user User
     */
    public static User getUserFromHeader(String header, UserService userService) {
        AccessTokenDTO token = TokenUtility.getTokenFromHeader(header);
        assert token != null;
        return getUser(token, userService);
    }

    /**
     * Validates the Token
     *
     * @param token a Token
     * @return boolean
     */
    public static boolean validateToken(AccessTokenDTO token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody();
            return claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Renews the Token
     *
     * @param token refreshToken
     * @param accessToken accessToken
     * @return newToken
     */
    public static AccessTokenDTO renewToken(AccessTokenDTO token, AccessTokenDTO accessToken) {
        try {
            Claims accessClaims;
            try {
                accessClaims = Jwts.parserBuilder()
                        .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parseClaimsJws(accessToken.getToken())
                        .getBody();
            } catch (ExpiredJwtException e) {
                accessClaims = e.getClaims();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody();

            if (claims.getSubject().equals(accessClaims.getSubject())) {
                User user = new User();
                user.setUsername(accessClaims.getSubject());
                user.setUUID(accessClaims.get("uId", UUID.class));
                //user.setRole(new Role(accessClaims.get("role", Integer.class)));
                return generateAccessToken(user, true);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the Token Type
     *
     * @param token Token
     * @return tokenType (access/refresh)
     */
    public static String getTokenType(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .get("tokenType", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public static String getSecretMethod(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .get("secretMethod", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public static Boolean get2FACompleted(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .get("2fa_completed", Boolean.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public static Boolean getMailVerified(AccessTokenDTO token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token.getToken())
                    .getBody()
                    .get("mail_verified", Boolean.class);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Returns the Token from the Header
     *
     * @param authHeader Authorization Header
     * @return token accessToken
     */
    public static AccessTokenDTO getTokenFromHeader(String authHeader) {
        try {
            return new AccessTokenDTO(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates the Authorization Header
     *
     * @param authHeader Authorization Header
     * @return boolean
     */
    public static boolean validateAuthHeader( String authHeader) {
        AccessTokenDTO token = getTokenFromHeader(authHeader);
        if (token == null) return false;
        return validateToken(token);
    }


    /**
     * Retrieves the User from a Mail Verification Token
     * If the token does not contain a uId, it will try to get the user by username.
     * If the token is invalid or expired, it will return null.
     *
     * @param token
     * @param userService
     * @return User or null if the token is invalid or expired
     * @throws JwtException
     */
    public static User getUserFromMailToken(String token, UserService userService) throws JwtException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String uIdString = claims.get("uId", String.class);
            if (uIdString != null) {
                UUID uId = UUID.fromString(uIdString);
                return userService.getUser(uId);
            } else {
                String username = claims.getSubject();
                if (username == null) {
                    return null;
                }
                return userService.getUserByUsername(username);
            }
        } catch (Exception e) {
            System.out.println("Unbekannter Fehler beim Parsen des Tokens: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a test token for development purposes
     * WARNING: Do not use this in production!
     *
     * @return Token
     */
    public static AccessTokenDTO createTestToken(){
        Date now = new Date();
        Key key = Keys.hmacShaKeyFor(EnvProvider.getEnv("JWT_SECRET").getBytes(StandardCharsets.UTF_8));
        return new AccessTokenDTO(Jwts.builder()
                .setSubject("lupier")
                .claim("tokenType", "access")
                .claim("uId", 1)
                .claim("secretMethod", null)
                //.claim("role", user.getRole().getRId())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 999999999))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact());
    }

}
