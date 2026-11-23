package net.aouchar.agentservice.web;

import net.aouchar.agentservice.agent.AiAgent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST de l'agent : utilisée pour tester dans le navigateur et par le client Angular (Q11).
 *   GET  http://localhost:8087/chat?query=Quels sont les clients ?
 *   POST http://localhost:8087/chat   {"conversationId":"abc","message":"..."}
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AgentRestController {

    private final AiAgent aiAgent;

    public AgentRestController(AiAgent aiAgent) {
        this.aiAgent = aiAgent;
    }

    @GetMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public String chat(@RequestParam String query,
                       @RequestParam(defaultValue = "web-default") String conversationId) {
        return aiAgent.ask(conversationId, query);
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String conversationId = request.conversationId() == null ? "web-default" : request.conversationId();
        return new ChatResponse(aiAgent.ask("web-" + conversationId, request.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> onError(Exception e) {
        return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_PLAIN)
                .body("L'agent n'a pas pu répondre : " + e.getMessage());
    }

    public record ChatRequest(String conversationId, String message) {}
    public record ChatResponse(String answer) {}
}
