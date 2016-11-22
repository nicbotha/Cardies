package au.web.service.scheduler.job;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import au.model.entity.Card;
import au.model.entity.Channel;
import au.model.entity.ChannelType;
import au.model.repository.CardCRUDRepository;
import au.model.repository.ChannelCRUDRepository;
import au.web.ApplicationTenantContext;
import au.web.service.email.CardEmailService;

public class ChannelJob implements Job {
	protected final Logger log = LoggerFactory.getLogger(ChannelJob.class);

	public static final String TENANT = "message";

	@Autowired
	public ChannelCRUDRepository channelRepository;
	
	@Autowired
	public CardCRUDRepository cardRepository;
	
	@Autowired
	public CardEmailService cardEmailService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info(">> execute(JobExecutionContext=" + context + ")");

		if (context.getJobDetail().getJobDataMap().containsKey(TENANT)) {
			setThreadTenant(context.getJobDetail().getJobDataMap().get(TENANT));
			
			List<Channel> channels = (List<Channel>) channelRepository.findAll();
			
			for(Channel channel: channels) {
				if(channel.getType().equals(ChannelType.EMAIL)) {
					triggerEmailCards(channel);
				}
			}
						
		}		

		log.info("<< execute()");
	}

	private void triggerEmailCards(Channel channel) {
		List<Card> cards = cardRepository.findByChannelId(channel.getId());
		
		for(Card c: cards) {
			cardEmailService.execute(channel, c);
		}		
	}

	private void setThreadTenant(Object tenant) {
		String s = (String) tenant;
		InitialContext ctx;
		try {
			ctx = new InitialContext();

			Context envCtx = (Context) ctx.lookup("java:comp/env");
			ApplicationTenantContext atContext = new ApplicationTenantContext(s);
			envCtx.addToEnvironment("TenantContext", atContext);
		} catch (NamingException e) {
		}

	}

}
