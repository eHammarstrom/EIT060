package utilities;

public class Agency extends User {
	public Agency(String username, String password, String division, long certNbr) {
		super(username, password, division, certNbr);
		this.permLevel = PermissionLevel.Agency;
	}
}
