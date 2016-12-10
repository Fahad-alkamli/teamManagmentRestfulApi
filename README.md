# teamManagmentRestfulApi
My team managment restful Api 

Notes:

1- don't forget to change the these settings in your sql server to make sure that a connection stays alive as long as possible

A. nano /etc/mysql/my.cnf

B. add the following to the end of the file: 

[mysqld]
wait_timeout = 86400
interactive_timeout = 86400

C. make sure you leave a line befoe and after the section 