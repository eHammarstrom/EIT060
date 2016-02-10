**Problem 1: Why is the text not echoed on the screen when the user enters the password?

*Echoing is disabled to prevent revealing the password.

**Problem 2: Does it exist some hash value that corresponds to two (or more) different passwords?

*Hashes is most likely to be replaced with an x and stored in the /etc/shadow file for security reasons. In the pwfile we store failed and successful logins stored in the last two fields of an entry.
