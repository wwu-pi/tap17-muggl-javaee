package de.wwu.pi.app.entities;

public enum CustomerStatus {

	SILVER(0), GOLD(1), PLATINUM(2);
	
	public final int level;
	
	CustomerStatus(int status) {
		this.level = status;
	}
}
