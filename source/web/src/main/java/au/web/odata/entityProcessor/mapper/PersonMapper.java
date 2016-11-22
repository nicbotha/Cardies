package au.web.odata.entityProcessor.mapper;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.Person;
import au.model.repository.PersonCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class PersonMapper implements Mapper<Person> {
	
	protected final Logger log = LoggerFactory.getLogger(PersonMapper.class);
	
	public static final String NAME_P = "Name";
	public static final String EMAIL_P = "Email";
	
	@Autowired
	protected PersonCRUDRepository repository;

	@Override
	public Person toJPAEntity(Entity from) throws MappingException {
		try {
			return toJPAEntity(from, Person.class);
		} catch (MappingException e) {
			log.error("Mapping error={}", e);
			throw e;
		}
	}

	@Override
	public Entity toOlingoEntity(Person from) throws MappingException {
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(PersonMapper.NAME_P, from.getName()));
			to.addProperty(createPrimitive(PersonMapper.EMAIL_P, from.getEmail()));
			to.setId(createId(ODataConst.PERSON_ES_NAME, from.getId()));
			to.setType(ODataConst.PERSON_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}

		return null;
	}

	@Override
	public void copyInto(Entity from, Person to) throws MappingException {
		if (from == null || to == null) {
			return;
		}

		Property nameProperty = from.getProperty(PersonMapper.NAME_P);
		Property emailProperty = from.getProperty(PersonMapper.EMAIL_P);

		// NAME
		if (nameProperty != null) {
			to.setName((String) nameProperty.getValue());
		}

		// TYPE
		if (emailProperty != null) {
			to.setEmail((String) emailProperty.getValue());
		}
		
	}
	
	public Person findOne(String id) {
		return repository.findOne(id);
	}

}
