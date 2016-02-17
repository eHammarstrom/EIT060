package utilities;

public class Agency extends User {
	public Agency(String username, String password, String division, long certNbr, boolean readMode) {
		super(username, password, division, certNbr, readMode);
		this.permLevel = PermissionLevel.Agency;
	}
}
