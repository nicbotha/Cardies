package au.model.repository;

import java.util.Arrays;

import au.model.entity.Card;
import au.model.entity.CardParameter;
import au.model.entity.Channel;
import au.model.entity.ChannelAttribute;
import au.model.entity.ChannelAttributeType;
import au.model.entity.ChannelType;
import au.model.entity.FileResource;
import au.model.entity.FileResourceType;
import au.model.entity.ParameterType;
import au.model.entity.Person;
import au.model.entity.Template;
import au.model.entity.TemplateParameter;

public class EntityTestHelper {
	
	public static FileResource newFileResource() {
		FileResource entity = new FileResource();
		entity.setName("name");
		entity.setDescription("description");
		entity.setDocStoreId("docStoreId");
		entity.setTags("tags");
		entity.setType(FileResourceType.HTML);
		return entity;
	}
	
	public static TemplateParameter newTemplateParameter(Template template) {
		TemplateParameter entity = new TemplateParameter();
		entity.setDefaultValues("defaultValues");
		entity.setName("name");
		entity.setDescription("description");
		entity.setType(ParameterType.TEXT);
		entity.setTemplate(template);
		return entity;
	}
	
	public static Template newTemplate() {
		Template entity = new Template();
		entity.setName("name");
		entity.setDescription("description");
		entity.setResource(newFileResource());
		return entity;
	}
	
	public static Person newPerson() {
		Person entity = new Person();
		entity.setName("name");
		entity.setEmail("email");
		return entity;
	}
	
	public static Card newCard() {
		Card entity = new Card();
		entity.setName("name");
		entity.setPublishDate("publishDate");
		entity.setTemplate(newTemplate());
		entity.setChannel(newChannel());
		return entity;
	}
	
	public static CardParameter newCardParameter(Card card, TemplateParameter templateParameter) {
		CardParameter entity = new CardParameter();
		entity.setValue("value");
		entity.setCard(card);
		entity.setTemplateParameter(templateParameter);
		return entity;
	}
	
	public static Channel newChannel() {
		Channel entity = new Channel();
		entity.setName("name");
		entity.setType(ChannelType.EMAIL);
		entity.setAttributes(Arrays.asList(newChannelAttribute(entity)));
		return entity;
	}
	
	public static ChannelAttribute newChannelAttribute(Channel channel) {
		ChannelAttribute entity = new ChannelAttribute();
		
		entity.setIdentifier("key");
		entity.setValue("value");
		entity.setType(ChannelAttributeType.USERNAME);
		entity.setChannel(channel);
		return entity;
	}
}
