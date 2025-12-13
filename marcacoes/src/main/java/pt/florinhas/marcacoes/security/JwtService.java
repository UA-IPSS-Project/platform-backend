package pt.florinhas.marcacoes.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Serviço responsável por gerar, validar e extrair informação de tokens JWT.
 *
 * Notas de desenho:
 * - Assinatura simétrica (HMAC) com chave secreta em jwt.secret.
 * - Subject do token = username (email ou NIF, conforme o UserDetails).
 * - Expiração configurável via jwt.expiration (milissegundos).
 * - API JJWT 0.12+: uso de Jwts.builder(), parser().verifyWith(...).build().
 *
 * Boas práticas:
 * - Garantir que a chave tem entropia e comprimento adequados ao algoritmo (>=
 * 256 bits para HS256).
 * - Nunca expor a secret em repositórios; injetar por variável de ambiente.
 */
@Service
public class JwtService {

    /**
     * Chave secreta HMAC (hex/ASCII). Por omissão, valor de fallback apenas para
     * dev.
     * Em produção, **definir externamente** via variável de ambiente/propriedades.
     */
    @Value("${jwt.secret:}")
    private String configuredSecret;

    private String secret;

    @PostConstruct
    public void init() {
        // Se não houver segredo configurado (ou se for o default inseguro), gera um
        // aleatório
        if (configuredSecret == null || configuredSecret.isEmpty() || configuredSecret.length() < 32) {
            this.secret = java.util.UUID.randomUUID().toString().replace("-", "")
                    + java.util.UUID.randomUUID().toString().replace("-", "");
        } else {
            this.secret = configuredSecret;
        }
    }

    /**
     * Tempo de expiração do token em milissegundos (default: 24h).
     * Controla o claim 'exp'.
     */
    @Value("${jwt.expiration:86400000}") // 24 horas em milissegundos
    private long jwtExpiration;

    /**
     * Extrai o "username" (subject) do token.
     * param token JWT assinado
     * return subject (email/NIF conforme UserDetails)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai um claim arbitrário aplicando um resolver aos Claims.
     * param token JWT
     * param claimsResolver função que recebe Claims e devolve T
     * param <T> tipo do claim devolvido
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Gera um token com as claims padrão a partir de um UserDetails.
     * param userDetails utilizador autenticado
     * return token JWT compacto (JWS)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Gera um token incluindo claims extra (custom claims).
     * param extraClaims claims adicionais (ex.: role, id, etc.)
     * param userDetails utilizador autenticado
     * return token JWT compacto (JWS)
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Exposição do valor de expiração configurado (ms). Útil para o frontend saber
     * o TTL.
     */
    // Expor a expiração configurada (em milissegundos)
    public long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * Constrói e assina o token:
     * - subject: username do UserDetails
     * - iat/exp: instantes de emissão e expiração
     * - claims: claims extra fornecidas
     * - assinatura: HMAC com secret configurada
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims) // claims customizadas
                .subject(userDetails.getUsername()) // "sub"
                .issuedAt(new Date(System.currentTimeMillis())) // "iat"
                .expiration(new Date(System.currentTimeMillis() + expiration)) // "exp"
                .signWith(getSignInKey()) // assina com HMAC (alg inferido pela chave)
                .compact();
    }

    /**
     * Valida o token verificando:
     * - se o subject coincide com o username do utilizador,
     * - se o token ainda não expirou.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Indica se o token está expirado (exp < now).
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrai o claim "exp" (expiração) como {@link Date}.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Analisa e valida o token (assinatura + integridade) e devolve todos os
     * claims.
     * Usa o verificador com a SecretKey HMAC configurada.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey()) // valida a assinatura com a chave simétrica
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Constrói a SecretKey HMAC a partir da string 'secret'.
     * A API da JJWT infere o algoritmo (ex.: HS256) do comprimento da chave.
     * Recomendado: chave >= 32 bytes para HS256.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
