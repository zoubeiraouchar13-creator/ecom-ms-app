package net.aouchar.agentservice.discord;

import net.aouchar.agentservice.agent.AiAgent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Q10 - Client Discord de l'agent IA (librairie JDA).
 *
 * Création du bot : https://discord.com/developers/applications -> New Application -> Bot
 *   - copier le token (Reset Token)
 *   - activer "MESSAGE CONTENT INTENT" dans l'onglet Bot (obligatoire pour lire les messages)
 *   - OAuth2 > URL Generator : scope "bot" + permissions "Send Messages" et "Read Message History",
 *     ouvrir l'URL pour inviter le bot sur votre serveur.
 *
 * Le bot répond :
 *   - en message privé (DM) à tous les messages ;
 *   - sur un serveur, quand il est mentionné (@MonBot question...) ou avec le préfixe "!ask ".
 */
@Component
@ConditionalOnProperty(name = "discord.bot.token")
public class DiscordBot extends ListenerAdapter implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);
    private static final int DISCORD_MAX_LENGTH = 1900; // limite Discord : 2000 caractères
    private static final String PREFIX = "!ask ";

    private final AiAgent aiAgent;
    private final String botToken;
    // l'appel au LLM est lent : on ne bloque pas le thread d'événements de JDA
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private JDA jda;

    public DiscordBot(AiAgent aiAgent, @Value("${discord.bot.token}") String botToken) {
        this.aiAgent = aiAgent;
        this.botToken = botToken;
    }

    @Override
    public void afterPropertiesSet() throws InterruptedException {
        this.jda = JDABuilder.createLight(botToken,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build()
                .awaitReady();
        log.info("Bot Discord démarré : {}", jda.getSelfUser().getName());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; // ignore ses propres messages et les autres bots

        String content = event.getMessage().getContentRaw().trim();
        String question;

        if (event.isFromType(ChannelType.PRIVATE)) {
            question = content;
        } else if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser())) {
            question = content.replaceAll("<@!?" + event.getJDA().getSelfUser().getId() + ">", "").trim();
        } else if (content.startsWith(PREFIX)) {
            question = content.substring(PREFIX.length()).trim();
        } else {
            return; // message qui ne s'adresse pas au bot
        }
        if (question.isEmpty()) return;

        MessageChannel channel = event.getChannel();
        channel.sendTyping().queue();
        // une mémoire de conversation par salon Discord
        String conversationId = "discord-" + channel.getId();

        executor.submit(() -> {
            String answer;
            try {
                answer = aiAgent.ask(conversationId, question);
            } catch (Exception e) {
                log.error("Erreur de l'agent", e);
                answer = "Désolé, une erreur est survenue : " + e.getMessage();
            }
            send(channel, answer);
        });
    }

    private void send(MessageChannel channel, String text) {
        if (text == null || text.isBlank()) text = "(pas de réponse)";
        for (int start = 0; start < text.length(); start += DISCORD_MAX_LENGTH) {
            channel.sendMessage(text.substring(start, Math.min(text.length(), start + DISCORD_MAX_LENGTH))).queue();
        }
    }

    @Override
    public void destroy() {
        executor.shutdown();
        if (jda != null) jda.shutdown();
    }
}
