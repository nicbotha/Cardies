package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import au.model.entity.TemplateParameter;

public class TemplateParameterCRUDRepositoryTest extends BaseRepositoryTestCase<TemplateParameterCRUDRepository, TemplateParameter> {

	@Override
	public TemplateParameter newEntity() {
		TemplateParameter templateParameter = EntityTestHelper.newTemplateParameter(EntityTestHelper.newTemplate());
		return templateParameter;
	}

	@Override
	protected TemplateParameter updateTestEntity(TemplateParameter entity) {
		entity.setDefaultValues("defaultValues1");
		entity.setName("name1");
		entity.setDescription("description1");
		return entity;
	}

	@Override
	protected void assertIsUpdated(TemplateParameter entity) {
		assertThat("name1", equalTo(entity.getName()));		
		assertThat("description1", equalTo(entity.getDescription()));			
		assertThat("defaultValues1", equalTo(entity.getDefaultValues()));		
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		TemplateParameter entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.templateparameter.name.size"), getI18n("model.templateparameter.description.size"), getI18n("model.templateparameter.defaultvalue.size")};
		
		entity.setName(SIZE_CONSTRAINT_VIOLATION);
		entity.setDescription(SIZE_CONSTRAINT_VIOLATION);
		entity.setDefaultValues(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	@Test
	public void testConstraints_Null() throws Exception {
		TemplateParameter entity = newEntity();
		String[] expected = new String[] {getI18n("model.templateparameter.name.notnull"),getI18n("model.templateparameter.type.notnull"),getI18n("model.templateparameter.template.notnull")};
		
		entity.setName(null);
		entity.setDescription(null);
		entity.setType(null);
		entity.setTemplate(null);
		
		checkExceptions(entity, expected);
	}

	@Test
	public void testNQ_findByTemplateId() throws Exception{
		TemplateParameter expected = getRepository().findOne("-1");
		List<TemplateParameter> results = getRepository().findByTemplateId("-10");
		
		assertThat(results, contains(expected));		
	}
}
