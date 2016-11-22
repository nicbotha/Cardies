package au.web.odata.edmProvider.provider;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.springframework.stereotype.Component;

import au.web.odata.ODataConst;
import au.web.odata.entityProcessor.mapper.TemplateParameterMapper;

@Component
public class TemplateParameterProvider implements EntityProvider {

	@Override
	public String getEntitySetName() {
		return ODataConst.TEMPLATEPARAMETER_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.TEMPLATEPARAMETER_ET_NAME;
	}

	@Override
	public FullQualifiedName getFullyQualifiedEntityTypeName() {
		return new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.TEMPLATEPARAMETER_ET_NAME);
	}

	@Override
	public List<CsdlProperty> getProperties() {
		return Arrays.asList(
				property(ODataConst.JPA_ENTITY_PKEY, EdmPrimitiveTypeKind.Int32), 
				property(TemplateParameterMapper.NAME_P, EdmPrimitiveTypeKind.String), 
				property(TemplateParameterMapper.DESCRIPTION_P, EdmPrimitiveTypeKind.String), 
				property(TemplateParameterMapper.DEFAULTVALUES_P, EdmPrimitiveTypeKind.String), 
				property(TemplateParameterMapper.TYPE_P, EdmPrimitiveTypeKind.String));
	}
	
	@Override
	public List<CsdlNavigationProperty> getNavigationProperties() {
		return Arrays.asList(new CsdlNavigationProperty().setName(ODataConst.TEMPLATE_ET_NAME).setType(ODataConst.TEMPLATE_ET_FQN).setNullable(false).setPartner(ODataConst.TEMPLATEPARAMETER_ES_NAME));
	}
	
	@Override
	public CsdlEntitySet getEntitySet() {
		CsdlEntitySet csdlEntitySet = EntityProvider.super.getEntitySet();
		
		CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
		navPropBinding.setTarget(ODataConst.TEMPLATE_ES_NAME); 
		navPropBinding.setPath(ODataConst.TEMPLATE_ET_NAME);
		
		csdlEntitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding));
		return csdlEntitySet;
	}

}
