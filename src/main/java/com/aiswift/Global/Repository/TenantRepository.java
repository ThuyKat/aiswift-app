package com.aiswift.Global.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiswift.Enum.Status;
import com.aiswift.Global.Entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
	Optional<Tenant> findByName(String name);

	List<Tenant> findByOwnerId(Long ownerId);
	
	List<Tenant> findByOwnerEmail(String email);
	
	//SELECT * FROM tenant WHERE owner_id = ? AND status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?;
	Page<Tenant> findByOwnerIdAndStatusOrderByCreatedAtDesc(Pageable pageable, Long ownerId, Status status);
	
	Optional<Tenant> findByDbName(String dbName);
}
