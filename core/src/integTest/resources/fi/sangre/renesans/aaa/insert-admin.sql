set schema 'data';

INSERT INTO  "user" (id, username, first_name,last_name,email,password,enabled,token_expired) values (nextval('user_id_seq'), 'admin','adminFirstName','adminLastName','admin@sangre.fi','$2a$10$e5TMfTmKKUNMwP2SOIsc4.wFCzOFRufXq8as74OY7rmXg.aGonOzu',true,true);
-- INSERT INTO  "user" (id, username, first_name,last_name,email,password,enabled,token_expired) values (nextval('user_id_seq'), 'user','userFirstName','userLastName','user@sangre.fi','$2a$10$e5TMfTmKKUNMwP2SOIsc4.wFCzOFRufXq8as74OY7rmXg.aGonOzu',true ,true );

INSERT INTO  users_roles (user_id, role_id) values (1,1);
-- INSERT INTO  users_roles (user_id, role_id) values (2,2);