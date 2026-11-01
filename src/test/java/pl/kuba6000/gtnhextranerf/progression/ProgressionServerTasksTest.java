package pl.kuba6000.gtnhextranerf.progression;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import cpw.mods.fml.common.gameevent.TickEvent;

class ProgressionServerTasksTest {

    @Test
    void networkSubmissionRunsOnTheServerTickThread() throws Exception {
        AtomicReference<String> rewardThread = new AtomicReference<>();
        Thread network = new Thread(
            () -> ProgressionServerTasks.enqueue(
                () -> rewardThread.set(
                    Thread.currentThread()
                        .getName())),
            "exam-network");
        network.start();
        network.join();
        assertNull(rewardThread.get());

        String serverThread = Thread.currentThread()
            .getName();
        new ProgressionServerTasks().onTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        assertEquals(serverThread, rewardThread.get());
    }
}
