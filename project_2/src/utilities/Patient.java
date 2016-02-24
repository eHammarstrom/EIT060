package utilities;

public class Patient extends User {
	public Patient(String username, String division, String certNbr) {
		super(username, division, certNbr);
		this.permLevel = PermissionLevel.Patient;
	}
}
