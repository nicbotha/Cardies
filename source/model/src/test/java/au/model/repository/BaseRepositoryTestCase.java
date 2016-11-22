package au.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import au.model.entity.BaseEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-context.xml", "classpath:/datasource-context.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup(value = { "classpath:/data/sample-data.xml" }, type = DatabaseOperation.CLEAN_INSERT)
@Profile("test")
public abstract class BaseRepositoryTestCase<T extends CrudRepository<L, String>, L extends BaseEntity> extends AbstractTransactionalJUnit4SpringContextTests {

	protected static final ResourceBundle bundle = ResourceBundle.getBundle("ValidationMessages");
	protected final Logger log = LoggerFactory.getLogger(BaseRepositoryTestCase.class);

	@Autowired
	protected T repository;

	public BaseRepositoryTestCase() {
	}

	@Test
	public void testGetEntity() throws Exception {
		L entity = repository.findOne("-1");

		assertNotNull(getI18n("model.entity.unknown"),entity);
		assertThat(entity.getId(), equalTo("-1"));
	}

	/**
	 * For every Entity id -2 will represent a different Tenant.
	 */
	@Test
	public void testMultiTenancyGetEntity() throws Exception {
		L entity = repository.findOne("-2");

		assertNull(getI18n("model.entity.unknown"),entity);
	}

	@Test
	public void testAllEntities() throws Exception {
		List<L> entities = (List<L>) repository.findAll();
		
		assertNotNull(getI18n("model.entity.unknown"), entities);
	}

	@Test
	public void testSaveEntity() throws Exception {
		try {
		log.debug(">> testSaveEntity()");
		L e = newEntity();

		assertNotNull(getI18n("model.entity.unknown"), e);
		e = repository.save(e);
		assertNotNull(e.getId());

		log.debug("<< testSaveEntity()");
		}catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			for(@SuppressWarnings("rawtypes") ConstraintViolation v : violations) {
				log.error("Constraint violation during save={}", v.getMessage());
			}
			throw e;
		}
	}

	@Test
	public void testUpdateEntity() throws Exception {
		try {
		L e = newEntity();
		assertNotNull(getI18n("model.entity.unknown"),e);

		e = repository.save(e);

		assertNotNull(getI18n("model.entity.unknown"), e);
		L entity = repository.findOne(e.getId());
		assertNotNull(getI18n("model.entity.unknown"), entity);
		assertNotNull(getI18n("model.entity.unknown"), entity.getId());
		assertThat(e.getId(), equalTo(entity.getId()));

		updateTestEntity(entity);
		repository.save(entity);
		L updatedEntity = repository.findOne(e.getId());
		assertNotNull(getI18n("model.entity.unknown"), updatedEntity);
		assertNotNull(getI18n("model.entity.unknown"), updatedEntity.getId());
		assertThat(e.getId(), equalTo(updatedEntity.getId()));
		assertIsUpdated(updatedEntity);
		} catch (ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			for(@SuppressWarnings("rawtypes") ConstraintViolation v : violations) {
				log.error("Constraint violation during update={}", v.getMessage());
			}
			throw e;
		}
	}

	@Test
	public void testDelete() throws Exception {
		log.debug(">> testDeleteEntity()");
		
		L entityK = null;
		try {
			L e = newEntity();
			assertNotNull(e);

			L entity = repository.save(e);

			assertNotNull(getI18n("model.entity.unknown"),entity);
			assertNotNull(getI18n("model.entity.unknown"),entity.getId());

			entityK = repository.findOne(entity.getId());
			assertNotNull(getI18n("model.entity.unknown"),entityK);
			assertNotNull(getI18n("model.entity.unknown"),entityK.getId());
			assertThat(entity.getId(), equalTo(entityK.getId()));
			repository.delete(entityK);
			assertFalse("Unexpected Entity in repository after deletion.", repository.exists(entity.getId()));
		} catch (ConstraintViolationException e) {
			log.error("Constraint violation for entity={}",entityK);
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			for(@SuppressWarnings("rawtypes") ConstraintViolation v : violations) {
				log.error("Constraint violation during delete={}", v.getMessage());
			}
			throw e;
		}

		log.debug("<< testDeleteEntity()");
	}
	
	public void checkExceptions(L entity, String[] expected) {
		boolean exception = false;
		try {
			getRepository().save(entity);
		} catch (ConstraintViolationException e) {
			exception = true;
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			List<String> actual = new ArrayList<>();
			for(@SuppressWarnings("rawtypes") ConstraintViolation v : violations) {
				actual.add(v.getMessage());
			}
			assertEquals("Unexpected number of exceptions.", expected.length, actual.size());
			assertThat("Expected exception not present.", actual, hasItems(expected));
			
		}
		assertTrue("Unexpected result status: Bean validation failed.",exception);		
	}

	public T getRepository() {
		return repository;
	}

	protected String getI18n(String key) {
		return bundle.getString(key);
	}
	
	public abstract L newEntity();

	protected abstract L updateTestEntity(L entity);

	protected abstract void assertIsUpdated(L entity);
}
