package au.web.odata.edmProvider.provider;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.springframework.stereotype.Component;

import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.PersonMapper;

@Component
public class PersonProvider implements EntityProvider {

	@Override
	public String getEntitySetName() {
		return ODataConst.PERSON_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.PERSON_ET_NAME;
	}

	@Override
	public FullQualifiedName getFullyQualifiedEntityTypeName() {
		return new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.PERSON_ET_NAME);
	}

	@Override
	public List<CsdlProperty> getProperties() {
		return Arrays.asList(
				property(ODataConst.JPA_ENTITY_PKEY, EdmPrimitiveTypeKind.Int32), 
				property(PersonMapper.NAME_P, EdmPrimitiveTypeKind.String), 
				property(PersonMapper.EMAIL_P, EdmPrimitiveTypeKind.String));
	}

	@Override
	public List<CsdlNavigationProperty> getNavigationProperties() {
		// TODO Auto-generated method stub
		return null;
	}



}
