package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import au.model.entity.Channel;

public class ChannelCRUDRepositoryTest extends BaseRepositoryTestCase<ChannelCRUDRepository, Channel> {

	@Override
	public Channel newEntity() {
		return EntityTestHelper.newChannel();
	}

	@Override
	protected Channel updateTestEntity(Channel entity) {
		entity.setName("name1");
		return entity;
	}

	@Override
	protected void assertIsUpdated(Channel entity) {
		assertThat("name1", equalTo(entity.getName()));	
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		Channel entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.channel.name.size")};
		
		entity.setName(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	
	@Test
	public void testConstraints_Null() throws Exception {
		Channel entity = newEntity();
		String[] expected = new String[] {getI18n("model.channel.name.notnull"), getI18n("model.channel.type.notnull")};
		
		entity.setName(null);
		entity.setType(null);
		
		checkExceptions(entity, expected);
	}

}
