package utilities;

public class Nurse extends User {
	public Nurse(String username, String password, int certNbr) {
		super(username, password, certNbr);
		this.permLevel = PermissionLevel.Nurse;
	}
}
