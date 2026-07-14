package pe.edu.upc.rentayaapi.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.rentayaapi.dto.ChatMessageResponse;
import pe.edu.upc.rentayaapi.dto.ConversationRequest;
import pe.edu.upc.rentayaapi.dto.ConversationResponse;
import pe.edu.upc.rentayaapi.dto.MessageRequest;
import pe.edu.upc.rentayaapi.service.ConversationService;
import pe.edu.upc.rentayaapi.service.ConversationService.ConversationCreationResult;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<ConversationResponse> list() {
        return conversationService.list();
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> getOrCreate(@Valid @RequestBody ConversationRequest request) {
        ConversationCreationResult result = conversationService.getOrCreate(request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.response());
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessageResponse> messages(@PathVariable String conversationId) {
        return conversationService.messages(conversationId);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ChatMessageResponse> send(
        @PathVariable String conversationId,
        @Valid @RequestBody MessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(conversationService.send(conversationId, request));
    }
}
