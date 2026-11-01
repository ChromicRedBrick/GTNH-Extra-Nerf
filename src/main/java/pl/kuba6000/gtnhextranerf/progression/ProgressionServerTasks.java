package pl.kuba6000.gtnhextranerf.progression;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Exams and safety incidents share one queue. Player/save operations run from the logical server tick. */
public final class ProgressionServerTasks {

    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    public static void enqueue(Runnable task) {
        TASKS.add(Objects.requireNonNull(task));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (int i = 0; i < 64; i++) {
            Runnable task = TASKS.poll();
            if (task == null) break;
            task.run();
        }
    }

    public static void reset() {
        TASKS.clear();
    }
}
