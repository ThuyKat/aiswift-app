package com.aiswift.MultiTenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.aiswift.Global.Service.TenantService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {

	@Autowired
	private TenantService tenanService;

	private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String shopName = request.getHeader("shop-name");
		System.out.println("Interceptor: " + shopName);
		
		if (shopName != null) {
			String dbName = tenanService.getDatabaseNameByShopName(shopName).getDbName();
			TenantContext.setCurrentTenant(dbName);
			logger.info("Tenant set to: {}", dbName);
		} else {
			logger.warn("Shop-name header is missing");
			TenantContext.setCurrentTenant("default");
			 TenantContext.clear();
		}
		return true;
	}
	
	@Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
