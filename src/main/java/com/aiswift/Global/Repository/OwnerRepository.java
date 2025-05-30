package com.aiswift.Global.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aiswift.Global.Entity.Owner;


public interface OwnerRepository extends JpaRepository<Owner, Long> {

	Optional<Owner> findByEmail(String email);

	@Query("SELECT o FROM Owner o LEFT JOIN FETCH o.tenants")
	List<Owner> findAllWithTenants();

	boolean existsByEmail(String email);

	Optional<Owner> findByResetToken(String token);
}
