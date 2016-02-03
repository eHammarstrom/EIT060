#include <stdio.h>
#include <unistd.h>
#include <crypt.h>

int main(int argc, char **argv)
{
  if (argc < 3 || argc > 3) {
    return 1;
  }
  
  char *passwd = argv[1];
  char *salt = argv[2];
  char *cPasswd = crypt(passwd, salt);

  puts(cPasswd);

  return 0;
}
