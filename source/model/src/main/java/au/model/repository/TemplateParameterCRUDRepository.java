package au.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import au.model.entity.TemplateParameter;

public interface TemplateParameterCRUDRepository extends CrudRepository<TemplateParameter, String> {

	@Transactional
	List<TemplateParameter> findByTemplateId(String templateId);
}
