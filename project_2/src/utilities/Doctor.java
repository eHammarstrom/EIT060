package utilities;

public class Doctor extends User {
	public Doctor(String username, String password, String division, int certNbr) {
		super(username, password, division, certNbr);
		this.permLevel = PermissionLevel.Doctor;
	}
}
