package backend.academy.linktracker.bot.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> tempLinks = new ConcurrentHashMap<>();

    public void setState(long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getState(long chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE);
    }

    public void saveTempLink(long chatId, String link) {
        tempLinks.put(chatId, link);
    }

    public String getTempLink(long chatId) {
        return tempLinks.get(chatId);
    }

    public void clearSession(long chatId) {
        userStates.remove(chatId);
        tempLinks.remove(chatId);
    }

    /**
     * Проверяет, ждет ли бот ввода текста от пользователя (ссылку или теги).
     * @return true - если ждем текст (мы в диалоге)
     *         false - если ждем команду
     */
    public boolean isWaitingForInput(long chatId) {
        UserState currentState = getState(chatId);
        return currentState != UserState.NONE;
    }
}
