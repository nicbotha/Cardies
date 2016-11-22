package au.web.service.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.ParameterType;
import au.model.entity.TemplateParameter;
import au.model.repository.TemplateCRUDRepository;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

@Component(value = "TemplateService")
public class TemplateServiceImpl implements TemplateService {
	protected final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);

	public static final String MODEL_ATTACHMENT = "Attachment";
	public static final String MODEL_RECEIVER = "Receiver";
	
	@Autowired
	protected TemplateCRUDRepository repository;

	private Map<String, Object> model;

	private Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);

	@Autowired
	public TemplateServiceImpl(CMISFreemarkerTemplateLoader cmisTemplateLoader) {
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setTemplateLoader(cmisTemplateLoader);
	}

	@Override
	public byte[] findOneById(String templateId) {

		au.model.entity.Template templateEntity = getTemplateEntity(templateId);

		Map<String, Object> root = new HashMap<>();
		Map<String, Object> attachments = new HashMap<>();
		for (TemplateParameter templateParameter : templateEntity.getParameters()) {
			if (templateParameter.getType().equals(ParameterType.ATTACHMENT)) {
				attachments.put(templateParameter.getName(), "http://localhost:8080/web/fileresource?ID=" + templateParameter.getDefaultValues());
			} else {
				root.put(templateParameter.getName(), templateParameter.getDefaultValues());
			}
			// If TYPE=ATTACHEMENT
			// http://localhost:8080/web/fileresource?ID=B_ZfJGw1Sp-Cx7r-iDeD80FQqHsFrIu3zHCazKWdCbI"
		}

		// Just hard code provided parameters
		Map<String, Object> receiver = new HashMap<>();
		receiver.put("Name", "[Receiver.Name]");

		root.put(MODEL_RECEIVER, receiver);
		root.put(MODEL_ATTACHMENT, attachments);

		try {
			Template temp = cfg.getTemplate(templateEntity.getResource().getDocStoreId());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(baos);
			temp.process(root, out);
			return baos.toByteArray();
		} catch (TemplateNotFoundException e) {
			log.error("Template not found.", e);
		} catch (MalformedTemplateNameException e) {
			log.error("Malformed Template name.", e);
		} catch (ParseException e) {
			log.error("Template parse error.", e);
		} catch (IOException e) {
			log.error("Template read error.", e);
		} catch (TemplateException e) {
			log.error("Template generation error.", e);
		}

		return null;
	}

	public au.model.entity.Template getTemplateEntity(String id) {
		return repository.findOne(id);
	}

	@Override
	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

	@Override
	public byte[] generateTemplate(au.model.entity.Template template) {

		try {
			Template temp = cfg.getTemplate(template.getResource().getDocStoreId());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(baos);
			temp.process(model, out);
			return baos.toByteArray();
		} catch (TemplateNotFoundException e) {
			log.error("Template not found.", e);
		} catch (MalformedTemplateNameException e) {
			log.error("Malformed Template name.", e);
		} catch (ParseException e) {
			log.error("Template parse error.", e);
		} catch (IOException e) {
			log.error("Template read error.", e);
		} catch (TemplateException e) {
			log.error("Template generation error.", e);
		}

		return null;
	}

}
