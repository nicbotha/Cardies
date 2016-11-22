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
import au.web.odata.entityProcessor.mapper.CardParameterMapper;

@Component
public class CardParameterProvider implements EntityProvider {

	@Override
	public String getEntitySetName() {
		return ODataConst.CARDPARAMETER_ES_NAME;
	}

	@Override
	public String getEntityTypeName() {
		return ODataConst.CARDPARAMETER_ET_NAME;
	}

	@Override
	public FullQualifiedName getFullyQualifiedEntityTypeName() {
		return new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CARDPARAMETER_ET_NAME);
	}

	@Override
	public List<CsdlProperty> getProperties() {
		return Arrays.asList(
				property(ODataConst.JPA_ENTITY_PKEY, EdmPrimitiveTypeKind.Int32), 
				property(CardParameterMapper.VALUE_P, EdmPrimitiveTypeKind.String));
	}

	@Override
	public List<CsdlNavigationProperty> getNavigationProperties() {
		return Arrays.asList(
				new CsdlNavigationProperty().setName(ODataConst.CARD_ET_NAME).setType(ODataConst.CARD_ET_FQN).setNullable(false).setPartner(ODataConst.CARDPARAMETER_ES_NAME),
				new CsdlNavigationProperty().setName(ODataConst.TEMPLATEPARAMETER_ET_NAME).setType(ODataConst.TEMPLATEPARAMETER_ET_FQN).setNullable(false));
	}
	
	@Override
	public CsdlEntitySet getEntitySet() {
		CsdlEntitySet csdlEntitySet = EntityProvider.super.getEntitySet();
		
		CsdlNavigationPropertyBinding navPropBinding_Card = new CsdlNavigationPropertyBinding();
		navPropBinding_Card.setTarget(ODataConst.CARD_ES_NAME); 
		navPropBinding_Card.setPath(ODataConst.CARD_ET_NAME);
		
		CsdlNavigationPropertyBinding navPropBinding_TemplateParameter = new CsdlNavigationPropertyBinding();
		navPropBinding_TemplateParameter.setTarget(ODataConst.TEMPLATEPARAMETER_ES_NAME); 
		navPropBinding_TemplateParameter.setPath(ODataConst.TEMPLATEPARAMETER_ET_NAME);
		
		csdlEntitySet.setNavigationPropertyBindings(Arrays.asList(navPropBinding_Card, navPropBinding_TemplateParameter));
		return csdlEntitySet;
	}

}
