package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import au.model.entity.Card;

public class CardCRUDRepositoryTest extends BaseRepositoryTestCase<CardCRUDRepository, Card> {

	@Override
	public Card newEntity() {
		return EntityTestHelper.newCard();
	}

	@Override
	protected Card updateTestEntity(Card entity) {
		entity.setName("name1");
		entity.setPublishDate("publishDate1");
		return entity;
	}

	@Override
	protected void assertIsUpdated(Card entity) {
		assertThat("name1", equalTo(entity.getName()));		
		assertThat("publishDate1", equalTo(entity.getPublishDate()));		
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		Card entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.card.name.size"), getI18n("model.card.publishdate.size")};
		
		entity.setName(SIZE_CONSTRAINT_VIOLATION);
		entity.setPublishDate(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	@Test
	public void testConstraints_Null() throws Exception {
		Card entity = newEntity();
		String[] expected = new String[] {getI18n("model.card.name.notnull"),getI18n("model.card.publishdate.notnull"),getI18n("model.card.template.notnull"),getI18n("model.card.channel.notnull")};
		
		entity.setName(null);
		entity.setPublishDate(null);
		entity.setTemplate(null);
		entity.setChannel(null);
		
		checkExceptions(entity, expected);
	}
}
