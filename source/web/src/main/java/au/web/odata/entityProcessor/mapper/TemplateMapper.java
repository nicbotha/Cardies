package au.web.odata.entityProcessor.mapper;

import java.util.List;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.FileResource;
import au.model.entity.Template;
import au.model.entity.TemplateParameter;
import au.model.repository.TemplateCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class TemplateMapper implements Mapper<Template> {

	protected final Logger log = LoggerFactory.getLogger(TemplateMapper.class);

	public static final String NAME_P = "Name";
	public static final String DESCRIPTION_P = "Description";
	public static final String RESOURCE_P = "Resource";
	public static final String PARAMETERS_P = "Parameters";

	@Autowired
	protected FileResourceMapper fileResourceMapper;

	@Autowired
	protected TemplateParameterMapper templateParameterMapper;
	
	@Autowired
	protected TemplateCRUDRepository templateRepository;

	@Override
	public Template toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, Template.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(Template from) throws MappingException {
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(TemplateMapper.NAME_P, from.getName()));
			to.addProperty(createPrimitive(TemplateMapper.DESCRIPTION_P, from.getDescription()));
			to.setId(createId(ODataConst.TEMPLATE_ES_NAME, from.getId()));
			to.setType(ODataConst.TEMPLATE_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}

		return null;
	}

	@Override
	public void copyInto(Entity from, Template to) throws MappingException {
		if (from == null || to == null) {
			return;
		}

		Property nameProperty = from.getProperty(TemplateMapper.NAME_P);
		Property descriptionProperty = from.getProperty(TemplateMapper.DESCRIPTION_P);

		// NAME
		if (nameProperty != null) {
			to.setName((String) nameProperty.getValue());
		}

		// DESCRIPTION
		if (descriptionProperty != null) {
			to.setDescription((String) descriptionProperty.getValue());
		}

		for (Link link : from.getNavigationLinks()) {
			if (Constants.ENTITY_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				Entity linkedEntity = link.getInlineEntity();
				if (ODataConst.FILERESOURCE_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
					to.setResource(getResource(linkedEntity));
				}
			} else if (Constants.ENTITY_SET_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				for (Entity linkedEntity : link.getInlineEntitySet()) {
					if (ODataConst.TEMPLATEPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
						attachTemplateParameter(to.getParameters(),linkedEntity, to);
					}
				}
			}
		}
	}

	private void attachTemplateParameter(List<TemplateParameter> parameters, Entity linkedEntity, Template template) throws MappingException {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			TemplateParameter templateParameter = template.findOneTemplateParameter(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
			templateParameterMapper.copyInto(linkedEntity, templateParameter);
		} else {
			TemplateParameter templateParameter = templateParameterMapper.toJPAEntity(linkedEntity);
			templateParameter.setTemplate(template);
			parameters.add(templateParameter);
		}
	}

	private FileResource getResource(Entity linkedEntity) {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			return fileResourceMapper.findOne(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
		} else {
			return fileResourceMapper.toJPAEntity(linkedEntity);
		}
	}
	
	public Template findOne(String key) {
		return templateRepository.findOne(key);
	}

}
