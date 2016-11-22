package au.web.service.template;

import java.util.Map;

public interface TemplateService {
	
	public void setModel(Map<String, Object> model);
	
	public byte[] generateTemplate(au.model.entity.Template template);

	public byte[] findOneById(String templateId);
}
