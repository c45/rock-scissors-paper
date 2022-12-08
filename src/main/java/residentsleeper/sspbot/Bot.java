package residentsleeper.sspbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot {
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));
    private final static String PROCESSING_LABEL = "...";
    private final static List<String> opponentWins = new ArrayList<String>() {{
        add("01");
        add("12");
        add("20");
    }};
    private final static Map<String, String> items = new HashMap<String, String>() {{
        put("0", "\uD83D\uDDFF");
        put("1", "✌");
        put("2", "\uD83D\uDCC3");
    }};


    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message = update.message();

        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();

        BaseRequest request = null;

        if (message != null && message.viaBot() != null && message.viaBot().username().equals("my_absolutely_unique_bot")) {
            InlineKeyboardMarkup replyMarkup = message.replyMarkup();
            if (replyMarkup == null) {
                return;
            }

            InlineKeyboardButton[][] buttons = replyMarkup.inlineKeyboard();

            if (buttons == null) {
                return;
            }

            InlineKeyboardButton button = buttons[0][0];
            String buttonLabel = button.text();

            if (!buttonLabel.equals(PROCESSING_LABEL)) {
                return;
            }

            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            String senderChose = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(
                            new InlineKeyboardMarkup(
                                    new InlineKeyboardButton("\uD83D\uDDFF")
                                            .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "0", messageId)),
                                    new InlineKeyboardButton("✂")
                                            .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "1", messageId)),
                                    new InlineKeyboardButton("\uD83D\uDCC3")
                                            .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "2", messageId))
                            )
                    );
        } else if (inlineQuery != null) {
            InlineQueryResultArticle stone = buildButton("stone", "\uD83D\uDDFF Камень", "0");
            InlineQueryResultArticle scissors = buildButton("scissors", "✂ Ножницы", "1");
            InlineQueryResultArticle paper = buildButton("paper", "\uD83D\uDCC3 Бумага", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), stone, scissors, paper).cacheTime(1);
        } else if (callbackQuery != null) {
            String[] data = callbackQuery.data().split(" ");
            if (data.length < 4) {
                return;
            }
            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChose = data[2];
            String opponentChose = data[3];
            int messageId = Integer.parseInt(data[4]);
            String opponentName = callbackQuery.from().firstName();

            if (senderChose.equals(opponentChose)) {
                bot.execute(new SendPhoto(chatId, "https://cataas.com/cat/cute")
                        .caption(
                                String.format(
                                "%s и %s выбрали %s. Ничья\uD83D\uDE33",
                                senderName,
                                opponentName,
                                items.get(senderChose
                                ))));
            } else if (opponentWins.contains(senderChose + opponentChose)) {
                bot.execute(new SendPhoto(chatId, "https://cataas.com/cat")
                        .caption(
                                String.format(
                                "Ты выбрал %s и победил %s\uD83D\uDE33",
                                items.get(senderChose),
                                opponentName
                                )));
            } else {
                bot.execute(new SendPhoto(chatId, "https://cataas.com/cat/Sad")
                        .caption(
                                String.format(
                                "Ты выбрал %s и проиграл %s\uD83D\uDE29",
                                items.get(senderChose),
                                opponentName
                                )));
            }
        }
            if (request != null) {
                bot.execute(request);
            }

        }

    private static InlineQueryResultArticle buildButton(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "Выбирай:")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callbackData)
                        )
                );
    }
}
