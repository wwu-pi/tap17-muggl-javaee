package de.unimuenster.pi.library.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.CascadeType.*;

/**
 * Entity representing a user of the system. Fields with get- and set
 * methods are automatically mapped to a corresponding database column.
 * Annotations are used to define the primary key and relations to other
 * entities.
 * 
 * @author Herbert Kuchen, Henning Heitkoetter
 * @version 1.1
 */
@Entity
public class User implements java.io.Serializable {
	private static final long serialVersionUID = -6091824661950209090L;
	/** Primary key */
	@Id
	protected int uid;
	@NotNull(message="Name required")
	@Size(min=1, message="Name required")
	protected String name;
	protected String address;
	// A user can not be deleted if he has pending loans.
	@OneToMany(cascade = {DETACH,MERGE,PERSIST,REFRESH}, mappedBy = "user")
	protected Collection<Loan> loans = new ArrayList<Loan>();

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Collection<Loan> getLoans() {
		return Collections.unmodifiableCollection(this.loans);
	}

	protected void setLoans(Collection<Loan> loans) {
		this.loans = loans;
	}

	protected void addLoan(Loan loan) {
		this.loans.add(loan);
	}

	protected void removeLoan(Loan loan) {
		this.loans.remove(loan);
	}
	
	@Override
	public String toString() {
		return getName() + " (UID=" + getUid() + (getAddress()!=null?", "+getAddress():"") + ")";
	}
}
