package com.aiswift.Global.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.PaymentDetail;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long>{

	List<PaymentDetail> findByPaymentId(long id);
}

