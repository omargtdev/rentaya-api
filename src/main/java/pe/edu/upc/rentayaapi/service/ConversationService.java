package pe.edu.upc.rentayaapi.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.rentayaapi.dto.ChatMessageResponse;
import pe.edu.upc.rentayaapi.dto.ConversationRequest;
import pe.edu.upc.rentayaapi.dto.ConversationResponse;
import pe.edu.upc.rentayaapi.dto.MessageRequest;
import pe.edu.upc.rentayaapi.exception.ApiException;
import pe.edu.upc.rentayaapi.model.Conversation;
import pe.edu.upc.rentayaapi.model.Message;
import pe.edu.upc.rentayaapi.model.Property;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.ConversationRepository;
import pe.edu.upc.rentayaapi.repository.MessageRepository;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PropertyService propertyService;
    private final CurrentUserService currentUserService;

    public ConversationService(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository,
        PropertyService propertyService,
        CurrentUserService currentUserService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.propertyService = propertyService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list() {
        User user = currentUserService.get();
        return conversationRepository.findForParticipant(user.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ConversationCreationResult getOrCreate(ConversationRequest request) {
        User tenant = currentUserService.get();
        if (tenant.getRole() != Rol.INQUILINO) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo los inquilinos pueden iniciar conversaciones");
        }

        Property property = propertyService.find(request.propertyId());
        User owner = property.getOwner();
        if (owner.getId().equals(tenant.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes iniciar una conversación sobre tu propia propiedad");
        }

        return conversationRepository.findByPropertyIdAndOwnerIdAndTenantId(
                property.getId(), owner.getId(), tenant.getId())
            .map(conversation -> new ConversationCreationResult(toResponse(conversation), false))
            .orElseGet(() -> {
                Conversation conversation = new Conversation();
                conversation.setProperty(property);
                conversation.setOwner(owner);
                conversation.setTenant(tenant);
                conversation.setLastMessage("");
                Conversation saved = conversationRepository.saveAndFlush(conversation);
                return new ConversationCreationResult(toResponse(saved), true);
            });
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> messages(String conversationId) {
        User user = currentUserService.get();
        Conversation conversation = findAndAuthorize(conversationId, user);
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId()).stream()
            .map(this::toMessageResponse)
            .toList();
    }

    @Transactional
    public ChatMessageResponse send(String conversationId, MessageRequest request) {
        User sender = currentUserService.get();
        Conversation conversation = findAndAuthorize(conversationId, sender);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.content().trim());
        Message saved = messageRepository.saveAndFlush(message);

        conversation.setLastMessage(saved.getContent());
        conversation.setLastMessageAt(saved.getSentAt());
        conversationRepository.save(conversation);
        return toMessageResponse(saved);
    }

    private Conversation findAndAuthorize(String externalId, User user) {
        Integer id = parseId(externalId);
        Conversation conversation = conversationRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        if (!conversation.getOwner().getId().equals(user.getId())
            && !conversation.getTenant().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No participas en esta conversación");
        }
        return conversation;
    }

    private Integer parseId(String externalId) {
        String value = externalId != null && externalId.startsWith("c")
            ? externalId.substring(1)
            : externalId;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Conversación no encontrada");
        }
    }

    private ConversationResponse toResponse(Conversation conversation) {
        return new ConversationResponse(
            "c" + conversation.getId(),
            conversation.getProperty().getId(),
            conversation.getProperty().getTitle(),
            conversation.getOwner().getId(),
            PropertyService.fullName(conversation.getOwner()),
            conversation.getTenant().getId(),
            PropertyService.fullName(conversation.getTenant()),
            conversation.getLastMessage(),
            conversation.getLastMessageAt()
        );
    }

    private ChatMessageResponse toMessageResponse(Message message) {
        return new ChatMessageResponse(
            message.getId(),
            "c" + message.getConversation().getId(),
            message.getSender().getId(),
            PropertyService.fullName(message.getSender()),
            message.getContent(),
            message.getSentAt()
        );
    }

    public record ConversationCreationResult(ConversationResponse response, boolean created) {}
}
