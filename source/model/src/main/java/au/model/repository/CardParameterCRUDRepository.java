package au.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import au.model.entity.CardParameter;

public interface CardParameterCRUDRepository extends CrudRepository<CardParameter, String> {


	@Transactional
	List<CardParameter> findByCardId(String cardId);
	
	
}
