package com.aiswift.Global.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Enum.Role;
import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Repository.DeveloperRepository;

@Service
public class DeveloperService {

	@Autowired
	DeveloperRepository developerRepository;

	public Developer getDeveloperByEmail(String email) {
		return developerRepository.findByEmail(email)
				.orElseThrow(() -> new NoDataFoundException("Developer not found for this email: " + email));
	}

	public Developer getByResetToken(String token) {
		return developerRepository.findByResetToken(token)
				.orElseThrow(() -> new NoDataFoundException("Developer not found for this token: " + token));
	}

	public Developer changeDevToNewRole(String email, String role) {
		Developer dev = getDeveloperByEmail(email);
		try {
			Role newRole = Role.valueOf(role.toUpperCase());
			dev.setRole(newRole);
			return developerRepository.save(dev);

		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid role. " + role);
		}
	}
}
