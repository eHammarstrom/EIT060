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

void read_username(char *username)
{
	printf("login: ");
	fgets(username, USERNAME_SIZE, stdin);

	/* remove the newline included by getline() */
	username[strlen(username) - 1] = '\0';
}

/*
   p->pw_name
   p->pw_passwd
   p->pw_uid
   p->pw_gid
   p->pw_gecos
   p->pw_dir
   p->pw_shell
   */

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

	// Get userinfo for further evaluations (password compare etc.).
	struct pwdb_passwd *p = pwdb_getpwnam(username);

	/*
	 * Write "password: " and read user input. Copies the password to the
	 * password variable.
	 */
	password = getpass("password: ");
	
	/*
	 * Here we grab the salt by the given username earlier.
	 */
	strncpy(salt, p->pw_passwd, SALT_SIZE);

	/*
	 * Now it is time to encrypt it with the password input 
	 * so we can match it with the pwfile and accept/deny user.
	 */
	password = crypt(password, salt);

	if (p != NULL && strcmp(password, p->pw_passwd) == 0) {
		printf("Successful login.");
		return 0;
	} else {
		printf("Unsuccessful login.");
		return -1;
	}
}



