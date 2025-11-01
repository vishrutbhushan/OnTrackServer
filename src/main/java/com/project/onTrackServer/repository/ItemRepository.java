package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByUserIdOrderByIdDesc(String userId);
    List<Item> findByUserIdAndSubjectAndSender(String userId, String subject, String sender);
}
