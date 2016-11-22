package au.web.odata.entityProcessor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.web.odata.ODataConst;
import au.web.odata.ODataUtil;

@Component("GenericEntityProcessor")
public class GenericEntityProcessor implements EntityCollectionProcessor, EntityProcessor, PrimitiveProcessor, PrimitiveValueProcessor, ComplexProcessor {

	protected final Logger log = LoggerFactory.getLogger(GenericEntityProcessor.class);

	@Autowired
	private ApplicationContext ctx;
	private OData odata;
	private ServiceMetadata serviceMetadata;

	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
		EdmEntitySet edmEntitySet = ODataUtil.getEdmEntitySet(uriInfo);
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();

		Entity preparedEntity = new Entity();

		preparedEntity.setType(requestEntity.getType());
		preparedEntity.getProperties().addAll(requestEntity.getProperties());

		addBindingLinks(request, edmEntitySet, edmEntityType, requestEntity, preparedEntity);

		addAssociatedLinks(request, edmEntitySet, edmEntityType, requestEntity, preparedEntity);

		/*--Creating the Entity--*/
		Entity createdEntity = createEntityInternal(edmEntitySet, preparedEntity);

		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);
		response.setContent(serializedResponse.getContent());
		response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	private void addAssociatedLinks(ODataRequest request, EdmEntitySet edmEntitySet, EdmEntityType edmEntityType, Entity requestEntity, Entity preparedEntity) throws ODataApplicationException {
		/*--Creating the associated entities--*/
		for (final Link link : requestEntity.getNavigationLinks()) {
			final EdmNavigationProperty edmNavigationProperty = edmEntityType.getNavigationProperty(link.getTitle());

			if (edmNavigationProperty.isCollection() && link.getInlineEntitySet() != null) {
				for (final Entity nestedEntity : link.getInlineEntitySet().getEntities()) {
					//final Entity newNestedEntity = nestedEntity;
					Entity newNestedPreparedEntity = new Entity();

					newNestedPreparedEntity.setType(nestedEntity.getType());
					newNestedPreparedEntity.getProperties().addAll(nestedEntity.getProperties());
					
					EdmEntitySet newNestedEntitySet = getEdmEntitySet(nestedEntity);
					addBindingLinks(request, newNestedEntitySet, newNestedEntitySet.getEntityType(), nestedEntity, newNestedPreparedEntity);
					addAssociatedLinks(request, newNestedEntitySet, newNestedEntitySet.getEntityType(), nestedEntity, newNestedPreparedEntity);
					//Carry on
					createLink(edmNavigationProperty, preparedEntity, newNestedPreparedEntity);
				}
			} else if (!edmNavigationProperty.isCollection() && link.getInlineEntity() != null) {
				final Entity newNestedEntity = link.getInlineEntity();
				createLink(edmNavigationProperty, preparedEntity, newNestedEntity);
			}
		}
	}

	private void addBindingLinks(ODataRequest request, EdmEntitySet edmEntitySet, EdmEntityType edmEntityType, Entity requestEntity, Entity preparedEntity) throws ODataApplicationException {
		/*--Add Binding links--*/
		for (final Link link : requestEntity.getNavigationBindings()) {
			final EdmNavigationProperty edmNavigationProperty = edmEntityType.getNavigationProperty(link.getTitle());
			final EdmEntitySet targetEntitySet = (EdmEntitySet) edmEntitySet.getRelatedBindingTarget(link.getTitle());

			if (edmNavigationProperty.isCollection() && link.getBindingLinks() != null) {
				for (final String bindingLink : link.getBindingLinks()) {
					final Entity relatedEntity = readEntityByBindingLink(bindingLink, targetEntitySet, request.getRawBaseUri());
					createLink(edmNavigationProperty, preparedEntity, relatedEntity);
				}
			} else if (!edmNavigationProperty.isCollection() && link.getBindingLink() != null) {
				final Entity relatedEntity = readEntityByBindingLink(link.getBindingLink(), targetEntitySet, request.getRawBaseUri());
				createLink(edmNavigationProperty, preparedEntity, relatedEntity);
			}
		}
	}
	
	private EdmEntitySet getEdmEntitySet(Entity newNestedEntity) {
		for (EdmEntitySet edmEntitySet : this.serviceMetadata.getEdm().getEntityContainer().getEntitySets()) {
			if (edmEntitySet.getEntityType().getFullQualifiedName().getFullQualifiedNameAsString().equals(newNestedEntity.getType())) {
				return edmEntitySet;
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	public void readEntityCollection(final ODataRequest request, ODataResponse response, final UriInfo uriInfo, final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
		EdmEntitySet responseEdmEntitySet = null;
		EntityCollection responseEntityCollection = null;
		List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		int segmentCount = resourceParts.size();

		UriResource uriResource = resourceParts.get(0);
		if (!(uriResource instanceof UriResourceEntitySet)) {
			throw new ODataApplicationException("Only EntitySet is supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}

		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
		EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		if (segmentCount == 1) {
			responseEdmEntitySet = startEdmEntitySet;

			for (String entityName : entityProcessors.keySet()) {
				au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

				if (entityProcessor.getEntitySetName().equals(startEdmEntitySet.getName())) {
					responseEntityCollection = entityProcessor.findAll();
					break;
				}
			}
		} else if (segmentCount == 2) {
			UriResource lastSegment = resourceParts.get(1);

			if (lastSegment instanceof UriResourceNavigation) {
				UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) lastSegment;
				EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
				List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

				// from Categories(1) to Products
				responseEdmEntitySet = ODataUtil.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);

				responseEntityCollection = findAllBy(responseEdmEntitySet.getEntityType(), startEdmEntitySet.getEntityType(), keyPredicates);
			}
		}

		ODataSerializer serializer = odata.createSerializer(requestedContentType);

		final ExpandOption expand = uriInfo.getExpandOption();
		final SelectOption select = uriInfo.getSelectOption();
		final String id = request.getRawBaseUri() + "/" + responseEdmEntitySet.getName();
		InputStream serializedContent = serializer.entityCollection(serviceMetadata, responseEdmEntitySet.getEntityType(), responseEntityCollection, EntityCollectionSerializerOptions.with().id(id).contextURL(isODataMetadataNone(requestedContentType) ? null : getContextUrl(responseEdmEntitySet, false, expand, select, null)).count(uriInfo.getCountOption()).expand(expand).select(select).build()).getContent();

		// Finally we set the response data, headers and status code
		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
	}

	@Override
	public void readEntity(final ODataRequest request, ODataResponse response, final UriInfo uriInfo, final ContentType requestedContentType) throws ODataApplicationException, SerializerException {
		EdmEntitySet responseEdmEntitySet = null;
		EdmEntityType responseEdmEntityType = null;
		Entity responseEntity = null;

		List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		int segmentCount = resourceParts.size();
		final ExpandOption expand = uriInfo.getExpandOption();
		final SelectOption select = uriInfo.getSelectOption();

		UriResource uriResource = resourceParts.get(0);

		if (!(uriResource instanceof UriResourceEntitySet)) {
			throw new ODataApplicationException("Only EntitySet is supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}

		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
		EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

		if (segmentCount == 1) {
			responseEdmEntityType = startEdmEntitySet.getEntityType();
			responseEdmEntitySet = startEdmEntitySet;
			final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.asUriInfoResource().getUriResourceParts().get(0);
			responseEntity = readEntityInternal(resourceEntitySet.getKeyPredicates(), responseEdmEntitySet);

			if (expand != null) {
				handleExpandOption(responseEntity, expand, responseEdmEntitySet, resourceEntitySet.getKeyPredicates());
			}

		} else if (segmentCount == 2) {
			UriResource navSegment = resourceParts.get(1);
			if (navSegment instanceof UriResourceNavigation) {
				UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) navSegment;
				EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
				responseEdmEntityType = edmNavigationProperty.getType();
				// contextURL displays the last segment
				responseEdmEntitySet = ODataUtil.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);

				// 2nd: fetch the data from backend.
				// e.g. for the URI: Products(1)/Category we have to find the
				// correct Category entity
				List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
				// e.g. for Products(1)/Category we have to find first the
				// Products(1)
				Entity sourceEntity = readEntityInternal(keyPredicates, startEdmEntitySet);

				if (sourceEntity == null) {
					throw new ODataApplicationException("No entity found for this key.", 500, Locale.ENGLISH);
				}

				// now we have to check if the navigation is
				// a) to-one: e.g. Products(1)/Category
				// b) to-many with key: e.g. Categories(3)/Products(5)
				// the key for nav is used in this case:
				// Categories(3)/Products(5)
				List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();

				if (navKeyPredicates.isEmpty()) { // e.g.
													// DemoService.svc/Products(1)/Category
					responseEntity = findOneRelated(sourceEntity, responseEdmEntityType, null);
				} else { // e.g. DemoService.svc/Categories(3)/Products(5)
					responseEntity = findOneRelated(sourceEntity, responseEdmEntityType, navKeyPredicates);
				}
			}
		}

		if (responseEntity == null) {
			// If no entity was found for the given key we throw an exception.
			throw new ODataApplicationException("No entity found for this key.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		} else {
			// If an entity was found we proceed by serializing it and sending
			// it to the client.
			ODataSerializer serializer = odata.createSerializer(requestedContentType);

			InputStream serializedContent = serializer.entity(serviceMetadata, responseEdmEntitySet.getEntityType(), responseEntity, EntitySerializerOptions.with().contextURL(isODataMetadataNone(requestedContentType) ? null : getContextUrl(responseEdmEntitySet, true, expand, select, null)).expand(expand).select(select).build()).getContent();
			response.setContent(serializedContent);
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
		}
	}

	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

		Entity preparedEntity = new Entity();

		preparedEntity.setType(requestEntity.getType());
		preparedEntity.getProperties().addAll(requestEntity.getProperties());

		addBindingLinks(request, edmEntitySet, edmEntityType, requestEntity, preparedEntity);

		addAssociatedLinks(request, edmEntitySet, edmEntityType, requestEntity, preparedEntity);

		/*--Creating the Entity--*/
		Entity createdEntity = updateEntityInternal(edmEntitySet, preparedEntity, keyPredicates);

		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);
		response.setContent(serializedResponse.getContent());
		response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		for (String entityName : entityProcessors.keySet()) {
			au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

			if (entityProcessor.getEntitySetName().equals(edmEntitySet.getName())) {
				entityProcessor.delete(edmEntitySet, keyPredicates);
				break;
			}
		}

		response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format) throws ODataApplicationException, SerializerException {
		readProperty(response, uriInfo, format, false);
	}

	@Override
	public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format) throws ODataApplicationException, SerializerException {
		readProperty(response, uriInfo, format, true);
	}

	@Override
	public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType format) throws ODataApplicationException, SerializerException {
		// First we have to figure out which entity set the requested entity is
		// in
		final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
		final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.asUriInfoResource().getUriResourceParts().get(0);
		// Next we fetch the requested entity from the database
		final Entity entity = readEntityInternal(resourceEntitySet.getKeyPredicates(), edmEntitySet);

		if (entity == null) {
			// If no entity was found for the given key we throw an exception.
			throw new ODataApplicationException("No entity found for this key", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		} else {
			// Next we get the property value from the entity and pass the value
			// to serialization
			UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
			EdmProperty edmProperty = uriProperty.getProperty();
			Property property = entity.getProperty(edmProperty.getName());
			if (property == null) {
				throw new ODataApplicationException("No property found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
			} else {
				if (property.getValue() == null) {
					response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
				} else {
					String value = String.valueOf(property.getValue());
					ByteArrayInputStream serializerContent = new ByteArrayInputStream(value.getBytes(Charset.forName("UTF-8")));
					response.setContent(serializerContent);
					response.setStatusCode(HttpStatusCode.OK.getStatusCode());
					response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());
				}
			}
		}
	}

	private void readProperty(ODataResponse response, UriInfo uriInfo, ContentType contentType, boolean complex) throws ODataApplicationException, SerializerException {
		// To read a property we have to first get the entity out of the entity
		// set
		final EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo.asUriInfoResource());
		final UriResourceEntitySet resourceEntitySet = (UriResourceEntitySet) uriInfo.asUriInfoResource().getUriResourceParts().get(0);
		Entity entity = readEntityInternal(resourceEntitySet.getKeyPredicates(), edmEntitySet);

		if (entity == null) {
			// If no entity was found for the given key we throw an exception.
			throw new ODataApplicationException("No entity found for this key", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		} else {
			// Next we get the property value from the entity and pass the value
			// to serialization
			UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 1);
			EdmProperty edmProperty = uriProperty.getProperty();
			Property property = entity.getProperty(edmProperty.getName());
			if (property == null) {
				throw new ODataApplicationException("No property found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
			} else {
				if (property.getValue() == null) {
					response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
				} else {
					ODataSerializer serializer = odata.createSerializer(contentType);
					final ContextURL contextURL = isODataMetadataNone(contentType) ? null : getContextUrl(edmEntitySet, true, null, null, edmProperty.getName());
					InputStream serializerContent = complex ? serializer.complex(serviceMetadata, (EdmComplexType) edmProperty.getType(), property, ComplexSerializerOptions.with().contextURL(contextURL).build()).getContent() : serializer.primitive(serviceMetadata, (EdmPrimitiveType) edmProperty.getType(), property, PrimitiveSerializerOptions.with().contextURL(contextURL).scale(edmProperty.getScale()).nullable(edmProperty.isNullable()).precision(edmProperty.getPrecision()).maxLength(edmProperty.getMaxLength()).unicode(edmProperty.isUnicode()).build()).getContent();
					response.setContent(serializerContent);
					response.setStatusCode(HttpStatusCode.OK.getStatusCode());
					response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
				}
			}
		}
	}

	private void handleExpandOption(Entity entity, ExpandOption expandOption, EdmEntitySet edmEntitySet, final List<UriParameter> keys) throws ODataApplicationException {

		// ExpandItem expandItem = expandOption.getExpandItems().get(0);
		for (ExpandItem expandItem : expandOption.getExpandItems()) {
			handleExpandItem(entity, edmEntitySet, keys, expandItem);
		}
	}

	private void handleExpandItem(Entity entity, EdmEntitySet edmEntitySet, final List<UriParameter> keys, ExpandItem expandItem) throws ODataApplicationException {
		EdmNavigationProperty edmNavigationProperty = null;
		if (expandItem.isStar()) {
			List<EdmNavigationPropertyBinding> bindings = edmEntitySet.getNavigationPropertyBindings();
			// we know that there are navigation bindings
			// however normally in this case a check if navigation bindings
			// exists is done
			if (!bindings.isEmpty()) {
				// can in our case only be 'Category' or 'Products', so we can
				// take the first
				EdmNavigationPropertyBinding binding = bindings.get(0);
				EdmElement property = edmEntitySet.getEntityType().getProperty(binding.getPath());
				// we don't need to handle error cases, as it is done in the
				// Olingo library
				if (property instanceof EdmNavigationProperty) {
					edmNavigationProperty = (EdmNavigationProperty) property;
				}
			}
		} else {
			// can be 'Category' or 'Products', no path supported
			UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
			// we don't need to handle error cases, as it is done in the Olingo
			// library
			if (uriResource instanceof UriResourceNavigation) {
				edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
			}
		}

		// can be 'Category' or 'Products', no path supported
		// we don't need to handle error cases, as it is done in the Olingo
		// library
		if (edmNavigationProperty != null) {
			EdmEntityType expandEdmEntityType = edmNavigationProperty.getType();
			String navPropName = edmNavigationProperty.getName();

			// build the inline data
			Link link = new Link();
			link.setTitle(navPropName);
			link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
			link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);

			if (edmNavigationProperty.isCollection()) { // in case of
														// Categories(1)/$expand=Products
				// fetch the data for the $expand (to-many navigation) from
				// backend
				// here we get the data for the expand

				EntityCollection expandEntityCollection = findAllBy(expandEdmEntityType, edmEntitySet.getEntityType(), keys);
				link.setInlineEntitySet(expandEntityCollection);
				// link.setHref(expandEntityCollection.getId().toASCIIString());
			} else { // in case of Products(1)?$expand=Category
				// fetch the data for the $expand (to-one navigation) from
				// backend
				// here we get the data for the expand
				Entity expandEntity = findOneRelated(entity, expandEdmEntityType, keys);
				link.setInlineEntity(expandEntity);
				link.setHref(expandEntity.getId().toASCIIString());
			}

			// set the link - containing the expanded data - to the current
			// entity
			entity.getNavigationLinks().add(link);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Entity readEntityInternal(final List<UriParameter> keys, final EdmEntitySet edmEntitySet) throws ODataApplicationException {
		Entity result = null;

		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		for (String entityName : entityProcessors.keySet()) {
			au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

			if (entityProcessor.getEntitySetName().equals(edmEntitySet.getName())) {
				try {
					result = entityProcessor.findOne(edmEntitySet, keys);
				} catch (EntityProcessorException e) {
					log.error("Exception reading entity.", e);
					throw new ODataApplicationException(e.getMessage(), HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
				}
				break;
			}
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Entity findOneRelated(final Entity sourceEntity, final EdmEntityType targetEntityType, final List<UriParameter> keys) throws ODataApplicationException {
		Entity result = null;
		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		for (String entityName : entityProcessors.keySet()) {
			au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

			if (entityProcessor.getEntityTypeName().equals(targetEntityType.getName())) {
				result = entityProcessor.findOneRelated(sourceEntity, keys);
				break;
			}
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EntityCollection findAllBy(EdmEntityType responseEdmEntityType, EdmEntityType startEdmEntityType, List<UriParameter> keyPredicates) throws ODataApplicationException {
		EntityCollection responseEntityCollection = null;
		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		for (String entityName : entityProcessors.keySet()) {
			au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

			if (entityProcessor.getEntityTypeName().equals(responseEdmEntityType.getName())) {
				responseEntityCollection = entityProcessor.findAllBy(startEdmEntityType, keyPredicates);
				break;
			}
		}
		return responseEntityCollection;
	}

	@SuppressWarnings("rawtypes")
	private Entity createEntityInternal(EdmEntitySet edmEntitySet, Entity requestEntity) throws ODataApplicationException {
		Entity result = null;

		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		for (String entityName : entityProcessors.keySet()) {
			au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

			if (entityProcessor.getEntitySetName().equals(edmEntitySet.getName())) {
				result = entityProcessor.create(edmEntitySet, requestEntity);
				break;
			}
		}

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Entity updateEntityInternal(EdmEntitySet edmEntitySet, Entity requestEntity, List<UriParameter> keyPredicates) throws ODataApplicationException {
		Entity result = null;

		Map<String, au.web.odata.entityProcessor.processor.EntityProcessor> entityProcessors = ctx.getBeansOfType(au.web.odata.entityProcessor.processor.EntityProcessor.class);

		for (String entityName : entityProcessors.keySet()) {
			au.web.odata.entityProcessor.processor.EntityProcessor entityProcessor = entityProcessors.get(entityName);

			if (entityProcessor.getEntitySetName().equals(edmEntitySet.getName())) {
				result = entityProcessor.update(edmEntitySet, keyPredicates, requestEntity);
				break;
			}
		}

		return result;
	}

	private EdmEntitySet getEdmEntitySet(final UriInfoResource uriInfo) throws ODataApplicationException {
		final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		/*
		 * To get the entity set we have to interpret all URI segments
		 */
		if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
			throw new ODataApplicationException("Invalid resource type for first segment.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}

		/*
		 * Here we should interpret the whole URI but in this example we do not
		 * support navigation so we throw an exception
		 */

		final UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
		return uriResource.getEntitySet();
	}

	private ContextURL getContextUrl(final EdmEntitySet entitySet, final boolean isSingleEntity, final ExpandOption expand, final SelectOption select, final String navOrPropertyPath) throws SerializerException {

		return ContextURL.with().entitySet(entitySet).selectList(odata.createUriHelper().buildContextURLSelectList(entitySet.getEntityType(), expand, select)).suffix(isSingleEntity ? Suffix.ENTITY : null).navOrPropertyPath(navOrPropertyPath).build();
	}

	@Override
	public void updatePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
		throw new ODataApplicationException("Primitive property update is not supported yet.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void updatePrimitiveValue(final ODataRequest request, ODataResponse response, final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Primitive property update is not supported yet.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
		throw new ODataApplicationException("Primitive property delete is not supported yet.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deletePrimitiveValue(final ODataRequest request, ODataResponse response, final UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Primitive property update is not supported yet.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void updateComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
		throw new ODataApplicationException("Complex property update is not supported yet.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo) throws ODataApplicationException {
		throw new ODataApplicationException("Complex property delete is not supported yet.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	public static boolean isODataMetadataNone(final ContentType contentType) {
		return contentType.isCompatible(ContentType.APPLICATION_JSON) && ContentType.VALUE_ODATA_METADATA_NONE.equalsIgnoreCase(contentType.getParameter(ContentType.PARAMETER_ODATA_METADATA));
	}

	private Entity readEntityByBindingLink(final String entityId, final EdmEntitySet edmEntitySet, final String rawServiceUri) throws ODataApplicationException {

		UriResourceEntitySet entitySetResource = null;
		try {
			entitySetResource = odata.createUriHelper().parseEntityId(this.serviceMetadata.getEdm(), entityId, rawServiceUri);

			if (!entitySetResource.getEntitySet().getName().equals(edmEntitySet.getName())) {
				throw new ODataApplicationException("Execpted an entity-id for entity set " + edmEntitySet.getName() + " but found id for entity set " + entitySetResource.getEntitySet().getName(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		} catch (DeserializerException e) {
			throw new ODataApplicationException(entityId + " is not a valid entity-Id", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}

		try {
			return createEntity(entitySetResource);
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetResource.getEntitySet().getName(), e);
		}
	}

	private Entity createEntity(UriResourceEntitySet entitySetResource) throws URISyntaxException {
		String entityKey = entitySetResource.getKeyPredicates().get(0).getText();
		Entity to = new Entity();
		to.addProperty(new Property(null, ODataConst.JPA_ENTITY_PKEY, ValueType.PRIMITIVE, new Integer(entityKey)));
		to.setId(new URI(entitySetResource.getEntitySet().getName() + "(" + entityKey + ")"));
		to.setType(entitySetResource.getEntityType().getFullQualifiedName().getFullQualifiedNameAsString());
		return to;
	}

	private void createLink(final EdmNavigationProperty navigationProperty, final Entity srcEntity, final Entity destEntity) {
		setLink(navigationProperty, srcEntity, destEntity);

		final EdmNavigationProperty partnerNavigationProperty = navigationProperty.getPartner();
		if (partnerNavigationProperty != null) {
			setLink(partnerNavigationProperty, destEntity, srcEntity);
		}
	}

	private void setLink(final EdmNavigationProperty navigationProperty, final Entity srcEntity, final Entity targetEntity) {
		if (navigationProperty.isCollection()) {
			setLinks(srcEntity, navigationProperty.getName(), targetEntity);
		} else {
			setLink(srcEntity, navigationProperty.getName(), targetEntity);
		}
	}

	private void setLink(final Entity entity, final String navigationPropertyName, final Entity target) {
		Link link = entity.getNavigationLink(navigationPropertyName);
		if (link == null) {
			link = new Link();
			link.setRel(Constants.NS_NAVIGATION_LINK_REL + navigationPropertyName);
			link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
			link.setTitle(navigationPropertyName);

			if (target.getId() != null) {
				link.setHref(target.getId().toASCIIString());
			}
			entity.getNavigationLinks().add(link);
		}
		link.setInlineEntity(target);
	}

	private void setLinks(final Entity entity, final String navigationPropertyName, final Entity... targets) {
		Link link = entity.getNavigationLink(navigationPropertyName);
		if (link == null) {
			link = new Link();
			link.setRel(Constants.NS_NAVIGATION_LINK_REL + navigationPropertyName);
			link.setType(Constants.ENTITY_SET_NAVIGATION_LINK_TYPE);
			link.setTitle(navigationPropertyName);
			if (entity.getId() != null) {
				link.setHref(entity.getId().toASCIIString() + "/" + navigationPropertyName);
			}
			EntityCollection target = new EntityCollection();
			target.getEntities().addAll(Arrays.asList(targets));
			link.setInlineEntitySet(target);

			entity.getNavigationLinks().add(link);
		} else {
			link.getInlineEntitySet().getEntities().addAll(Arrays.asList(targets));
		}
	}

}
