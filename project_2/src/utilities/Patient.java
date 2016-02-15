package utilities;

public class Patient extends User {
	public Patient(String username, String password, int certNbr) {
		super(username, password, certNbr);
		this.permLevel = PermissionLevel.Patient;
	}
}
