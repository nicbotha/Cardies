package au.web.odata.entityProcessor.processor;

import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.CardParameter;
import au.model.entity.TemplateParameter;
import au.model.repository.TemplateParameterCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.CardParameterMapper;
import au.web.odata.entityProcessor.mapper.MappingException;
import au.web.odata.entityProcessor.mapper.TemplateParameterMapper;

@Component
public class TemplateParameterProcessor implements EntityProcessor<TemplateParameterCRUDRepository, TemplateParameter> {

	@Autowired
	protected TemplateParameterCRUDRepository repository;
	
	@Autowired
	protected TemplateParameterMapper mapper;
	
	@Autowired
	protected CardParameterMapper cardParameterMapper;
	
	@Override
	public String getEntitySetName() {
		return ODataConst.TEMPLATEPARAMETER_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.TEMPLATEPARAMETER_ET_NAME;
	}

	@Override
	public TemplateParameterCRUDRepository getRepository() {
		return repository;
	}

	@Override
	public TemplateParameterMapper getMapper() {
		return mapper;
	}
	
	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		final String from = navigateFromEdmEntityType.getName();
		
		if(ODataConst.TEMPLATE_ET_NAME.equals(from)) {
			EntityCollection entities = new EntityCollection();
			Iterable<TemplateParameter> data = getRepository().findByTemplateId(keys.get(0).getText());

			IteratorUtils.forEach(data.iterator(), e -> {

				try {
					entities.getEntities().add(getMapper().toOlingoEntity(e));
				} catch (MappingException ex) {
					log.error("MappingException", ex);
				}
			});
			
			return entities;
		}else {
			throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	@Override
	public Entity findOneRelated(final Entity sourceEntity, final List<UriParameter> keys) throws ODataApplicationException{
		
		if(ODataConst.TEMPLATE_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String templateId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			String templateParameterId = keys.get(0).getText();		
			
			TemplateParameter templateParameter = getRepository().findOne(templateParameterId);
			
			if(templateParameter == null) {
				return null;
			}
			
			if(templateId.equals(templateParameter.getTemplate().getId())){
				try {
					return getMapper().toOlingoEntity(templateParameter);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}else if(ODataConst.CARDPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String cardParameterId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			CardParameter cp = this.cardParameterMapper.findOne(cardParameterId);
			try {
				return this.mapper.toOlingoEntity(cp.getTemplateParameter());
			} catch (MappingException e) {
				log.error("Could not find TemplateParameter.", e);
				throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

}
