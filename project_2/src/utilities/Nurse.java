package utilities;

public class Nurse extends User {
	public Nurse(String username, String division, String certNbr) {
		super(username, division, certNbr);
		this.permLevel = PermissionLevel.Nurse;
	}
}
