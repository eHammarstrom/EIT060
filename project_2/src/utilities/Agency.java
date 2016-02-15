package utilities;

public class Agency extends User {
	public Agency(String username, String password, String division, int certNbr) {
		super(username, password, division, certNbr);
		this.permLevel = PermissionLevel.Agency;
	}
}
