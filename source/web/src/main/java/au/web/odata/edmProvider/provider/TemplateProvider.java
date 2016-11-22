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
import au.web.odata.entityProcessor.mapper.TemplateMapper;

@Component
public class TemplateProvider implements EntityProvider {

	@Override
	public String getEntitySetName() {
		return ODataConst.TEMPLATE_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.TEMPLATE_ET_NAME;
	}

	@Override
	public FullQualifiedName getFullyQualifiedEntityTypeName() {
		return new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.TEMPLATE_ET_NAME);
	}

	@Override
	public List<CsdlProperty> getProperties() {
		return Arrays.asList(
				property(ODataConst.JPA_ENTITY_PKEY, EdmPrimitiveTypeKind.Int32), 
				property(TemplateMapper.NAME_P, EdmPrimitiveTypeKind.String), 
				property(TemplateMapper.DESCRIPTION_P, EdmPrimitiveTypeKind.String));
	}
	
	@Override
	public List<CsdlNavigationProperty> getNavigationProperties() {
		return Arrays.asList(
				new CsdlNavigationProperty().setName(ODataConst.TEMPLATEPARAMETER_ES_NAME).setType(ODataConst.TEMPLATEPARAMETER_ET_FQN).setCollection(true).setPartner(ODataConst.TEMPLATE_ET_NAME),
				new CsdlNavigationProperty().setName(ODataConst.FILERESOURCE_ET_NAME).setType(ODataConst.FILERESOURCE_ET_FQN).setNullable(false).setPartner(ODataConst.TEMPLATE_ES_NAME));
	}

	@Override
	public CsdlEntitySet getEntitySet() {
		CsdlEntitySet csdlEntitySet = EntityProvider.super.getEntitySet();
		
		CsdlNavigationPropertyBinding navPropBinding_TemplateParam = new CsdlNavigationPropertyBinding();
		navPropBinding_TemplateParam.setTarget(ODataConst.TEMPLATEPARAMETER_ES_NAME); 
		navPropBinding_TemplateParam.setPath(ODataConst.TEMPLATEPARAMETER_ET_NAME);
		
		CsdlNavigationPropertyBinding navPropBinding_FileResource = new CsdlNavigationPropertyBinding();
		navPropBinding_FileResource.setTarget(ODataConst.FILERESOURCE_ES_NAME); 
		navPropBinding_FileResource.setPath(ODataConst.FILERESOURCE_ET_NAME);
		
		csdlEntitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding_TemplateParam,navPropBinding_FileResource));
		return csdlEntitySet;
	}
}
