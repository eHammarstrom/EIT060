package utilities;

public class Agency extends User {
	public Agency(String username, Division division, String certNbr) {
		super(username, division, certNbr);
		this.permLevel = PermissionLevel.Agency;
	}
}
