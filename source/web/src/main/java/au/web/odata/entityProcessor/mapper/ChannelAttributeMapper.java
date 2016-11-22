package au.web.odata.entityProcessor.mapper;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.ChannelAttribute;
import au.model.entity.ChannelAttributeType;
import au.model.repository.ChannelAttributeCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class ChannelAttributeMapper implements Mapper<ChannelAttribute> {

	protected final Logger log = LoggerFactory.getLogger(ChannelAttributeMapper.class);
	
	public static final String IDENTIFIER_P = "Identifier";
	public static final String VALUE_P = "Value";
	public static final String TYPE_P = "Type";
	
	@Autowired
	protected ChannelAttributeCRUDRepository repository;
	
	@Override
	public ChannelAttribute toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, ChannelAttribute.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(ChannelAttribute from) throws MappingException {
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(ChannelAttributeMapper.IDENTIFIER_P, from.getIdentifier()));
			to.addProperty(createPrimitive(ChannelAttributeMapper.VALUE_P, from.getValue()));
			to.addProperty(createPrimitive(ChannelAttributeMapper.TYPE_P, from.getType()));
			to.setId(createId(ODataConst.CHANNELATTRIBUTE_ES_NAME, from.getId()));
			to.setType(ODataConst.CHANNELATTRIBUTE_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}

		return null;
	}

	@Override
	public void copyInto(Entity from, ChannelAttribute to) throws MappingException {
		if (from == null || to == null) {
			return;
		}

		Property identifierProperty = from.getProperty(ChannelAttributeMapper.IDENTIFIER_P);
		Property valueProperty = from.getProperty(ChannelAttributeMapper.VALUE_P);
		Property typeProperty = from.getProperty(ChannelAttributeMapper.TYPE_P);

		// NAME
		if (identifierProperty != null) {
			to.setIdentifier((String) identifierProperty.getValue());
		}

		// VALUE
		if (valueProperty != null) {
			to.setValue((String) valueProperty.getValue());
		}
		
		// TYPE
		if (typeProperty != null) {
			to.setType(ChannelAttributeType.valueOf((String) typeProperty.getValue()));
		}
	}
	
	public ChannelAttribute findOne(String id) {
		return repository.findOne(id);
	}

}
