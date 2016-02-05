CC=gcc

CFLAGS=-Wall -g

all: openshell_demo pwdblib update_user userinfo

login:
	$(CC) mylogin.c pwdblib.c -o login $(CFLAGS) -lcrypt

login_clean:
	rm -f login
