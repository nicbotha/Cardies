package au.web.odata.entityProcessor.mapper;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.model.entity.FileResource;
import au.model.entity.FileResourceType;
import au.model.repository.FileResourceCRUDRepository;
import au.web.odata.ODataConst;

@Component
public class FileResourceMapper implements Mapper<FileResource> {
	
	public static final String NAME_P = "Name";
	public static final String DESCRIPTION_P = "Description";
	public static final String DOCSTOREID_P = "DocStoreId";
	public static final String TAGS_P = "Tags";
	public static final String TYPE_P = "Type";
	
	@Autowired
	FileResourceCRUDRepository fileResourceCRUDRepository;	

	public FileResource toJPAEntity(Entity from) throws IllegalArgumentException{
		
		if (from != null) {
			FileResource to = new FileResource();
			copyInto(from, to);
			return to;
		}
		
		return null;
	}

	public Entity toOlingoEntity(FileResource from) throws IllegalArgumentException{
		
		if (from != null) {
			Entity to = new Entity();
			to.addProperty(createPrimitive(ODataConst.JPA_ENTITY_PKEY, new Integer(from.getId())));
			to.addProperty(createPrimitive(FileResourceMapper.NAME_P, from.getName()));
			to.addProperty(createPrimitive(FileResourceMapper.DESCRIPTION_P, from.getDescription()));
			to.addProperty(createPrimitive(FileResourceMapper.DOCSTOREID_P, from.getDocStoreId()));
			to.addProperty(createPrimitive(FileResourceMapper.TAGS_P, from.getTags()));
			to.addProperty(createPrimitive(FileResourceMapper.TYPE_P, from.getType()));
			to.setId(createId(ODataConst.FILERESOURCE_ES_NAME, from.getId()));
			to.setType(ODataConst.FILERESOURCE_ET_FQN.getFullQualifiedNameAsString());
			return to;
		}
		
		return null;
	}

	public void copyInto(Entity from, FileResource to) throws IllegalArgumentException{
		
		if(from == null || to == null) {
			return;
		}
		
		Property nameProperty = from.getProperty(FileResourceMapper.NAME_P);
		Property descriptionProperty = from.getProperty(FileResourceMapper.DESCRIPTION_P);
		Property docStoreIdProperty = from.getProperty(FileResourceMapper.DOCSTOREID_P);
		Property tagsProperty = from.getProperty(FileResourceMapper.TAGS_P);
		Property typeProperty = from.getProperty(FileResourceMapper.TYPE_P);

		
		//NAME
		if (nameProperty != null) {
			to.setName((String) nameProperty.getValue());
		}
		
		//DESCRIPTION
		if (descriptionProperty != null) {
			to.setDescription((String) descriptionProperty.getValue());
		}
		//DOCSTOREID
		if (docStoreIdProperty != null) {
			to.setDocStoreId((String) docStoreIdProperty.getValue());
		}
		//TAGS
		if (tagsProperty != null) {
			to.setTags((String) tagsProperty.getValue());
		}
		//TYPE
		if (typeProperty != null) {
			to.setType(FileResourceType.valueOf((String)typeProperty.getValue()));
		}
		
	}
	
	public FileResource findOne(String key) {
		return this.fileResourceCRUDRepository.findOne(key);
	}

}
