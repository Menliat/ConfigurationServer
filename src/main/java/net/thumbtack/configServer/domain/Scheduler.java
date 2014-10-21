package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.InvalidTimeoutException;

import java.util.Date;
import java.util.concurrent.*;

/**
 * Scheduler is a class that can run some task at the given time.
 */
public class Scheduler {
    private ScheduledExecutorService executorService;
    private ConcurrentHashMap<String, ScheduledTask> queuedSchedules;

    public Scheduler() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        queuedSchedules = new ConcurrentHashMap<>();
    }

    /**
     * Schedule the execution of the given named task to the given time.
     * When there is an already scheduled task with the same key, then it will be rescheduled
     * (the previous task will be canceled and the new one will be queued).
     * @param taskKey the unique key that identifies the task
     * @param task an action to execute
     * @param whenExecute time when the task should be executed
     * @throws InvalidTimeoutException when date is expired
     */
    public void schedule(String taskKey, Runnable task, Date whenExecute) throws InvalidTimeoutException {
        final long delay = getDelayFromNowInMilliseconds(whenExecute);
        final Runnable wrappedAction = wrapWithQueuedSchedulesCleaning(taskKey, task);
        final ScheduledFuture<?> token = executorService.schedule(wrappedAction, delay, TimeUnit.MILLISECONDS);
        ScheduledTask scheduledTask = new ScheduledTask(token, delay, task);
        ScheduledTask previous = queuedSchedules.put(taskKey, scheduledTask);
        if (previous != null) {
            previous.cancel();
        }
    }

    /**
     * Schedule the execution of the given named task to the given time.
     * When there is an already scheduled task with the same key, then it will be rescheduled
     * (the previous task will be canceled and the new one will be queued).
     * @param taskKey the unique key that identifies the task
     * @param task an action to execute
     * @param msTimeout delay from now when to execute the task
     * @throws InvalidTimeoutException when msTimeout is not positive
     */
    public void schedule(String taskKey, Runnable task, long msTimeout) throws InvalidTimeoutException {
        schedule(taskKey, task, new Date(System.currentTimeMillis() + msTimeout));
    }

    /**
     * Reschedule task with the given key, if a task with the same key is already scheduled.
     * This operation is not atomic, it is possible to schedule a new task when the previous was executed.
     * @param taskKey the unique key that identifies the task
     */
    public void reschedule(String taskKey) {
        if (queuedSchedules.containsKey(taskKey)) {
            ScheduledTask task = queuedSchedules.get(taskKey);
            if (task != null) {
                // other thread can already delete the task between the "contains" check and the actual getting.
                // we can't use atomic replace because we need some parameters of the queued task.
                try {
                    schedule(taskKey, task.action, task.msTimeout);
                } catch (InvalidTimeoutException e) {
                    // this is not possible since we've already added the schedule
                }
            }
        }
    }

    private long getDelayFromNowInMilliseconds(Date whenExecute) throws InvalidTimeoutException {
        Date now = new Date();
        long delay = (whenExecute.getTime() - now.getTime());
        if (delay > 0) {
            return delay;
        } else {
            throw new InvalidTimeoutException("Timeout for schedule should be greater than 0");
        }
    }

    private Runnable wrapWithQueuedSchedulesCleaning(final String taskKey, final Runnable action) {
        return new Runnable() {
            @Override
            public void run() {
                action.run();
                queuedSchedules.remove(taskKey);
            }
        };
    }

    /**
     * This class is used to store parameters of task for rescheduling.
     */
    private static class ScheduledTask {
        private ScheduledFuture<?> token = null;
        private long msTimeout;
        private Runnable action;

        public ScheduledTask(ScheduledFuture<?> token, long msTimeout, Runnable task) {
            this.token = token;
            this.msTimeout = msTimeout;
            this.action = task;
        }

        public void cancel() {
            token.cancel(true);
        }
    }
}
