package pe.edu.upc.rentayaapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.rentayaapi.model.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByConversationIdOrderBySentAtAsc(Integer conversationId);
}
