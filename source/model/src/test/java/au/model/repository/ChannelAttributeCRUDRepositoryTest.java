package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import au.model.entity.ChannelAttribute;

public class ChannelAttributeCRUDRepositoryTest extends BaseRepositoryTestCase<ChannelAttributeCRUDRepository, ChannelAttribute>{

	@Override
	public ChannelAttribute newEntity() {
		return EntityTestHelper.newChannelAttribute(EntityTestHelper.newChannel());
	}

	@Override
	protected ChannelAttribute updateTestEntity(ChannelAttribute entity) {
		entity.setIdentifier("identifier1");
		entity.setValue("value1");
		return entity;
	}

	@Override
	protected void assertIsUpdated(ChannelAttribute entity) {
		assertThat("identifier1", equalTo(entity.getIdentifier()));	
		assertThat("value1", equalTo(entity.getValue()));	
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		ChannelAttribute entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.channelattribute.identifier.size"),getI18n("model.channelattribute.value.size")};
		
		entity.setIdentifier(SIZE_CONSTRAINT_VIOLATION);
		entity.setValue(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	
	@Test
	public void testConstraints_Null() throws Exception {
		ChannelAttribute entity = newEntity();
		String[] expected = new String[] {getI18n("model.channelattribute.identifier.notnull"), getI18n("model.channelattribute.value.notnull"),getI18n("model.channelattribute.type.notnull"), getI18n("model.channelattribute.channel.notnull")};
		
		entity.setIdentifier(null);
		entity.setValue(null);
		entity.setType(null);
		entity.setChannel(null);
		
		checkExceptions(entity, expected);
	}

}
