package au.model.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

@Entity
@Table(name = "FileResource")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
public class FileResource extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	@NotNull(message = "{model.fileresource.name.notnull}")
	@Size(message = "{model.fileresource.name.size}", min = 1, max = 50)
	private String name;
	
	@Column(name="description",length = 250, nullable = true)
	@Size(message = "{model.fileresource.description.size}", min = 0, max = 250)
	private String description;
	
	@Column(name="docStoreId", length = 50, nullable = false)
	@NotNull(message = "{model.fileresource.docStoreId.notnull}")
	@Size(message = "{model.fileresource.docStoreId.size}", min = 1, max = 50)
	private String docStoreId;
	
	@Column(name="tags", length = 100, nullable = true)
	@Size(message = "{model.fileresource.tags.size}", min = 0, max = 100)
	private String tags;
	
	@Column(name="type", nullable = false)
	@NotNull(message = "{model.fileresource.type.notnull}")
	@Enumerated(EnumType.STRING)
	private FileResourceType type;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDocStoreId() {
		return docStoreId;
	}
	public void setDocStoreId(String docStoreId) {
		this.docStoreId = docStoreId;
	}

	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public FileResourceType getType() {
		return type;
	}
	public void setType(FileResourceType type) {
		this.type = type;
	}
	
	
}

