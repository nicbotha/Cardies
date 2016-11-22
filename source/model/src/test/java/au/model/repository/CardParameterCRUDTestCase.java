package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import au.model.entity.Card;
import au.model.entity.CardParameter;
import au.model.entity.Template;

public class CardParameterCRUDTestCase extends BaseRepositoryTestCase<CardParameterCRUDRepository, CardParameter> {

	@Override
	public CardParameter newEntity() {
		Card card = EntityTestHelper.newCard();
		Template template = card.getTemplate();
		return EntityTestHelper.newCardParameter(card, EntityTestHelper.newTemplateParameter(template));
	}

	@Override
	protected CardParameter updateTestEntity(CardParameter entity) {
		entity.setValue("value1");
		return entity;
	}

	@Override
	protected void assertIsUpdated(CardParameter entity) {
		assertThat("value1", equalTo(entity.getValue()));			
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		CardParameter entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.cardparameter.value.size")};
		
		entity.setValue(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	
	@Test
	public void testConstraints_Null() throws Exception {
		CardParameter entity = newEntity();
		String[] expected = new String[] {getI18n("model.cardparameter.card.notnull"), getI18n("model.cardparameter.templateparameter.notnull")};
		
		entity.setCard(null);
		entity.setTemplateParameter(null);
		
		checkExceptions(entity, expected);
	}

}
