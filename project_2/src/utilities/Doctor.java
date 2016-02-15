package utilities;

public class Doctor extends User {
	public Doctor(String username, String password, int certNbr) {
		super(username, password, certNbr);
		this.permLevel = PermissionLevel.Doctor;
	}
}
