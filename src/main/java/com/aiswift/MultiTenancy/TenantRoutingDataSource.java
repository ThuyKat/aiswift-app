package com.aiswift.MultiTenancy;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.Scheduled;

import com.zaxxer.hikari.HikariDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {
	@Autowired
	private DataSourceUtil dataSourceUtil;

	private final Map<Object, Object> dataSourceMap = new ConcurrentHashMap<>();
	private final Map<Object, Long> lastUsedTime = new ConcurrentHashMap<>();

	@Override
	protected Object determineCurrentLookupKey() {
		String tenant = TenantContext.getCurrentTenant();
		return (tenant != null) ? tenant : "default";
	}

	@Override
	protected DataSource determineTargetDataSource() {
		String tenant = (String) determineCurrentLookupKey();

		// if not Hikari pool has not connected yet
		dataSourceMap.computeIfAbsent(tenant, key -> {
			lastUsedTime.put(tenant, System.currentTimeMillis()); // start time
			return dataSourceUtil.createDataSource(tenant); // start the connection
		});

		// already connected
		lastUsedTime.put(tenant, System.currentTimeMillis()); // update
		return (DataSource) dataSourceMap.get(tenant); // return the connection
	}

	@Scheduled(fixedRate = 10 * 60 * 1000)
	public void removedUnusedDataSource() {
		long now = System.currentTimeMillis();
		long threshold = 30 * 60 * 1000;
		for (Object tenant : new HashSet<>(dataSourceMap.keySet())) {
			if (now - lastUsedTime.getOrDefault(tenant, 0L) > threshold) {
				// remove statement will return HikariDataSource, then close the pool connection
				((HikariDataSource) dataSourceMap.remove(tenant)).close();
				lastUsedTime.remove(tenant);
			}
		}
	}

	public void addDataSource(String dbName, DataSource dataSource) {
		if (!dataSourceMap.containsKey(dbName)) {
			this.dataSourceMap.put(dbName, dataSource);
			super.setTargetDataSources(dataSourceMap);
			super.afterPropertiesSet();
			System.out.println("DataSourceMap size: " + dataSourceMap.size());
		}
	}

}
