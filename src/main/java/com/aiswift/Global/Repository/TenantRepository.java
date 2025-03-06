package com.aiswift.Global.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long>{
	Tenant findByName(String name);
}
