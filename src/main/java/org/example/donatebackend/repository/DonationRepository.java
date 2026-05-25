package org.example.donatebackend.repository;

import org.example.donatebackend.entity.Donation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("""
    SELECT d.donorName, SUM(d.amount)
    FROM Donation d
    WHERE d.streamer.token = :token
      AND d.donorId IS NOT NULL
      AND d.status = 'SUCCESS'
    GROUP BY d.donorName
    ORDER BY SUM(d.amount) DESC
    """)
    List<Object[]> findTopDonors(@Param("token") String token, Pageable pageable);

    List<Donation> findTop10ByStatusOrderByCreatedAtDesc(String status);

    List<Donation> findByStreamer_IdAndStatusOrderByCreatedAtDesc(
            Long streamerId,
            String status,
            Pageable pageable
    );

    List<Donation> findByDonorIdAndStatusOrderByCreatedAtDesc(
            Long donorId,
            String status,
            Pageable pageable
    );

    List<Donation> findByStreamer_UserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            String status,
            Pageable pageable
    );

    Donation findByContentAndStatus(String content, String status);

    List<Donation> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);

    long countByStreamer_IdAndStatus(Long streamerId, String status);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.status = :status")
    Double sumAmountByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.streamer.id = :streamerId AND d.status = 'SUCCESS'")
    Double sumSuccessAmountByStreamerId(@Param("streamerId") Long streamerId);
}
