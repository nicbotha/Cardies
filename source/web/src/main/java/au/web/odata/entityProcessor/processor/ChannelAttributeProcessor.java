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

import au.model.entity.ChannelAttribute;
import au.model.repository.ChannelAttributeCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.ChannelAttributeMapper;
import au.web.odata.entityProcessor.mapper.Mapper;
import au.web.odata.entityProcessor.mapper.MappingException;

@Component
public class ChannelAttributeProcessor implements EntityProcessor<ChannelAttributeCRUDRepository, ChannelAttribute> {

	@Autowired
	protected ChannelAttributeCRUDRepository repository;

	@Autowired
	protected ChannelAttributeMapper mapper;

	@Override
	public String getEntitySetName() {
		return ODataConst.CHANNELATTRIBUTE_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CHANNELATTRIBUTE_ET_NAME;
	}

	@Override
	public ChannelAttributeCRUDRepository getRepository() {
		return this.repository;
	}

	@Override
	public Mapper<ChannelAttribute> getMapper() {
		return this.mapper;
	}

	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		final String from = navigateFromEdmEntityType.getName();

		if (ODataConst.CHANNEL_ET_NAME.equals(from)) {
			EntityCollection entities = new EntityCollection();
			Iterable<ChannelAttribute> data = getRepository().findByChannelId(keys.get(0).getText());

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
		if(ODataConst.CHANNEL_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String channelId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			String channelAttributeId = keys.get(0).getText();		
			
			ChannelAttribute channelAttribute = getRepository().findOne(channelAttributeId);
			
			if(channelAttribute == null) {
				return null;
			}
			
			if(channelId.equals(channelAttribute.getChannel().getId())){
				try {
					return getMapper().toOlingoEntity(channelAttribute);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}
		
		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

}
