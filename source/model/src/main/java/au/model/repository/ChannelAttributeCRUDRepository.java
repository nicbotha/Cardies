package au.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import au.model.entity.ChannelAttribute;

public interface ChannelAttributeCRUDRepository extends CrudRepository<ChannelAttribute, String> {
	
	@Transactional
	List<ChannelAttribute> findByChannelId(String channelId);
}
