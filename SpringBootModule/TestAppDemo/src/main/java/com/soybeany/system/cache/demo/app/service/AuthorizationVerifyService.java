package com.soybeany.system.cache.demo.app.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface AuthorizationVerifyService {

    boolean isLegalUser(HttpServletRequest request);

}
