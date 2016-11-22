package au.web.odata.entityProcessor.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.Card;
import au.model.entity.CardParameter;
import au.model.entity.Channel;
import au.model.entity.Person;
import au.model.entity.Template;
import au.model.repository.CardCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class CardMapper implements Mapper<Card> {

	protected final Logger log = LoggerFactory.getLogger(CardMapper.class);
	public static final String NAME_P = "Name";
	public static final String PUBLISHDATE_P = "PublishDate";
	
	@Autowired
	protected CardCRUDRepository repository;
	
	@Autowired
	protected CardParameterMapper cardParameterMapper;
	
	@Autowired
	protected TemplateMapper templateMapper;
	
	@Autowired
	protected ChannelMapper channelMapper;
	
	@Autowired
	protected PersonMapper personMapper;
	

	@Override
	public Card toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, Card.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(Card from) throws MappingException {
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(CardMapper.NAME_P, from.getName()));
			to.addProperty(createPrimitive(CardMapper.PUBLISHDATE_P, from.getPublishDate()));
			to.setId(createId(ODataConst.CARD_ES_NAME, from.getId()));
			to.setType(ODataConst.CARD_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}
		
		return null;
	}

	@Override
	public void copyInto(Entity from, Card to) throws MappingException {
		if (from == null || to == null) {
			return;
		}

		Property nameProperty = from.getProperty(CardMapper.NAME_P);
		Property publishDateProperty = from.getProperty(CardMapper.PUBLISHDATE_P);

		// NAME
		if (nameProperty != null) {
			to.setName((String) nameProperty.getValue());
		}
		
		//PUBLISH DATE
		if(publishDateProperty != null) {
			to.setPublishDate((String) publishDateProperty.getValue()); 
		}
		
		//This should not matter because there will never be a dependency on CardParameter.
		for(CardParameter cardParameter : to.getCardParameters()) {
			cardParameterMapper.delete(cardParameter.getId());
		}
		to.setCardParameters(new ArrayList<CardParameter>());
		to.setReceivers(new ArrayList<Person>());

		for (Link link : from.getNavigationLinks()) {
			if (Constants.ENTITY_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				Entity linkedEntity = link.getInlineEntity();
				if (ODataConst.TEMPLATE_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
					to.setTemplate(getTemplate(linkedEntity));
				}else if (ODataConst.CHANNEL_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
					to.setChannel(getChannel(linkedEntity));
				}
			} else if (Constants.ENTITY_SET_NAVIGATION_LINK_TYPE.equals(link.getType())) {
				for (Entity linkedEntity : link.getInlineEntitySet()) {
					if (ODataConst.CARDPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
						attachCardParameter(to.getCardParameters(),linkedEntity, to);
					}else if (ODataConst.PERSON_ET_FQN.getFullQualifiedNameAsString().equals(linkedEntity.getType())) {
						attachPerson(linkedEntity, to);
					}
				}
			}
		}
		
	}

	private void attachPerson(Entity linkedEntity, Card to) {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			Person person = personMapper.findOne(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
			to.getReceivers().add(person);
		}
	}

	private void attachCardParameter(List<CardParameter> cardParameters, Entity linkedEntity, Card card) throws MappingException{
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			CardParameter cardParameter = card.findOneCardParameter(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
			cardParameterMapper.copyInto(linkedEntity, cardParameter);
		} else {
			CardParameter cardParameter = cardParameterMapper.toJPAEntity(linkedEntity);
			cardParameter.setCard(card);
			cardParameters.add(cardParameter);
		}
	}
	
	private Template getTemplate(Entity linkedEntity) throws MappingException {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			return templateMapper.findOne(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
		} else {
			return templateMapper.toJPAEntity(linkedEntity);
		}
	}
	
	private Channel getChannel(Entity linkedEntity) throws MappingException {
		if (linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY) != null) {
			return channelMapper.findOne(String.valueOf(linkedEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));
		} else {
			return channelMapper.toJPAEntity(linkedEntity);
		}
	}
	
	public Card findOne(String id) {
		return this.repository.findOne(id);
	}
	
}
