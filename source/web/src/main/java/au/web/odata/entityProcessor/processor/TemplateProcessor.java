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

import au.model.entity.Card;
import au.model.entity.Template;
import au.model.entity.TemplateParameter;
import au.model.repository.TemplateCRUDRepository;
import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.CardMapper;
import au.web.odata.entityProcessor.mapper.MappingException;
import au.web.odata.entityProcessor.mapper.TemplateMapper;
import au.web.odata.entityProcessor.mapper.TemplateParameterMapper;

@Component
public class TemplateProcessor implements EntityProcessor<TemplateCRUDRepository, Template> {

	@Autowired
	protected TemplateCRUDRepository repository;

	@Autowired
	protected TemplateParameterMapper templateParameterMapper;

	@Autowired
	protected TemplateMapper mapper;

	@Autowired
	protected CardMapper cardMapper;

	@Override
	public String getEntitySetName() {
		return ODataConst.TEMPLATE_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.TEMPLATE_ET_NAME;
	}

	@Override
	public TemplateCRUDRepository getRepository() {
		return repository;
	}

	@Override
	public TemplateMapper getMapper() {
		return mapper;
	}

	@Override
	public EntityCollection findAllBy(EdmEntityType navigateFromEdmEntityType, List<UriParameter> keys) throws ODataApplicationException {
		final String from = navigateFromEdmEntityType.getName();

		if (ODataConst.FILERESOURCE_ET_NAME.equals(from)) {
			EntityCollection entities = new EntityCollection();
			Iterable<Template> data = getRepository().findByFileResourceId(keys.get(0).getText());

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

		if (ODataConst.TEMPLATEPARAMETER_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			TemplateParameter templateParameter = templateParameterMapper.findOne(String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue()));

			if (templateParameter == null) {
				throw new ODataApplicationException("No entity found for this key.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}

			try {
				return getMapper().toOlingoEntity(templateParameter.getTemplate());
			} catch (MappingException ex) {
				log.error("MappingException", ex);
				throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		} else if (ODataConst.FILERESOURCE_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String fileresourceId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			String templateId = keys.get(0).getText();

			Template template = getRepository().findOne(templateId);

			if (template == null) {
				return null;
			}

			if (fileresourceId.equals(template.getResource().getId())) {
				try {
					return getMapper().toOlingoEntity(template);
				} catch (MappingException ex) {
					log.error("MappingException", ex);
					throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		} else if (ODataConst.CARD_ET_FQN.getFullQualifiedNameAsString().equals(sourceEntity.getType())) {
			String cardId = String.valueOf(sourceEntity.getProperty(ODataConst.JPA_ENTITY_PKEY).getValue());
			Card card = this.cardMapper.findOne(cardId);

			if (card == null) {
				return null;
			}

			try {
				return getMapper().toOlingoEntity(card.getTemplate());
			} catch (MappingException e) {
				log.error("MappingException", e);
				throw new ODataApplicationException("Could not map entity.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		}

		throw new ODataApplicationException("Unknown Entity relationship.", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	}

}
