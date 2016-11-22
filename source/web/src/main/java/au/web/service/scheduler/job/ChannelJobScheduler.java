package au.web.service.scheduler.job;

import javax.inject.Named;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Named("ChannelJobScheduler")
public class ChannelJobScheduler {

	protected final Logger log = LoggerFactory.getLogger(ChannelJobScheduler.class);

	@Autowired
	protected SchedulerFactoryBean schedulerFactoryBean;

	public ChannelJobScheduler() {
		log.info(">> constructor called.");
	}

	public void schedule() throws SchedulerException {
		log.info(">> schedule()");
		try {
			JobDetail job = JobBuilder.newJob(ChannelJob.class).build();
			job.getJobDataMap().put(ChannelJob.TENANT, "TENANT-01");
			Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(getCronSchedule())).build();
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			
			scheduler.scheduleJob(job, trigger);
			// TODO register a JobListener and a TriggerListener with the
			// scheduler. This will raise Akka messages for Actors to consume.
		} catch (Exception e) {
			log.error("Error scheduling Channel", e);
			throw new SchedulerException("Error scheduling Channel", e);
		} finally {
			log.info("<< schedule()");
		}
	}
	
	private String getCronSchedule() throws Exception {
		return "0 */1 * * * ?";
	}
}
