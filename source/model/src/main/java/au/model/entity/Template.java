package au.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

@Entity
@Table(name = "Template")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
@NamedQuery(name="Template.findByFileResourceId", query="select t from Template t where t.resource.id = ?1")
public class Template extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	@NotNull(message = "{model.template.name.notnull}")
	@Size(message = "{model.template.name.size}", min = 1, max = 50)
	public String name;
	
	@Column(name="description",length = 250, nullable = true)
	@Size(message = "{model.template.description.size}", min = 0, max = 250)
	public String description;
	
	@OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<TemplateParameter> parameters = new ArrayList<>();
	
	@OneToOne (fetch = FetchType.EAGER)
	@NotNull(message = "{model.template.resource.notnull}")
	@JoinColumn(name = "resourcelId")
	public FileResource resource;
	
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
	public List<TemplateParameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<TemplateParameter> parameters) {
		this.parameters = parameters;
	}
	public FileResource getResource() {
		return resource;
	}
	public void setResource(FileResource resource) {
		this.resource = resource;
	}
	
	@Transient
	public TemplateParameter findOneTemplateParameter(String id) {
		for(TemplateParameter tp : this.getParameters()) {
			if(tp.getId().equals(id)) {
				return tp;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append("description", this.description).append("resource", this.resource).append(super.toString()).build();
	}
	
	
}
