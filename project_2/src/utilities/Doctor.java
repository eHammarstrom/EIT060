package utilities;

public class Doctor extends User {
	public Doctor(String username, String division, String certNbr) {
		super(username, division, certNbr);
		this.permLevel = PermissionLevel.Doctor;
	}
}
