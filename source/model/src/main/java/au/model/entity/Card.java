package au.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "Card")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
@NamedQuery(name="Card.findByChannelId", query="select c from Card c where c.channel.id = ?1")
public class Card extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	@NotNull(message = "{model.card.name.notnull}")
	@Size(message = "{model.card.name.size}", min = 1, max = 50)
	public String name;

	@Column(name = "publishDate", length = 50, nullable = false)
	@NotNull(message = "{model.card.publishdate.notnull}")
	@Size(message = "{model.card.publishdate.size}", min = 1, max = 50)
	public String publishDate = null;
	
	@OneToMany(mappedBy = "card", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<CardParameter> cardParameters = new ArrayList<CardParameter>();
	
	@OneToOne (fetch = FetchType.EAGER)
	@NotNull(message = "{model.card.template.notnull}")
	@JoinColumn(name = "templateId")
	public Template template;
	
	@OneToOne (fetch = FetchType.EAGER)
	@NotNull(message = "{model.card.channel.notnull}")
	@JoinColumn(name = "channelId")
	public Channel channel;
	
	@ManyToMany
	@JoinTable(name = "cardReceivers", joinColumns = @JoinColumn(name = "cardId") , inverseJoinColumns = @JoinColumn(name = "personId") )
	public List<Person> receivers = new ArrayList<Person>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}

	public List<CardParameter> getCardParameters() {
		return cardParameters;
	}

	public void setCardParameters(List<CardParameter> cardParameters) {
		this.cardParameters = cardParameters;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public List<Person> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<Person> receivers) {
		this.receivers = receivers;
	}

	@Transient
	public CardParameter findOneCardParameter(String id) {
		for(CardParameter cp : this.getCardParameters()) {
			if(cp.getId().equals(id)) {
				return cp;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("Name", this.name).append(super.toString()).toString();
	}
	
}
