import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

public class PollScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void schedulePollActivation(LocalDateTime activationTime, int pollId, PollManager pollManager, TelegramLongPollingBot bot, UserManager userManager) {
        long delay = Duration.between(LocalDateTime.now(), activationTime).toMillis();
        if (delay < 0) delay = 0; // זהירות אם הזמן כבר עבר בטעות

        scheduler.schedule(() -> {
            System.out.println("🕒 Activating poll ID " + pollId);

            PollsCsvManager pollsCsvManager = new PollsCsvManager();
            pollsCsvManager.activatePollById(pollId); // תשנה את הסטטוס ל-ACTIVE

            pollManager.sendActivePollToAllUsers(bot, userManager);

        }, delay, TimeUnit.MILLISECONDS);
    }
}
