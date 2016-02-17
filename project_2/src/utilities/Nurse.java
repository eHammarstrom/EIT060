package utilities;

public class Nurse extends User {
	public Nurse(String username, String password, String division, long certNbr, boolean readMode) {
		super(username, password, division, certNbr, readMode);
		this.permLevel = PermissionLevel.Nurse;
	}
}
