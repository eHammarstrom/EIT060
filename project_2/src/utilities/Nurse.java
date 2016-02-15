package utilities;

public class Nurse extends User {
	public Nurse(String username, String password, String division, int certNbr) {
		super(username, password, division, certNbr);
		this.permLevel = PermissionLevel.Nurse;
	}
}
