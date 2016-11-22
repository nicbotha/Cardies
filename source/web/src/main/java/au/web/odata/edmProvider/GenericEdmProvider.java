package au.web.odata.edmProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.web.odata.edmProvider.provider.EntityProvider;

@Component("GenericEdmProvider")
public class GenericEdmProvider extends CsdlAbstractEdmProvider {

	@Autowired
	private ApplicationContext ctx;

	public static final String NAMESPACE = "au.model";
	public static final String CONTAINER_NAME = "Container";
	public static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		CsdlEntityType result = null;

		Map<String, EntityProvider> entityProviders = ctx.getBeansOfType(EntityProvider.class);

		for (String entityName : entityProviders.keySet()) {
			EntityProvider entityProvider = entityProviders.get(entityName);

			if (entityProvider.getEntityTypeName().equals(entityTypeName.getName())) {
				result = entityProvider.getEntityType();
				break;
			}
		}

		return result;
	}

	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
		CsdlEntitySet result = null;

		Map<String, EntityProvider> entityProviders = ctx.getBeansOfType(EntityProvider.class);

		for (String entityName : entityProviders.keySet()) {
			EntityProvider entityProvider = entityProviders.get(entityName);

			if (entityProvider.getEntitySetName().equals(entitySetName)) {
				result = entityProvider.getEntitySet();
				break;
			}
		}

		return result;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		CsdlEntityContainer container = new CsdlEntityContainer();
		container.setName(CONTAINER_FQN.getName());

		// EntitySets
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		container.setEntitySets(entitySets);

		Map<String, EntityProvider> entityProviders = ctx.getBeansOfType(EntityProvider.class);

		for (String entityName : entityProviders.keySet()) {
			EntityProvider entityProvider = entityProviders.get(entityName);

			entitySets.add(getEntitySet(CONTAINER_FQN, entityProvider.getEntitySetName()));
		}

		return container;
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAMESPACE);
		// EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		Map<String, EntityProvider> entityProviders = ctx.getBeansOfType(EntityProvider.class);

		for (String entityName : entityProviders.keySet()) {
			EntityProvider entityProvider = entityProviders.get(entityName);
			entityTypes.add(entityProvider.getEntityType());

		}
		schema.setEntityTypes(entityTypes);

		// ComplexTypes
		List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
		// complexTypes.add(getComplexType(CT_ADDRESS));
		schema.setComplexTypes(complexTypes);

		// EntityContainer
		schema.setEntityContainer(getEntityContainer());
		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName) throws ODataException {
		if (entityContainerName == null || CONTAINER_FQN.equals(entityContainerName)) {
			return new CsdlEntityContainerInfo().setContainerName(CONTAINER_FQN);
		}
		return null;
	}
}
