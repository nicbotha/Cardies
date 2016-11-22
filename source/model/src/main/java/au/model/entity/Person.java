package au.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.TenantDiscriminatorColumn;

@Entity
@Table(name = "Person")
@Multitenant
@TenantDiscriminatorColumn(name = "tenant_id")
@NamedQuery(name="Person.findByCardId", query="select P from Person P inner join P.cards card where card.id = ?1")
public class Person extends BaseEntity{

	@Column(name = "name", length = 50, nullable = false)
	@NotNull(message = "{model.person.name.notnull}")
	@Size(message = "{model.person.name.size}", min = 1, max = 50)
	public String name;
	
	@Column(name = "email", length = 50, nullable = false)
	@NotNull(message = "{model.person.email.notnull}")
	@Size(message = "{model.person.email.size}", min = 1, max = 50)
	public String email;
	
	@ManyToMany(mappedBy="receivers")
	public List<Card> cards = new ArrayList<Card>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
