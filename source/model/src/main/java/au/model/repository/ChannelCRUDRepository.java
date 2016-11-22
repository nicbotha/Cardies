package au.model.repository;

import org.springframework.data.repository.CrudRepository;

import au.model.entity.Channel;

public interface ChannelCRUDRepository extends CrudRepository<Channel, String> {

}
