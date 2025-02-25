package com.aiswift.Tenant.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Tenant.Entity.User;
import com.aiswift.Tenant.Repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	public User getUserByEmail(String email) {
		return userRepository.findByEmailWithPermissions(email);
	}
}
