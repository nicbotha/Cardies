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
import au.web.odata.entityProcessor.mapper.CardMapper;

@Component
public class CardProvider implements EntityProvider {

	@Override
	public String getEntitySetName() {
		return ODataConst.CARD_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CARD_ET_NAME;
	}

	@Override
	public FullQualifiedName getFullyQualifiedEntityTypeName() {
		return new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CARD_ET_NAME);
	}

	@Override
	public List<CsdlProperty> getProperties() {
		return Arrays.asList(
				property(ODataConst.JPA_ENTITY_PKEY, EdmPrimitiveTypeKind.Int32), 
				property(CardMapper.NAME_P, EdmPrimitiveTypeKind.String), 
				property(CardMapper.PUBLISHDATE_P, EdmPrimitiveTypeKind.String));
	}

	@Override
	public List<CsdlNavigationProperty> getNavigationProperties() {
		return Arrays.asList(
				new CsdlNavigationProperty().setName(ODataConst.CARDPARAMETER_ES_NAME).setType(ODataConst.CARDPARAMETER_ET_FQN).setCollection(true).setPartner(ODataConst.CARD_ET_NAME),
				new CsdlNavigationProperty().setName(ODataConst.TEMPLATE_ET_NAME).setType(ODataConst.TEMPLATE_ET_FQN).setNullable(false),
				new CsdlNavigationProperty().setName(ODataConst.CHANNEL_ET_NAME).setType(ODataConst.CHANNEL_ET_FQN).setNullable(false),
				new CsdlNavigationProperty().setName(ODataConst.PERSON_ES_NAME).setType(ODataConst.PERSON_ET_FQN).setCollection(true));
	}
	
	@Override
	public CsdlEntitySet getEntitySet() {
		CsdlEntitySet csdlEntitySet = EntityProvider.super.getEntitySet();
		
		CsdlNavigationPropertyBinding navPropBinding_CardParameters = new CsdlNavigationPropertyBinding();
		navPropBinding_CardParameters.setTarget(ODataConst.CARDPARAMETER_ES_NAME); 
		navPropBinding_CardParameters.setPath(ODataConst.CARDPARAMETER_ET_NAME);
		
		CsdlNavigationPropertyBinding navPropBinding_Template = new CsdlNavigationPropertyBinding();
		navPropBinding_Template.setTarget(ODataConst.TEMPLATE_ES_NAME); 
		navPropBinding_Template.setPath(ODataConst.TEMPLATE_ET_NAME);
		
		CsdlNavigationPropertyBinding navPropBinding_Channel = new CsdlNavigationPropertyBinding();
		navPropBinding_Channel.setTarget(ODataConst.CHANNEL_ES_NAME); 
		navPropBinding_Channel.setPath(ODataConst.CHANNEL_ET_NAME);
		
		CsdlNavigationPropertyBinding navPropBinding_Person = new CsdlNavigationPropertyBinding();
		navPropBinding_Person.setTarget(ODataConst.PERSON_ES_NAME); 
		navPropBinding_Person.setPath(ODataConst.PERSON_ET_NAME);
		
		csdlEntitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding_CardParameters, navPropBinding_Template,navPropBinding_Channel,navPropBinding_Person));
		return csdlEntitySet;
	}

}
