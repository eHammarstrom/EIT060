/*
 * Shows user info from local pwfile.
 *  
 * Usage: userinfo username
 */

#define _XOPEN_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pwd.h>
#include "pwdblib.h"   /* include header declarations for pwdblib.c */

/* Define some constants. */
#define USERNAME_SIZE (32)
#define PASSWORD_SIZE (8)
#define SALT_SIZE (2)
#define NOUSER (-1)

/*
int print_info(const char *username)
{
	struct pwdb_passwd *p = pwdb_getpwnam(username);
	if (p != NULL) {
		printf("Name: %s\n", p->pw_name);
		printf("Passwd: %s\n", p->pw_passwd);
		printf("Uid: %u\n", p->pw_uid);
		printf("Gid: %u\n", p->pw_gid);
		printf("Real name: %s\n", p->pw_gecos);
		printf("Home dir: %s\n",p->pw_dir);
		printf("Shell: %s\n", p->pw_shell);
		return 0;
	} else {
		return NOUSER;
	}
}
*/

void read_username(char *username)
{
	printf("login: ");
	fgets(username, USERNAME_SIZE, stdin);

	/* remove the newline included by getline() */
	username[strlen(username) - 1] = '\0';
}

void read_salt(char *password, char *salt)
{
	strncpy(salt, password, SALT_SIZE);
}

int main(int argc, char **argv)
{
	char username[USERNAME_SIZE];
	char *password;
	char salt[SALT_SIZE];

	/* 
	 * Write "login: " and read user input. Copies the username to the
	 * username variable.
	 */
	read_username(username);
	// Get userinfo for further evaluations.
	struct pwdb_passwd *p = pwdb_getpwnam(username);
	/*
	 * Write "password: " and read user input. Copies the password to the
	 * password variable.
	 */
	password = getpass("password: ");
	//printf("%s\n", password);
	/*
	 * First get salt of user.
	 * Now it is time to encrypt it with the password input 
	 * so we can match it with the pwfile and accept/deny user.
	 */
	read_salt(p->pw_passwd, salt);
	password = crypt(password, salt);

	//int cmp = strcmp(password, p->pw_passwd);

	/*
	if (cmp < 0)
		printf("password is less than p->pw_passwd\n");
	else if (cmp > 0)
		printf("p->pw_passwd is less than password\n");
	else if (cmp == 0)
		printf("password is equal to p->pw_passwd\n");
	*/

	if (p != NULL && strcmp(password, p->pw_passwd) == 0) {
		printf("Successful login.");
		return 0;
	} else {
		printf("Unsuccessful login.");
		return -1;
	}
	

	/* Show user info from our local pwfile. */
	
	/*
	if (print_info(username) == NOUSER) {
		// if there are no user with that username...
		printf("\nFound no user with name: %s\n", username);   
		return 0;
	} else {
		// if there is a user with that username 
		printf("\nFound user with name: %s\n", username);
		return 0;
	}
	*/
}



