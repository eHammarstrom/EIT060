package utilities;

public class Patient extends User {
	public Patient(String username, String password, String division, long certNbr, boolean readMode) {
		super(username, password, division, certNbr, readMode);
		this.permLevel = PermissionLevel.Patient;
	}
}
