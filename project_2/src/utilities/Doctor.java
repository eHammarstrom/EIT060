package utilities;

public class Doctor extends User {
	public Doctor(String username, String password, String division, long certNbr) {
		super(username, password, division, certNbr);
		this.permLevel = PermissionLevel.Doctor;
	}
}
