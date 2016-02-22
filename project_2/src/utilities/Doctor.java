package utilities;

public class Doctor extends User {
	public Doctor(String username, String password, String division, String certNbr, boolean readMode) {
		super(username, password, division, certNbr, readMode);
		this.permLevel = PermissionLevel.Doctor;
	}
}
