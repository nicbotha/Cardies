package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import au.model.entity.Template;

public class TemplateCRUDRepositoyTest extends BaseRepositoryTestCase<TemplateCRUDRepository, Template> {

	@Override
	public Template newEntity() {
		return EntityTestHelper.newTemplate();
	}

	@Override
	protected Template updateTestEntity(Template entity) {
		entity.setName("name1");
		entity.setDescription("description1");
		return entity;
	}

	@Override
	protected void assertIsUpdated(Template entity) {
		assertThat("name1", equalTo(entity.getName()));		
		assertThat("description1", equalTo(entity.getDescription()));			
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		Template entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.template.name.size"), getI18n("model.template.description.size")};
		
		entity.setName(SIZE_CONSTRAINT_VIOLATION);
		entity.setDescription(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	@Test
	public void testConstraints_Null() throws Exception {
		Template entity = newEntity();
		String[] expected = new String[] {getI18n("model.template.name.notnull"),getI18n("model.template.resource.notnull")};
		
		entity.setName(null);
		entity.setResource(null);
		
		checkExceptions(entity, expected);
	}

}
