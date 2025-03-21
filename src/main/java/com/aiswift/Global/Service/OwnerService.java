package com.aiswift.Global.Service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiswift.Enum.Status;
import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Repository.OwnerRepository;

@Service
public class OwnerService {

	@Autowired
	private OwnerRepository ownerRepository;

	public Owner getOwnerByEmail(String email) {
		return ownerRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Owner not found: " + email));
	}

	public Owner getOwnerById(long id) {
		return ownerRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException("Owner not found with Id: " + id));
	}

	public List<Owner> findAll() {		
		return ownerRepository.findAllWithTenants();
	}

	public Owner getByResetToken(String token) {
		return ownerRepository.findByResetToken(token)
				.orElseThrow(() -> new RuntimeException("Owner not found for this toke: " + token));
	}

	public void saveOwner(Owner owner) {
		ownerRepository.save(owner);
	}

	// fetch SubPlanDetail List this time to avoid Lazy.Fetch in Entity
	@Transactional(transactionManager = "globalTransactionManager")
	public Owner getOwnerWithSubPlanDetails(String email) {
		Owner owner = getOwnerByEmail(email);
		owner.getSubPlanDetails().size();
		return owner;
	}

	@Transactional(transactionManager = "globalTransactionManager")
	public Owner getOwnerWithSubPlanDetailsById(Long id) {
		Owner owner = getOwnerById(id);
		owner.getSubPlanDetails().size();
		return owner;
	}
	
	@Transactional(transactionManager = "globalTransactionManager")
	public Owner getOwnerWithPayments(Long id) {
		Owner owner = getOwnerById(id);
		owner.getPayments().size();
		
		return owner;
	}
	@Transactional(transactionManager = "globalTransactionManager")
	public Owner changeOwnerStatus(Long id, String status) {
		try {
			Owner owner = getOwnerById(id);
			Status newStatus = Status.valueOf(status.toUpperCase());
			owner.setStatus(newStatus);
			return ownerRepository.save(owner);
			
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid status value: " + status);
		}

	}
}