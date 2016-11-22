package au.web.odata.entityProcessor;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;

public class EntityProcessorException extends Exception {

	private static final long serialVersionUID = 4198989604739472814L;

	public EntityProcessorException(String msg, EdmPrimitiveTypeException e) {
		super(msg, e);
	}
	
}
