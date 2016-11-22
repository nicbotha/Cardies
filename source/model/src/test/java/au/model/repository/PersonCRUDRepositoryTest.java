package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.model.entity.Person;

public class PersonCRUDRepositoryTest extends BaseRepositoryTestCase<PersonCRUDRepository, Person> {

	protected final Logger log = LoggerFactory.getLogger(PersonCRUDRepositoryTest.class);

	public Person newEntity() {
		return EntityTestHelper.newPerson();
	}

	protected Person updateTestEntity(Person entity) {
		entity.setName("name1");
		entity.setEmail("email1");
		return entity;
	}

	//@Override
	protected void assertIsUpdated(Person entity) {
		assertThat("name1", equalTo(entity.getName()));		
		assertThat("email1", equalTo(entity.getEmail()));
	}
	
	@Test
	public void testConstraints_Size() throws Exception {
		Person entity = newEntity();
		final String SIZE_CONSTRAINT_VIOLATION = StringUtils.repeat("a", 500);
		
		String[] expected = new String[] {getI18n("model.person.name.size"), getI18n("model.person.email.size")};
		
		entity.setName(SIZE_CONSTRAINT_VIOLATION);
		entity.setEmail(SIZE_CONSTRAINT_VIOLATION);
				
		checkExceptions(entity, expected);
	}
	
	@Test
	public void testConstraints_Null() throws Exception {
		Person entity = newEntity();
		String[] expected = new String[] {getI18n("model.person.name.notnull"),getI18n("model.person.email.notnull")};
		
		entity.setName(null);
		entity.setEmail(null);
		
		checkExceptions(entity, expected);
	}
	

	@Test
	public void testNQ_findByCardId() throws Exception{
		List<Person> result_01 = getRepository().findByCardId("-5");		
		assertEquals("Unexpected number of exceptions.", 1, result_01.size());
		assertThat("Card.ID -5 should have just one Person.ID=-5", result_01, contains(getRepository().findOne("-5")));
		
		List<Person> result_02 = getRepository().findByCardId("-6");
		List<Person> expected_02 = Arrays.asList(getRepository().findOne("-5"), getRepository().findOne("-6"));
		
		assertEquals("Unexpected number of exceptions.", 2, result_02.size());
		assertThat("Card.ID -6 should have just two Person.ID=[-5,-6]", result_02, contains(expected_02.toArray()));
	}

}
