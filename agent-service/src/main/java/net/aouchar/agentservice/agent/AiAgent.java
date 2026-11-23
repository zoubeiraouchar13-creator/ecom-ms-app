package net.aouchar.agentservice.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class AiAgent {

    private static final String SYSTEM_PROMPT = """
            Tu es l'assistant de l'application e-commerce (gestion des clients, produits et factures).
            Règles :
            - Pour toute question sur les clients, les produits, le stock ou les factures,
              utilise TOUJOURS les outils disponibles ; n'invente jamais de données.
            - Si un outil renvoie une erreur ou rien, dis-le simplement à l'utilisateur.
            - La devise de l'application est le dirham marocain : écris toujours les montants en MAD
              (ex : 34 000,00 MAD), jamais en euros ni en dollars.
            - Ne refais AUCUN calcul : reprends tels quels les montants fournis par les outils
              (lineTotal, total). Pour le total d'une facture, utilise le champ total ou l'outil getBillTotal.
            - Pour les factures d'un client : trouve d'abord son id (searchCustomersByName),
              puis appelle getBillsByCustomerId avec cet id. N'affiche que les factures renvoyées.
            - Réponds dans la langue de l'utilisateur (français par défaut), de façon concise.
              Présente les listes sous forme de puces courtes.
            """;

    private final ChatClient chatClient;

    public AiAgent(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(mcpTools) // outils MCP de customer, inventory et billing
                .build();
    }

    /**
     * @param conversationId identifiant de la conversation (pour la mémoire)
     * @param question       message de l'utilisateur
     * @return la réponse de l'agent
     */
    public String ask(String conversationId, String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
