package au.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

@Entity
@Table(name = "TemplateParameter")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
@NamedQuery(name="TemplateParameter.findByTemplateId", query="select tp from TemplateParameter tp where tp.template.id = ?1")
public class TemplateParameter extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	@NotNull(message = "{model.templateparameter.name.notnull}")
	@Size(message = "{model.templateparameter.name.size}", min = 1, max = 50)
	public String name;
	
	@Column(name="description",length = 250, nullable = true)
	@Size(message = "{model.templateparameter.description.size}", min = 0, max = 250)
	public String description;	
	
	@Column(name="defaultValues",length = 250, nullable = true)
	@Size(message = "{model.templateparameter.defaultvalue.size}", min = 0, max = 250)
	public String defaultValues;
	
	@Enumerated(EnumType.STRING)
	@NotNull(message = "{model.templateparameter.type.notnull}")
	public ParameterType type;
	
	@ManyToOne
	@NotNull(message = "{model.templateparameter.template.notnull}")
	@JoinColumn(name = "templateId", nullable=false)
	public Template template;

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

	public String getDefaultValues() {
		return defaultValues;
	}

	public void setDefaultValues(String defaultValues) {
		this.defaultValues = defaultValues;
	}

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}	
}
