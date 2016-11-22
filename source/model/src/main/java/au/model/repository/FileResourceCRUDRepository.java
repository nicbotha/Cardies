package au.model.repository;

import org.springframework.data.repository.CrudRepository;

import au.model.entity.FileResource;

public interface FileResourceCRUDRepository extends CrudRepository<FileResource, String> {

}
