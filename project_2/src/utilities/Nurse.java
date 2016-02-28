package utilities;

public class Nurse extends User {
	public Nurse(String username, Division division, String certNbr) {
		super(username, division, certNbr);
		this.permLevel = PermissionLevel.Nurse;
	}
}
