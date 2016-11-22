package au.web.service.scheduler;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import au.web.service.scheduler.SchedulerApplicationListener.SchedulerState.State;

public class SchedulerApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	protected final Logger log = LoggerFactory.getLogger(SchedulerApplicationListener.class);

	@Autowired
	ChannelScheduler channelSchedulerManager;

	public static final SchedulerState schedulerState = SchedulerState._instance;
	private final Lock schedulerLock = new ReentrantLock();

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextEvent) {
		log.info(">> onApplicationEvent(ContextRefreshedEvent=" + contextEvent + ")");

		try {
			//make use of Lock to ensure there are NEVER duplicate schedulers.
			schedulerLock.lock();			
			if (!schedulerState.hasStarted()) {
				log.debug(">>> Trigger start schedulers");
				schedulerState.setState(State.START);
				triggerStartSchedulers();
			} else {
				log.debug(">>> Scheduler already started.");
			}
		} finally {
			schedulerLock.unlock();
		}
		log.info("<< onApplicationEvent()");
	}

	private void triggerStartSchedulers() {
		log.info(">> triggerStartSchedulers");
		channelSchedulerManager.start();
		log.info("<< triggerStartSchedulers");
	}

	static class SchedulerState {
		public static final SchedulerState _instance = new SchedulerState();

		public static enum State {
			STOP, START
		}

		private State currentState;
		private boolean started = false;

		private SchedulerState() {
		}

		public void setState(State state) {
			currentState = state;
			if (currentState.equals(State.START)) {
				started = true;
			}
		}

		public boolean hasStarted() {
			return started;
		}
	}
}
