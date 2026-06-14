package ru.bibikov.myblogbackapp.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CorsFilter implements Filter {

    private static final String ALLOWED_METHODS="GET, POST, PUT, DELETE, OPTIONS, PATCH";
    private static final String ALLOWED_HEADERS="Origin, Content-Type, Accent, Authorization, X-Requested-With, " +
                                                "Access-Control-Request-Method, Access-Control-Request-Headers";


    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse =(HttpServletResponse) servletResponse;
        HttpServletRequest httpRequest=(HttpServletRequest) servletRequest;
        httpResponse.setHeader("Access-Control-Allow-Origin","http://localhost");
        httpResponse.setHeader("Access-Control-Allow-Methods",ALLOWED_METHODS);
        httpResponse.setHeader("Access-Control-Allow-Credentials","true");
        httpResponse.setHeader("Access-Control-Max-Age","3600");
        httpResponse.setHeader("Access-Control-Allow-Headers",ALLOWED_HEADERS);

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())){
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(servletRequest,servletResponse);
    }
}
