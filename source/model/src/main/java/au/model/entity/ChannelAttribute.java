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
@Table(name = "ChannelAttribute")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
@NamedQuery(name="ChannelAttribute.findByChannelId", query="select ca from ChannelAttribute ca where ca.channel.id = ?1")
public class ChannelAttribute extends BaseEntity {

	@Column(name = "identifier", length = 50, nullable = false)
	@NotNull(message = "{model.channelattribute.identifier.notnull}")
	@Size(message = "{model.channelattribute.identifier.size}", min = 1, max = 50)
	public String identifier;
	
	
	@Column(name = "value", length = 250, nullable = false)
	@NotNull(message = "{model.channelattribute.value.notnull}")
	@Size(message = "{model.channelattribute.value.size}", min = 1, max = 250)
	public String value;
	
	@Column(name="type", nullable = false)
	@NotNull(message = "{model.channelattribute.type.notnull}")
	@Enumerated(EnumType.STRING)
	public ChannelAttributeType type;
	
	@ManyToOne
	@NotNull(message = "{model.channelattribute.channel.notnull}")
	@JoinColumn(name = "channelId", nullable=false)
	public Channel channel;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ChannelAttributeType getType() {
		return type;
	}

	public void setType(ChannelAttributeType type) {
		this.type = type;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	
}
