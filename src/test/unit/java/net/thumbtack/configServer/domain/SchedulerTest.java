package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.InvalidTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;

public class SchedulerTest {
    private Scheduler scheduler = null;
    private Runnable taskMock = null;

    @Before
    public void setUp() {
        scheduler = new Scheduler();
        taskMock = Mockito.mock(Runnable.class);
    }

    @Test
    public void whenThereIsNoScheduleWithTheGivenKey_Schedule_ShouldAddIt() throws InvalidTimeoutException, InterruptedException {
        schedule("key", 10);

        Thread.sleep(11);
        Mockito.verify(taskMock).run();
    }

    @Test
    public void whenThereIsScheduleWithTheGivenKey_Schedule_ShouldCancelIt() throws InvalidTimeoutException, InterruptedException {
        schedule("key", 12);

        schedule("key", 60);

        Thread.sleep(25);
        Mockito.verify(taskMock, never()).run();
    }

    @Test
    public void whenThereIsScheduleWithTheGivenKey_Schedule_ShouldQueueNewSchedule() throws InvalidTimeoutException, InterruptedException {
        schedule("key", 5);

        schedule("key", 10);

        Thread.sleep(11);
        Mockito.verify(taskMock).run();
    }

    @Test (expected = InvalidTimeoutException.class)
    public void whenGivenTimeoutIsInvalid_Schedule_ShouldThrowInvalidTimeoutException() throws InvalidTimeoutException {
        schedule("key", -10);
    }

    @Test
    public void whenScheduleExists_Reschedule_ShouldCancelPreviousExecution() throws InvalidTimeoutException, InterruptedException {
        schedule("key", 5);

        Thread.sleep(4);
        scheduler.reschedule("key");

        Thread.sleep(4);
        Mockito.verify(taskMock, never()).run();
    }

    @Test
    public void whenScheduleExists_Reschedule_ShouldPostponeExecutionOnTheScheduleTimeout() throws InvalidTimeoutException, InterruptedException {
        schedule("key", 5);

        scheduler.reschedule("key");

        Thread.sleep(11);
        Mockito.verify(taskMock).run();
    }

    private void schedule(String key, long msTimeout) throws InvalidTimeoutException {
        scheduler.schedule(key, taskMock, msTimeout);
    }
}
