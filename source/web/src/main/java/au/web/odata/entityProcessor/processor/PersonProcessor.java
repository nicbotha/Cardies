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

import au.model.entity.Person;
import au.model.repository.PersonCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.Mapper;
import au.web.odata.entityProcessor.mapper.MappingException;
import au.web.odata.entityProcessor.mapper.PersonMapper;

@Component
public class PersonProcessor implements EntityProcessor<PersonCRUDRepository, Person> {

	@Autowired
	private PersonCRUDRepository repository;
	
	@Autowired
	private PersonMapper mapper;
	
	@Override
	public String getEntitySetName() {
		return ODataConst.PERSON_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.PERSON_ET_NAME;
	}

	@Override
	public PersonCRUDRepository getRepository() {
		return this.repository;
	}

	@Override
	public Mapper<Person> getMapper() {
		return this.mapper;
	}

	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		final String from = navigateFromEdmEntityType.getName();

		if (ODataConst.CARD_ET_NAME.equals(from)) {
			EntityCollection entities = new EntityCollection();
			Iterable<Person> data = getRepository().findByCardId(keys.get(0).getText());

			IteratorUtils.forEach(data.iterator(), e -> {

				try {
					entities.getEntities().add(getMapper().toOlingoEntity(e));
				} catch (MappingException ex) {
					log.error("MappingException", ex);
				}
			});

			return entities;
		} else {
			throw new ODataApplicationException("Unknown Card Entity relationship for "+ from, HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	}

	@Override
	public Entity findOneRelated(Entity sourceEntity, List<UriParameter> keys) throws ODataApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

}
