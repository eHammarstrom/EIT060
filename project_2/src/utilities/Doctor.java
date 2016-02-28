package utilities;

public class Doctor extends User {
	public Doctor(String username, Division division, String certNbr) {
		super(username, division, certNbr);
		this.permLevel = PermissionLevel.Doctor;
	}
}
