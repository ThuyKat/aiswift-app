package com.aiswift.Global.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.PaymentType;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, Integer> {

}
