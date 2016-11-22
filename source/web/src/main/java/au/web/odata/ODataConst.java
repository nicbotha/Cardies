package au.web.odata;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public class ODataConst {
	public static final String JPA_ENTITY_PKEY = "ID";
	
	// Service Namespace
	public static final String NAMESPACE = "au.model";

	public static final String SOMEENTITY_ES_NAME = "Somemore";
	public static final String SOMEENTITY_ET_NAME = "Some";
	
	//FileResource entity
	public static final String FILERESOURCE_ES_NAME="FileResources";
	public static final String FILERESOURCE_ET_NAME="FileResource";
	public static final FullQualifiedName FILERESOURCE_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.FILERESOURCE_ET_NAME);
	
	//Template parameter
	public static final String TEMPLATEPARAMETER_ES_NAME="TemplateParameters";
	public static final String TEMPLATEPARAMETER_ET_NAME="TemplateParameter";
	public static final FullQualifiedName TEMPLATEPARAMETER_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.TEMPLATEPARAMETER_ET_NAME);
	
	//Template 
	public static final String TEMPLATE_ES_NAME="Templates";
	public static final String TEMPLATE_ET_NAME="Template";
	public static final FullQualifiedName TEMPLATE_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.TEMPLATE_ET_NAME);
		
	//Card
	public static final String CARD_ES_NAME="Cards";
	public static final String CARD_ET_NAME="Card";
	public static final FullQualifiedName CARD_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CARD_ET_NAME);
	
	//Card parameter
	public static final String CARDPARAMETER_ES_NAME="CardParameters";
	public static final String CARDPARAMETER_ET_NAME="CardParameter";
	public static final FullQualifiedName CARDPARAMETER_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CARDPARAMETER_ET_NAME);
	
	//Persion
	public static final String PERSON_ES_NAME="Persons";
	public static final String PERSON_ET_NAME="Person";
	public static final FullQualifiedName PERSON_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.PERSON_ET_NAME);
	
	//Channel
	public static final String CHANNEL_ES_NAME="Channels";
	public static final String CHANNEL_ET_NAME="Channel";
	public static final FullQualifiedName CHANNEL_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CHANNEL_ET_NAME);
	
	//ChannelAttribute
	public static final String CHANNELATTRIBUTE_ES_NAME="ChannelAttributes";
	public static final String CHANNELATTRIBUTE_ET_NAME="ChannelAttribute";
	public static final FullQualifiedName CHANNELATTRIBUTE_ET_FQN = new FullQualifiedName(ODataConst.NAMESPACE, ODataConst.CHANNELATTRIBUTE_ET_NAME);
}
