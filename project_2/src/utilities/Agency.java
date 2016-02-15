package utilities;

public class Agency extends User {
	public Agency(String username, String password, int certNbr) {
		super(username, password, certNbr);
		this.permLevel = PermissionLevel.Agency;
	}
}
