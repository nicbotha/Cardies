package au.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

@MappedSuperclass
@OptimisticLocking(type = OptimisticLockingType.VERSION_COLUMN)
public abstract class BaseEntity{

	@Id
	@Column(name = "id", length = 10)
	@TableGenerator(name = "IdGenerator", table = "MODEL_ID_GENERATOR", pkColumnName = "GENERATOR_NAME", valueColumnName = "GENERATOR_VALUE", pkColumnValue = "id", initialValue = 100000000, allocationSize = 100)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "IdGenerator")
	private String id;
	
	/**
	 * The {@link Date} the object was created at.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationDate", updatable = false)
	private Date createdAt = null;

	/**
	 * The {@link Date} the object was last modified at.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modifiedDate")
	private Date lastModifiedAt = null;

	/**
	 * ID of the user who created the object.
	 */
	@Column(name = "createdBy", updatable = false, length = 20)
	@Size(max = 20)
	private String createdBy = null;

	/**
	 * ID of the user who was the last to modify the object.
	 */
	@Column(name = "modifiedBy", length = 20)
	@Size(max = 20)
	private String lastModifiedBy = null;

	/**
	 * The version number used for optimistic locking.
	 * 
	 * @see http://en.wikibooks.org/wiki/Java_Persistence/Locking
	 * @see http://eclipse.org/eclipselink/documentation/2.4/jpa/extensions/
	 *      a_optimisticlocking.htm
	 */
	@Column(name = "version")
	@Version
	private Long version = 0L;

	/**
	 * Life-cycle event callback, which automatically sets the last modification
	 * date.
	 */
	@PreUpdate
	protected void updateAuditInformation() {
		lastModifiedAt = new Date();

		// TODO - obtain currently logged-on user
	}

	/**
	 * Life-cycle event callback, which automatically creates a unique ID for
	 * the object and populates its audit information.
	 */
	@PrePersist
	protected void generateAuditInformation() {
		final Date now = new Date();

		createdAt = now;
		lastModifiedAt = now;

		// TODO - obtain currently logged-on user
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getLastModifiedAt() {
		return this.lastModifiedAt;
	}

	public void setLastModifiedAt(Date lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("createdAt", this.createdAt)
				.append("createdBy", this.createdBy).append("lastModifiedAt", this.lastModifiedAt)
				.append("lastModifiedBy", this.lastModifiedBy).append("version", this.version).toString();
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof BaseEntity)) {
			return false;
		}

		BaseEntity other = (BaseEntity) obj;

		if (id == null) {
			return false;
		}

		return id.equals(other.getId());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		if (id != null) {
			return id.hashCode();
		} else {
			return super.hashCode();
		}
	}
}