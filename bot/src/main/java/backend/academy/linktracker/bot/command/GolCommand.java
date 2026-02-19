package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class GolCommand implements Command {

    /** Response provided by Google */
    private static final List<String> PHRASES = List.of(
            "🐘 ГОООООООЛ! НАШ СЛОНЯРА ТОПЧЕТ! 🐘🐘🐘",
            "🌊 ВЫПИЛ ВОДЫ ИЗ БАЙКАЛА — ТЕПЕРЬ ВИЖУ СКВОЗЬ СТЕНЫ! 👁️",
            "🦎 ЯЩЕРЫ ПЫТАЛИСЬ ВЫЧИСЛИТЬ ЧИСЛО ПИ, НО СЛОМАЛИ КАЛЬКУЛЯТОР ОБ НАШУ ЛОГИКУ! 📐",
            "🚀 ИЛОН МАСК? ЭТО ЖЕ ИЛЮХА МАСКОВ ИЗ СЫЗРАНИ! НАШ ПАЦАН! 🚗💨",
            "💪 СЛАВЯНСКИЙ ЗАЖИМ... КЛАВИАТУРОЙ! КОД ПИШЕТСЯ САМ! ⌨️🔥",
            "🐻 МЕДВЕДЬ С БАЛАЛАЙКОЙ ВЗЛОМАЛ ПЕНТАГОН ЧЕРЕЗ БЛЮТУЗ! 📡🎸",
            "🧢 ЭТО БАЗА! ЭТО ГРУНТ! ЭТО ФУНДАМЕНТ! ЭТО НЕСУЩАЯ КОНСТРУКЦИЯ! 🏗️",
            "🦖 ДИНОЗАВРЫ НЕ ВЫМЕРЛИ, ОНИ ПРОСТО УШЛИ В IT! 💻",
            "🥞 БЛИНЫ С ЛОПАТЫ? НЕТ, БЛИНЫ С ВИДЕОКАРТЫ RTX 4090! 🔥🔥🔥",
            "🏋️‍♂️ ПОКА ТЫ СПАЛ, РУСЫ УЖЕ СДЕЛАЛИ ПОДХОД НА ПРЕСС ВСЕЙ СТРАНОЙ!",
            "🧘‍♂️ Я В ПОТОКЕ! Я В РЕСУРСЕ! Я В МОМЕНТЕ! Я В ТАНКЕ! 🚜",
            "🎺 ТУ-ТУ-ТУ! СЛОНОВАЯ КАВАЛЕРИЯ ПРИБЫЛА! 🐘🎺",
            "🥛 КЕФИР — ЭТО ТОПЛИВО БУДУЩЕГО! НЕФТЬ ОТДЫХАЕТ! ⛽️❌",
            "🦅 ОРЛЫ В НЕБЕ, СЛОНЫ НА ЗЕМЛЕ, А МЫ НА РАССЛАБОНЕ! 😎");

    @Override
    public String command() {
        return "gol";
    }

    @Override
    public String description() {
        return "Лютая база";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        String text = PHRASES.get(ThreadLocalRandom.current().nextInt(PHRASES.size()));

        return new SendMessage(chatId, text);
    }
}
