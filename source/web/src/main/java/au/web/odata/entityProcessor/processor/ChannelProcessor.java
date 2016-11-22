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
import au.model.entity.Channel;
import au.model.entity.ChannelAttribute;
import au.model.repository.ChannelCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.CardMapper;
import au.web.odata.entityProcessor.mapper.ChannelAttributeMapper;
import au.web.odata.entityProcessor.mapper.ChannelMapper;
import au.web.odata.entityProcessor.mapper.Mapper;
import au.web.odata.entityProcessor.mapper.MappingException;

@Component
public class ChannelProcessor implements EntityProcessor<ChannelCRUDRepository, Channel>{

	@Autowired
	protected ChannelCRUDRepository repository;
	
	@Autowired
	protected ChannelMapper mapper;
	
	@Autowired
	protected ChannelAttributeMapper channelAttributeMapper;
	
	@Autowired
	protected CardMapper cardMapper;
		
	@Override
	public String getEntitySetName() {
		return ODataConst.CHANNEL_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CHANNEL_ET_NAME;
	}

	@Override
	public ChannelCRUDRepository getRepository() {
		return this.repository;
	}

	@Override
	public Mapper<Channel> getMapper() {
		return this.mapper;
	}

	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Entity findOneRelated(Entity sourceEntity, List<UriParameter> keys) throws ODataApplicationException {
		if (ODataConst.CHANNELATTRIBUTE_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			ChannelAttribute channelAttribute = channelAttributeMapper.findOne(String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));

			if (channelAttribute == null) {
				throw new ODataApplicationException("No entity found for this key.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}

			try {
				return getMapper().toOlingoEntity(channelAttribute.getChannel());
			} catch (MappingException ex) {
				log.error("MappingException", ex);
				throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		} else if (ODataConst.CARD_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			Card card = cardMapper.findOne(String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));

			if (card == null) {
				throw new ODataApplicationException("No entity found for this key.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}

			try {
				return getMapper().toOlingoEntity(card.getChannel());
			} catch (MappingException ex) {
				log.error("MappingException", ex);
				throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		} 

		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

}
