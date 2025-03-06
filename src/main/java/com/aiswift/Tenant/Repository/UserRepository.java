package com.aiswift.Tenant.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aiswift.Tenant.Entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	@Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH r.permissions WHERE u.email = :email")
	User findByEmailWithPermissions(@Param("email") String email);

}
