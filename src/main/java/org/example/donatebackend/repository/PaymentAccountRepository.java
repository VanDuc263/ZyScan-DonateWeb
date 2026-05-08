package org.example.donatebackend.repository;

import org.example.donatebackend.entity.PaymentAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccountEntity,Long> {


    @Query(value = """
    select pa from PaymentAccountEntity pa
    where pa.streamerId = :streamerId and pa.providerType = "BANK"
    """)
    Optional<PaymentAccountEntity> findByStreamerId(@Param("streamerId") Long streamerId);
}
