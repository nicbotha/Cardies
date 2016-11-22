package au.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import au.model.entity.Person;

public interface PersonCRUDRepository extends CrudRepository<Person, String> {

	@Transactional
	List<Person> findByCardId(String cardId);
}
