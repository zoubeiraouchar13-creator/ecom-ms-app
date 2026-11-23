package net.aouchar.agentservice.telegram;

import net.aouchar.agentservice.agent.AiAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Q10 - Client Telegram de l'agent IA.
 *
 * Création du bot : dans Telegram, parler à @BotFather -> /newbot -> récupérer le token.
 * Ce bean n'est créé que si la propriété telegram.bot.token est définie
 * (ou la variable d'environnement TELEGRAM_BOT_TOKEN).
 * Mode "long polling" : pas besoin d'URL publique / webhook.
 */
@Component
@ConditionalOnProperty(name = "telegram.bot.token")
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final int TELEGRAM_MAX_LENGTH = 4000; // limite Telegram : 4096 caractères

    private final AiAgent aiAgent;
    private final String botToken;
    private TelegramClient telegramClient;
    private TelegramBotsLongPollingApplication botsApplication;

    public TelegramBot(AiAgent aiAgent, @Value("${telegram.bot.token}") String botToken) {
        this.aiAgent = aiAgent;
        this.botToken = botToken;
    }

    @Override
    public void afterPropertiesSet() throws TelegramApiException {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botsApplication = new TelegramBotsLongPollingApplication();
        this.botsApplication.registerBot(botToken, this);
        log.info("Bot Telegram démarré");
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (text.startsWith("/start")) {
            send(chatId, "Bonjour 👋 Je suis l'assistant e-commerce. Pose-moi une question sur "
                    + "les clients, les produits ou les factures.\nEx : « Quels produits ont moins de 10 unités en stock ? »");
            return;
        }

        String answer;
        try {
            // une mémoire de conversation par chat Telegram
            answer = aiAgent.ask("telegram-" + chatId, text);
        } catch (Exception e) {
            log.error("Erreur de l'agent", e);
            answer = "Désolé, une erreur est survenue : " + e.getMessage();
        }
        send(chatId, answer);
    }

    private void send(Long chatId, String text) {
        if (text == null || text.isBlank()) text = "(pas de réponse)";
        // découpe les réponses trop longues
        for (int start = 0; start < text.length(); start += TELEGRAM_MAX_LENGTH) {
            String part = text.substring(start, Math.min(text.length(), start + TELEGRAM_MAX_LENGTH));
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text(part).build());
            } catch (TelegramApiException e) {
                log.error("Impossible d'envoyer le message Telegram", e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (botsApplication != null) botsApplication.close();
    }
}
