package chat.blubbai.backend.filter;

import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.utils.TokenUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Objects;

/**
 * JwtRequestFilter
 * <p>
 * This filter intercepts incoming HTTP requests and checks for a valid JWT in the Authorization header.
 * If a valid access token is found, it sets the authentication in the Spring Security context.
 *
 * <h2>Behavior:</h2>
 * <ul>
 *     <li>Checks for "Bearer &lt;token&gt;" in the Authorization header.</li>
 *     <li>Validates the token and extracts the username.</li>
 *     <li>Sets the authentication in the SecurityContext if valid.</li>
 *     <li>Does not block the request if the token is missing or invalid (other filters may handle this).</li>
 * </ul>
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    /**
     * Intercepts each request to check for a valid JWT access token.
     * If valid, sets the authentication in the SecurityContext.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException if an error occurs during request processing.
     * @throws IOException if an I/O error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            AccessTokenDTO jwt = new AccessTokenDTO(authHeader.substring(7));
            if (TokenUtility.validateToken(jwt)) {
                if (Objects.requireNonNull(TokenUtility.getTokenType(jwt)).equals("access")) {
                    String username = TokenUtility.getSubject(jwt);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
