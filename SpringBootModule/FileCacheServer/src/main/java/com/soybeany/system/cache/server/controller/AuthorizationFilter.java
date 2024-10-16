package com.soybeany.system.cache.server.controller;

import com.soybeany.system.cache.core.security.model.OptFilter;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2020/12/23
 */
@Component
class AuthorizationFilter extends OptFilter implements Filter {

    @Autowired
    private IDynamicConfigProvider configProvider;

    @Override
    protected String onGetAuthorization() {
        return configProvider.getAuthorization();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean canAccess = canAccess(request, response);
        if (!canAccess) {
            return;
        }
        chain.doFilter(request, response);
    }
}
