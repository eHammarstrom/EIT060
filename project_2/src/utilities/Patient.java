package utilities;

public class Patient extends User {
	public Patient(String username, String password, String division, long certNbr) {
		super(username, password, division, certNbr);
		this.permLevel = PermissionLevel.Patient;
	}
}
