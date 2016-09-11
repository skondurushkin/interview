package org.infotex.skondurushkin;

public class Command {
	public static final String ACTION_ADD = "add";
	public static final String ACTION_REMOVE = "remove";

	protected String	action;
	protected Long	value;
	
	public Command() {
		
	}
	
	public Command(String action, Long value) {
		this.action = action;
		this.value = value;
	}
	
	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

}
