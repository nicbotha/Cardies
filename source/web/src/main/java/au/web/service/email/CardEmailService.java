package au.web.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import au.model.entity.Card;
import au.model.entity.CardParameter;
import au.model.entity.Channel;
import au.model.entity.ParameterType;
import au.model.entity.Person;
import au.model.entity.TemplateParameter;
import au.web.service.cmis.CMISService;
import au.web.service.template.TemplateService;
import au.web.service.template.TemplateServiceImpl;

@Component
public class CardEmailService {
	protected final Logger log = LoggerFactory.getLogger(CardEmailService.class);

	@Autowired
	protected TemplateService templateService;

	@Autowired
	protected CMISService cmisService;

	public void execute(final Channel channel, final Card card) {
		log.info(">> execute(Card={})", card);

		for (Person person : card.getReceivers()) {
			if (allowSend(card, person)) {
				Map<String, Object> model = buildTemplateModel(card, person);
				byte[] template = generateTemplate(card, model);
				sendEmail(channel, card, person, template);
			}
		}
	}

	/**
	 * Contains all logic to check if a person should receive an email.
	 */
	private boolean allowSend(Card card, Person person) {
		return true;
	}

	private Map<String, Object> buildTemplateModel(Card card, Person person) {
		Map<String, Object> root = new HashMap<>();
		Map<String, Object> attachments = new HashMap<>();
		Map<String, Object> receiver = new HashMap<>();
		
		receiver.put("Name", person.getName());

		// Card parameters
		for (CardParameter cp : card.getCardParameters()) {
			if (cp.getTemplateParameter().getType().equals(ParameterType.ATTACHMENT)) {
				attachments.put(cp.templateParameter.getName(), "cid:" + cp.getTemplateParameter().defaultValues);
			} else {
				root.put(cp.getTemplateParameter().getName(), nullCheck(cp.getValue(), cp.getTemplateParameter().defaultValues));
			}
		}

		root.put(TemplateServiceImpl.MODEL_RECEIVER, receiver);
		root.put(TemplateServiceImpl.MODEL_ATTACHMENT, attachments);

		return root;
	}

	private byte[] generateTemplate(Card card, Map<String, Object> model) {
		templateService.setModel(model);
		return templateService.generateTemplate(card.getTemplate());
	}

	private void sendEmail(Channel channel, Card card, Person receiver, byte[] content) {
		MimeMessagePreparator mmp = getMessagePreparator(card, receiver, content);
		JavaMailSender mailSender = getMailSender();

		mailSender.send(mmp);
	}

	private String nullCheck(String value, String ifNull) {
		if (StringUtils.isEmpty(value)) {
			return ifNull;
		} else {
			return value;
		}
	}

	private MimeMessagePreparator getMessagePreparator(final Card card, final Person receiver, final byte[] content) {

		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

				helper.setSubject(card.getName());
				helper.setFrom("no-reply@cardies.com");
				helper.setTo(receiver.getEmail());

				// use the true flag to indicate you need a multipart message
				MimeBodyPart part = new MimeBodyPart();
				part.setContent(new String(content, "UTF-8"), "text/html");
				//helper.setText(new String(content, "UTF-8"), true);
				
				List<MimeBodyPart> attachments = new ArrayList<>();

				// Additionally, let's add a resource as an attachment as well.
				for (TemplateParameter tp : card.getTemplate().getParameters()) {
					if (tp.getType().equals(ParameterType.ATTACHMENT)) {
						Object attachment = cmisService.getObjectById(tp.getDefaultValues());
						Document document = (Document) attachment;
						
						byte[] src = IOUtils.toByteArray(document.getContentStream().getStream());
						
						MimeBodyPart imagePart = new MimeBodyPart();
						ByteArrayDataSource fds = new ByteArrayDataSource(src,document.getContentStreamMimeType());
						imagePart.setDataHandler(new DataHandler(fds));
						imagePart.setHeader("Content-ID", "<"+tp.getDefaultValues()+">");
						imagePart.setDisposition(Part.INLINE);
						attachments.add(imagePart);
					}
				}


				Multipart mp = new MimeMultipart();

				mp.addBodyPart(part);
				for(MimeBodyPart mdp: attachments) {
					mp.addBodyPart(mdp);
				}

				// Set the message's content
				mimeMessage.setContent(mp);
			}
		};
		return preparator;
	}

	private JavaMailSender getMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		// Using gmail.
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("");
		mailSender.setPassword("");

		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.starttls.enable", "true");
		javaMailProperties.put("mail.smtp.auth", "true");
		javaMailProperties.put("mail.transport.protocol", "smtp");
		javaMailProperties.put("mail.debug", "true");

		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}
}
