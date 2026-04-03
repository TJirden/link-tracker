package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.Subscription;
import backend.academy.linktracker.scrapper.entity.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaSubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

    List<Subscription> findByChatId(Long chatId);

    void deleteByChatIdAndLinkId(Long chatId, Long linkId);

    boolean existsByChatIdAndLinkId(Long chatId, Long linkId);

    @Query("SELECT s.link.id, s.link.url, s.chat.id FROM Subscription s WHERE s.link.id = :linkId")
    List<Object[]> findSubscribersByLinkId(@Param("linkId") Long linkId);
}
