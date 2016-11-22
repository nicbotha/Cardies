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
import au.web.odata.entityProcessor.mapper.ChannelMapper;

@Component
public class ChannelProvider implements EntityProvider {

	@Override
	public String getEntitySetName() {
		return ODataConst.CHANNEL_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CHANNEL_ET_NAME;
	}

	@Override
	public FullQualifiedName getFullyQualifiedEntityTypeName() {
		return new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CHANNEL_ET_NAME);
	}

	@Override
	public List<CsdlProperty> getProperties() {
		return Arrays.asList(
				property(ODataConst.JPA_ENTITY_PKEY, EdmPrimitiveTypeKind.Int32), 
				property(ChannelMapper.NAME_P, EdmPrimitiveTypeKind.String),
				property(ChannelMapper.TYPE_P, EdmPrimitiveTypeKind.String));
	}
	
	@Override
	public List<CsdlNavigationProperty> getNavigationProperties() {
		return Arrays.asList(
				new CsdlNavigationProperty().setName(ODataConst.CHANNELATTRIBUTE_ES_NAME).setType(ODataConst.CHANNELATTRIBUTE_ET_FQN).setCollection(true).setPartner(ODataConst.CHANNEL_ET_NAME));
	}

	@Override
	public CsdlEntitySet getEntitySet() {
		CsdlEntitySet csdlEntitySet = EntityProvider.super.getEntitySet();
		
		CsdlNavigationPropertyBinding navPropBinding_TemplateParam = new CsdlNavigationPropertyBinding();
		navPropBinding_TemplateParam.setTarget(ODataConst.CHANNELATTRIBUTE_ES_NAME); 
		navPropBinding_TemplateParam.setPath(ODataConst.CHANNELATTRIBUTE_ET_NAME);
		
		csdlEntitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding_TemplateParam));
		return csdlEntitySet;
	}
}
