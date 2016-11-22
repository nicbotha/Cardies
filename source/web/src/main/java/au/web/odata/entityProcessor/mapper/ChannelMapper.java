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

import au.model.entity.Channel;
import au.model.entity.ChannelAttribute;
import au.model.entity.ChannelType;
import au.model.repository.ChannelCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class ChannelMapper implements Mapper<Channel> {

	protected final Logger log = LoggerFactory.getLogger(ChannelMapper.class);

	public static final String NAME_P = "Name";
	public static final String TYPE_P = "Type";

	@Autowired
	protected ChannelAttributeMapper channelAttributeMapper;

	@Autowired
	protected ChannelCRUDRepository repository;

	@Override
	public Channel toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, Channel.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(Channel from) throws MappingException {
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(ChannelMapper.NAME_P, from.getName()));
			to.addProperty(createPrimitive(ChannelMapper.TYPE_P, from.getType()));
			to.setId(createId(ODataConst.CHANNEL_ES_NAME, from.getId()));
			to.setType(ODataConst.CHANNEL_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}

		return null;
	}

	@Override
	public void copyInto(Entity from, Channel to) throws MappingException {
		if (from == null || to == null) {
			return;
		}

		Property nameProperty = from.getProperty(ChannelMapper.NAME_P);
		Property typeProperty = from.getProperty(ChannelMapper.TYPE_P);

		// NAME
		if (nameProperty != null) {
			to.setName((String) nameProperty.getValue());
		}

		// TYPE
		if (typeProperty != null) {
			to.setType(ChannelType.valueOf((String) typeProperty.getValue()));
		}

		for (Link link : from.getNavigationLinks()) {
			if (Constants.ENTITY_SET_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				for (Entity linkedEntity : link.getInlineEntitySet()) {
					if (ODataConst.CHANNELATTRIBUTE_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
						attachAttributeParameter(to.getAttributes(), linkedEntity, to);
					}
				}
			}
		}
	}

	private void attachAttributeParameter(List<ChannelAttribute> attributes, Entity linkedEntity, Channel channel) throws MappingException {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			ChannelAttribute channelAttribute = channel.findOneChannelAttribute(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
			channelAttributeMapper.copyInto(linkedEntity, channelAttribute);
		} else {
			ChannelAttribute channelAttribute = channelAttributeMapper.toJPAEntity(linkedEntity);
			channelAttribute.setChannel(channel);
			attributes.add(channelAttribute);
		}
	}

	public Channel findOne(String id) {
		return this.repository.findOne(id);
	}

}
