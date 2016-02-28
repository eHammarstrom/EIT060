package utilities;

import java.io.Serializable;

public class Division implements Serializable {
	private String name;
	
	public Division(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
