package de.unimuenster.pi.library.jpa;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity representing a copy of a medium which can be borrowed. Fields
 * with get- and set methods are automatically mapped to a corresponding
 * database column. Annotations are used to define the primary key and relations
 * to other entities.
 * 
 * @author Herbert Kuchen, Henning Heitkoetter
 * @version 1.1
 */
@Entity
public class Copy implements java.io.Serializable {
	private static final long serialVersionUID = 2133698994451972839L;
	/** primary key */
	@Id
	protected int inventoryNo;
	@ManyToOne
	protected Medium medium;
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "copy")
	protected Loan loan;

	public int getInventoryNo() {
		return this.inventoryNo;
	}

	public void setInventoryNo(int inventoryNo) {
		this.inventoryNo = inventoryNo;
	}

	public Medium getMedium() {
		return this.medium;
	}

	public void setMedium(Medium medium) {
		if(this.medium != null)
			this.medium.removeCopy(this);
		this.medium = medium;
		if(medium != null)
			medium.addCopy(this);
	}

	public Loan getLoan() {
		return this.loan;
	}

	public void setLoan(Loan loan) {
		this.loan = loan;
	}
}
