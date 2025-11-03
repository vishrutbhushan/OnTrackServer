package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByUser(User user);
    List<Order> findByUserAndIsDeletedFalse(User user);
    Optional<Order> findByIdAndUser(Long id, User user);
}
