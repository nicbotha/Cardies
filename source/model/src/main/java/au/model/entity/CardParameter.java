package au.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

@Entity
@Table(name = "CardParameter")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
@NamedQuery(name="CardParameter.findByCardId", query="select cp from CardParameter cp where cp.card.id = ?1")
public class CardParameter extends BaseEntity{

	@Column(name = "value", length = 50, nullable = true)
	@Size(message = "{model.cardparameter.value.size}", min = 0, max = 250)
	public String value;
	
	@ManyToOne
	@NotNull(message = "{model.cardparameter.card.notnull}")
	@JoinColumn(name = "cardId", nullable=false)
	public Card card;
	
	@OneToOne (fetch = FetchType.EAGER)
	@NotNull(message = "{model.cardparameter.templateparameter.notnull}")
	@JoinColumn(name = "templateParameterId")
	public TemplateParameter templateParameter;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public TemplateParameter getTemplateParameter() {
		return templateParameter;
	}

	public void setTemplateParameter(TemplateParameter templateParameter) {
		this.templateParameter = templateParameter;
	}
	
	
	
}
