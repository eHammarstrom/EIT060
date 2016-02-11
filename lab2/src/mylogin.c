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
#define SALT_SIZE (2)
#define NOUSER (-1)
#define PW_FAILED (1)
#define PW_AGE (0)

void read_username(char *username);
void write_pw(int entry, int value, struct pwdb_passwd *p);

/*
   p->pw_name
   p->pw_passwd
   p->pw_uid
   p->pw_gid
   p->pw_gecos
   p->pw_dir
   p->pw_shell
   p->pw_age
   p->pw_failed
   */

int main(int argc, char **argv)
{
	char username[USERNAME_SIZE];
	char *password; // We store in heap, remember to free the memory.
	char salt[SALT_SIZE];
	struct pwdb_passwd *p;

	/* 
	 * Write "login: " and read user input. Copies the username to the
	 * username variable.
	 */
	read_username(username);

	// Get userinfo for further evaluations (password compare etc.).
	p = pwdb_getpwnam(username);
	if (p == NULL) {
		printf("Unsuccessful login.");
		return -1;
	}

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

	if (p->pw_failed < 0) {
		printf("This account has been locked, please contact an administrator.\n");
	} else if (strcmp(password, p->pw_passwd) == 0) {
		printf("Successful login.\n");
		printf("Previous failed logins: %d\n", p->pw_failed);
		if (p->pw_age > 10)
			printf("Your password is old, please consider changing it.\n");
		write_pw(PW_AGE, p->pw_age + 1, p);
		write_pw(PW_FAILED, 0, p);
	} else {
		printf("Unsuccessful login.\n");
		write_pw(PW_FAILED, p->pw_failed + 1, p);
		if (PW_FAILED > 5)
			write_pw(PW_FAILED, -1, p);
	}

	memset(password, '0', strlen(password));

	return 0;
}

void read_username(char *username)
{
	printf("login: ");
	fgets(username, USERNAME_SIZE, stdin);

	/* remove the newline included by getline() */
	username[strlen(username) - 1] = '\0';
}

void write_pw(int entry, int value, struct pwdb_passwd *p)
{
	if (entry == PW_FAILED) {
		p->pw_failed = value;
	} else if (entry == PW_AGE) {
		p->pw_age = value;
	} else {
		printf("Faulty entry passed");
		return;
	}

	switch (entry) {
		case PW_FAILED:
			p->pw_failed = value;
			break;
		case PW_AGE:
			p->pw_age = value;
			break;
		default:
			printf("Erroneous entry passed to write_pw.");
			return;
	}

	if (pwdb_update_user(p) != 0) {
		printf("pwdb_update_user returned error %s\n",
				pwdb_err2str(pwdb_errno));
	}
}








