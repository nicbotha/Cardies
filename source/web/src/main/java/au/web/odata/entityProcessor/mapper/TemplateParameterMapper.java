package au.web.odata.entityProcessor.mapper;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.ParameterType;
import au.model.entity.TemplateParameter;
import au.model.repository.TemplateParameterCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class TemplateParameterMapper implements Mapper<TemplateParameter> {
	protected final Logger log = LoggerFactory.getLogger(TemplateParameterMapper.class);
	public static final String NAME_P = "Name";
	public static final String DESCRIPTION_P = "Description";
	public static final String DEFAULTVALUES_P = "DefaultValues";
	public static final String TYPE_P = "Type";
	
	@Autowired
	protected TemplateParameterCRUDRepository repository;

	@Override
	public TemplateParameter toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, TemplateParameter.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(TemplateParameter from) throws MappingException {

		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(TemplateParameterMapper.NAME_P, from.getName()));
			to.addProperty(createPrimitive(TemplateParameterMapper.DESCRIPTION_P, from.getDescription()));
			to.addProperty(createPrimitive(TemplateParameterMapper.DEFAULTVALUES_P, from.getDefaultValues()));
			to.addProperty(createPrimitive(TemplateParameterMapper.TYPE_P, from.getType()));
			to.setId(createId(ODataConst.TEMPLATEPARAMETER_ES_NAME, from.getId()));
			to.setType(ODataConst.TEMPLATEPARAMETER_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}

		return null;
	}

	@Override
	public void copyInto(Entity from, TemplateParameter to) throws MappingException{

		if (from == null || to == null) {
			return;
		}

		Property nameProperty = from.getProperty(TemplateParameterMapper.NAME_P);
		Property descriptionProperty = from.getProperty(TemplateParameterMapper.DESCRIPTION_P);
		Property defaultValuesProperty = from.getProperty(TemplateParameterMapper.DEFAULTVALUES_P);
		Property typeProperty = from.getProperty(TemplateParameterMapper.TYPE_P);

		// NAME
		if (nameProperty != null) {
			to.setName((String) nameProperty.getValue());
		}

		// DESCRIPTION
		if (descriptionProperty != null) {
			to.setDescription((String) descriptionProperty.getValue());
		}
		
		// DEFAULTVALUES
		if (defaultValuesProperty != null) {
			to.setDefaultValues((String) defaultValuesProperty.getValue());
		}
		
		// TYPE
		if (typeProperty != null) {
			to.setType(ParameterType.valueOf((String) typeProperty.getValue()));
		}

	}
	
	public TemplateParameter findOne(String id) {
		return repository.findOne(id);
	}

}
