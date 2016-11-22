package au.web.odata.entityProcessor.processor;

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.Card;
import au.model.entity.CardParameter;
import au.model.repository.CardCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.CardMapper;
import au.web.odata.entityProcessor.mapper.CardParameterMapper;
import au.web.odata.entityProcessor.mapper.Mapper;
import au.web.odata.entityProcessor.mapper.MappingException;

@Component
public class CardProcessor implements EntityProcessor<CardCRUDRepository, Card> {

	@Autowired
	protected CardCRUDRepository repositoy;
	
	@Autowired
	protected CardMapper mapper;
	
	@Autowired
	protected CardParameterMapper cardParameterMapper;
	
	@Override
	public String getEntitySetName() {
		return ODataConst.CARD_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CARD_ET_NAME;
	}

	@Override
	public CardCRUDRepository getRepository() {
		return this.repositoy;
	}

	@Override
	public Mapper<Card> getMapper() {
		return this.mapper;
	}

	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Entity findOneRelated(Entity sourceEntity, List<UriParameter> keys) throws ODataApplicationException {
		if (ODataConst.CARDPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			CardParameter cardParameter = cardParameterMapper.findOne(String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));

			if (cardParameter == null) {
				throw new ODataApplicationException("No entity found for this key.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}

			try {
				return getMapper().toOlingoEntity(cardParameter.getCard());
			} catch (MappingException ex) {
				log.error("MappingException", ex);
				throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		} else if (ODataConst.TEMPLATE_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String templateId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			String cardId = keys.get(0).getText();

			Card template = getRepository().findOne(cardId);

			if (template == null) {
				return null;
			}

			if (templateId.equals(template.getTemplate().getId())) {
				try {
					return getMapper().toOlingoEntity(template);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		} else if (ODataConst.CHANNEL_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String channelId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			String cardId = keys.get(0).getText();

			Card card = getRepository().findOne(cardId);

			if (card == null) {
				return null;
			}

			if (channelId.equals(card.getChannel().getId())) {
				try {
					return getMapper().toOlingoEntity(card);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}

		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

}
