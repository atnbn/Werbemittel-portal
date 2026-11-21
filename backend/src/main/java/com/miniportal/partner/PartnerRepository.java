package com.miniportal.partner;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Partner p where p.id = :id")
    Optional<Partner> findByIdForUpdate(Long id);
}
