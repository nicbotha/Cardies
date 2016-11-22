package au.web.odata.entityProcessor.mapper;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

import au.model.entity.BaseEntity;

public interface Mapper<K extends BaseEntity> {

	public K toJPAEntity(Entity odata) throws MappingException;

	public default K toJPAEntity(Entity from, Class<K> to) throws MappingException {

		if (from != null) {
			K _instance;
			try {
				_instance = to.newInstance();
				copyInto(from, _instance);
				return _instance;
			} catch (InstantiationException e) {
				throw new MappingException("Mapping exception for " + to.getName(), e);
			} catch (IllegalAccessException e) {
				throw new MappingException("Mapping exception for " + to.getName(), e);
			}
		}

		return null;
	}

	public Entity toOlingoEntity(K jpa) throws MappingException;

	public void copyInto(Entity odata, K jpa) throws MappingException;

	public default Property createPrimitive(final String name, final Object value) {
		return new Property(null, name, ValueType.PRIMITIVE, value);
	}

	public default URI createId(String entitySetName, Object id) {
		try {
			return new URI(entitySetName + "(" + String.valueOf(id) + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}
}
