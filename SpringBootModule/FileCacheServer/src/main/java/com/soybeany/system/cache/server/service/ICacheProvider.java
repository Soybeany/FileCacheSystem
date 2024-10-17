package com.soybeany.system.cache.server.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ICacheProvider {

    String onEnsure(String token, HttpServletRequest request, HttpServletResponse response);

    void onDownload(String token, HttpServletRequest request, HttpServletResponse response);

}
