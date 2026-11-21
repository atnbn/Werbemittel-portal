package com.miniportal.order;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"template", "fieldValues"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"template", "fieldValues"})
    List<Order> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"template", "fieldValues"})
    List<Order> findAllByPartnerIdOrderByIdAsc(Long partnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"template", "fieldValues"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(Long id);
}
