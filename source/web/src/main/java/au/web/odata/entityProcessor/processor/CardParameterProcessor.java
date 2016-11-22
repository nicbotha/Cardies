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
import au.model.repository.CardParameterCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.CardParameterMapper;
import au.web.odata.entityProcessor.mapper.Mapper;
import au.web.odata.entityProcessor.mapper.MappingException;

@Component
public class CardParameterProcessor implements EntityProcessor<CardParameterCRUDRepository, CardParameter> {

	@Autowired
	protected CardParameterCRUDRepository repository;

	@Autowired
	protected CardParameterMapper mapper;

	@Override
	public String getEntitySetName() {
		return ODataConst.CARDPARAMETER_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CARDPARAMETER_ET_NAME;
	}

	@Override
	public CardParameterCRUDRepository getRepository() {
		return this.repository;
	}

	@Override
	public Mapper<CardParameter> getMapper() {
		return this.mapper;
	}

	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		final String from = navigateFromEdmEntityType.getName();

		if (ODataConst.CARD_ET_NAME.equals(from)) {
			EntityCollection entities = new EntityCollection();
			Iterable<CardParameter> data = getRepository().findByCardId(keys.get(0).getText());

			IteratorUtils.forEach(data.iterator(), e -> {

				try {
					entities.getEntities().add(getMapper().toOlingoEntity(e));
				} catch (MappingException ex) {
					log.error("MappingException", ex);
				}
			});

			return entities;
		} else {
			throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	}

	@Override
	public Entity findOneRelated(Entity sourceEntity, List<UriParameter> keys) throws ODataApplicationException {
		String cardParameterId = keys.get(0).getText();		
		
		CardParameter cardParameter = getRepository().findOne(cardParameterId);
		
		if(cardParameter == null) {
			return null;
		}
		
		if(ODataConst.CARD_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String cardId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			
			if(cardId.equals(cardParameter.getCard().getId())){
				try {
					return getMapper().toOlingoEntity(cardParameter);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}else if(ODataConst.TEMPLATEPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String templateParameterId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			
			if(templateParameterId.equals(cardParameter.getTemplateParameter().getId())){
				try {
					return getMapper().toOlingoEntity(cardParameter);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}
		
		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

}
