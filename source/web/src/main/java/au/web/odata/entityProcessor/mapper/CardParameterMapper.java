package au.web.odata.entityProcessor.mapper;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.CardParameter;
import au.model.entity.TemplateParameter;
import au.model.repository.CardParameterCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class CardParameterMapper implements Mapper<CardParameter> {

	protected final Logger log = LoggerFactory.getLogger(CardParameterMapper.class);
	public static final String VALUE_P = "Value";
	
	@Autowired
	protected CardParameterCRUDRepository repository;
	
	@Autowired
	protected TemplateParameterMapper templateParameterMapper;
	
	@Override
	public CardParameter toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, CardParameter.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(CardParameter from) throws MappingException {
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(CardParameterMapper.VALUE_P, from.getValue()));
			to.setId(createId(ODataConst.CARDPARAMETER_ES_NAME, from.getId()));
			to.setType(ODataConst.CARDPARAMETER_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}

		return null;
	}

	@Override
	public void copyInto(Entity from, CardParameter to) throws MappingException {
		if (from == null || to == null) {
			return;
		}

		Property valueProperty = from.getProperty(CardParameterMapper.VALUE_P);

		// NAME
		if (valueProperty != null) {
			to.setValue((String) valueProperty.getValue());
		}
		
		for (Link link : from.getNavigationLinks()) {
			if (Constants.ENTITY_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				Entity linkedEntity = link.getInlineEntity();
				if (ODataConst.TEMPLATEPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
					to.setTemplateParameter(getTemplateParameter(linkedEntity));
				}
			} else if (Constants.ENTITY_SET_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				
			}
		}		
	}
	
	private TemplateParameter getTemplateParameter(Entity linkedEntity) throws MappingException {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			return templateParameterMapper.findOne(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
		} else {
			return templateParameterMapper.toJPAEntity(linkedEntity);
		}
	}

	public CardParameter findOne(String id) {
		return repository.findOne(id);
	}
	
	public void delete(String id) {
		repository.delete(id);
	}

}
