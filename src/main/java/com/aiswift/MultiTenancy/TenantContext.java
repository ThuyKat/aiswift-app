package com.aiswift.MultiTenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
	private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
	
	//each tenant has its own isolated copy of the tenant information
	public static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
	
	public static String getCurrentTenant() {
		logger.info("Tenant Context: getCurrentTenant(): {}", CURRENT_TENANT.get());
		return CURRENT_TENANT.get();
	}
	
	public static void setCurrentTenant(String dbName) {
		CURRENT_TENANT.set(dbName);
	}
	
	public static void clear() {
		CURRENT_TENANT.remove();
	}
}
