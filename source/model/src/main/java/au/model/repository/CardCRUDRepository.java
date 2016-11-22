package au.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import au.model.entity.Card;

public interface CardCRUDRepository extends CrudRepository<Card, String> {

	@Transactional
	List<Card> findByChannelId(String channelId);
}
