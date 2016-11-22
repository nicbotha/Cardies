package au.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

@Entity
@Table(name = "Channel")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
public class Channel extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	@NotNull(message = "{model.channel.name.notnull}")
	@Size(message = "{model.channel.name.size}", min = 1, max = 50)
	public String name;
	
	@OneToMany(mappedBy = "channel", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<ChannelAttribute> attributes = new ArrayList<ChannelAttribute>();
	
	@Column(name="type", nullable = false)
	@NotNull(message = "{model.channel.type.notnull}")
	@Enumerated(EnumType.STRING)
	public ChannelType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ChannelAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ChannelAttribute> attributes) {
		this.attributes = attributes;
	}

	public ChannelType getType() {
		return type;
	}

	public void setType(ChannelType type) {
		this.type = type;
	}
	
	@Transient
	public ChannelAttribute findOneChannelAttribute(String id) {
		for(ChannelAttribute ca : this.getAttributes()) {
			if(ca.getId().equals(id)) {
				return ca;
			}
		}
		return null;
	}
	
	
}
