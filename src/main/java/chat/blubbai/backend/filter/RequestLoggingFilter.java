package chat.blubbai.backend.filter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Order(1) // Ensure this filter runs before the security filter
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);

        System.out.println("Incoming request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
        chain.doFilter(request, responseWrapper);
        System.out.println("Response status: " + responseWrapper.getStatus());
    }

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }
}
