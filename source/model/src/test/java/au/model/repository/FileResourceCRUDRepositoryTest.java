package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.model.entity.FileResource;
import au.model.entity.FileResourceType;

public class FileResourceCRUDRepositoryTest extends BaseRepositoryTestCase<FileResourceCRUDRepository, FileResource> {
	
	protected final Logger log = LoggerFactory.getLogger(FileResourceCRUDRepositoryTest.class);
	@Override
	public FileResource newEntity() {
		return EntityTestHelper.newFileResource();
	}

	@Override
	protected FileResource updateTestEntity(FileResource entity) {
		entity.setName("name1");
		entity.setDescription("description1");
		entity.setDocStoreId("docStoreId1");
		entity.setTags("tags1");
		entity.setType(FileResourceType.HTML);
		return entity;
	}

	@Override
	protected void assertIsUpdated(FileResource entity) {
		assertThat("name1", equalTo(entity.getName()));		
		assertThat("description1", equalTo(entity.getDescription()));		
		assertThat("docStoreId1", equalTo(entity.getDocStoreId()));	
		assertThat("tags1", equalTo(entity.getTags()));
		assertThat(FileResourceType.HTML, equalTo(entity.getType()));
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		FileResource entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.fileresource.name.size"), getI18n("model.fileresource.description.size"),getI18n("model.fileresource.docStoreId.size"),getI18n("model.fileresource.tags.size")};
		
		entity.setName(SIZE_CONSTRAINT_VIOLATION);
		entity.setDescription(SIZE_CONSTRAINT_VIOLATION);
		entity.setDocStoreId(SIZE_CONSTRAINT_VIOLATION);
		entity.setTags(SIZE_CONSTRAINT_VIOLATION);
		
		checkExceptions(entity, expected);
	}
	
	@Test
	public void testConstraints_Null() throws Exception {
		FileResource entity = newEntity();
		String[] expected = new String[] {getI18n("model.fileresource.name.notnull"), getI18n("model.fileresource.docStoreId.notnull"),getI18n("model.fileresource.type.notnull")};
		
		entity.setName(null);
		entity.setDocStoreId(null);
		entity.setType(null);
		
		checkExceptions(entity, expected);
	}

}
