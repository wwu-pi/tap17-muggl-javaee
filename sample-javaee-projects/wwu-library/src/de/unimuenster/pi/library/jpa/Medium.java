package de.unimuenster.pi.library.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity representing an abstract medium. Fields with get- and set
 * methods are automatically mapped to a corresponding database column.
 * Annotations are used to define the primary key and relations to other
 * entities.
 * 
 * @author Herbert Kuchen, Henning Heitkoetter
 * @version 1.1
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Medium implements java.io.Serializable {
	private static final long serialVersionUID = -8542384203222995659L;
	/** Primary key */
	@Id
	protected int id;
	@NotNull(message="Title required")
	@Size(min=1, message="Title required")
	protected String title;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "medium")
	protected Collection<Copy> copies = new ArrayList<Copy>();

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Collection<Copy> getCopies() {
		return Collections.unmodifiableCollection(this.copies);
	}

	protected void setCopies(Collection<Copy> copies) {
		this.copies = copies;
	}

	protected void addCopy(Copy copy) {
		this.copies.add(copy);
	}

	protected void removeCopy(Copy copy) {
		this.copies.remove(copy);
	}
}
