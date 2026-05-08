package org.example.donatebackend.repository;

import org.example.donatebackend.entity.PaymentAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccountEntity,Long> {

}
