package au.web.odata.edmProvider.provider;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

import au.web.odata.ODataConst;

public interface EntityProvider {
	
	public default CsdlEntityType getEntityType() {
		// create PropertyRef for Key element
		CsdlPropertyRef propertyRef = new CsdlPropertyRef();
		propertyRef.setName(ODataConst.JPA_ENTITY_PKEY);

		// configure EntityType
		CsdlEntityType entityType = new CsdlEntityType();
		entityType.setName(getEntityTypeName());
		entityType.setProperties(getProperties());
		entityType.setNavigationProperties(getNavigationProperties());
		entityType.setKey(getKeys());

		return entityType;
	}
	
	public default CsdlEntitySet getEntitySet() {
		return new CsdlEntitySet().setName(getEntitySetName()).setType(getFullyQualifiedEntityTypeName());
	}

	public String getEntitySetName();

	public String getEntityTypeName();

	public FullQualifiedName getFullyQualifiedEntityTypeName();
	
	public List<CsdlProperty> getProperties();
	
	public List<CsdlNavigationProperty> getNavigationProperties();
	
	public default List<CsdlPropertyRef> getKeys(){
		CsdlPropertyRef propertyRef = new CsdlPropertyRef();
		propertyRef.setName(ODataConst.JPA_ENTITY_PKEY);
		return Arrays.asList(propertyRef);
	}
	
	public default CsdlProperty property(String key, EdmPrimitiveTypeKind value) {
		return  new CsdlProperty().setName(key).setType(value.getFullQualifiedName());
	}
}
