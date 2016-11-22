package au.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import au.model.entity.Template;

public interface TemplateCRUDRepository extends CrudRepository<Template, String> {

	@Transactional
	List<Template> findByFileResourceId(String fileResourceId);
}
