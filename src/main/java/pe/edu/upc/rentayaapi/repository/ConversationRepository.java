package pe.edu.upc.rentayaapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.rentayaapi.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    Optional<Conversation> findByPropertyIdAndOwnerIdAndTenantId(
        Integer propertyId,
        Integer ownerId,
        Integer tenantId
    );

    @Query("""
        select c from Conversation c
        where c.owner.id = :userId or c.tenant.id = :userId
        order by c.lastMessageAt desc
        """)
    List<Conversation> findForParticipant(@Param("userId") Integer userId);
}
