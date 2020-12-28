package com.soybeany.system.cache.core.model;

import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Soybeany
 * @date 2020/12/23
 */
public abstract class OptFilter {

    public boolean canAccess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 只拦截“操作”请求
        if (!httpServletRequest.getServletPath().startsWith(FileCacheHttpContract.OPT_PREFIX)) {
            return true;
        }
        // 验证成功，允许通过
        String authorization = httpServletRequest.getHeader(FileCacheHttpContract.AUTHORIZATION);
        if (onGetAuthorization().equals(authorization)) {
            return true;
        }
        // 验证失败，不允许通过
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    protected abstract String onGetAuthorization();

}
