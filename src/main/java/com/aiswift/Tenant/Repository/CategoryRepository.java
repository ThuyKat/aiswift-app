package com.aiswift.Tenant.Repository;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Category;



@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public interface CategoryRepository extends JpaRepository<Category,Long> {

	List<Category> findByParentIsNull();

	boolean existsByNameIgnoreCase(String name);

}
