package com.aiswift.Global.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.ProrataDetail;

public interface ProrataDetailRepository extends JpaRepository<ProrataDetail, Long> {
	List<ProrataDetail> findByPaymentId(long id);
}
