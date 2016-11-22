package au.web.service.scheduler;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import au.web.service.scheduler.job.ChannelJobScheduler;

public class ChannelSchedulerManager implements ChannelScheduler {

	protected final Logger log = LoggerFactory.getLogger(ChannelSchedulerManager.class);
	
	@Autowired
	ChannelJobScheduler channelJobScheduler;

	@Override
	public void start() {
		log.info(">>start()");

		try {
			channelJobScheduler.schedule();
		} catch (SchedulerException e) {
			log.error("Error starting job scheduler", e);
		}

		log.info("<<start()");

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
