INSERT INTO privilege(id, name) values (1, 'CREATE') ON CONFLICT DO NOTHING;
INSERT INTO privilege(id, name) values (2, 'READ') ON CONFLICT DO NOTHING;
INSERT INTO privilege(id, name) values (3, 'DELETE') ON CONFLICT DO NOTHING;
INSERT INTO privilege(id, name) values (4, 'UPDATE') ON CONFLICT DO NOTHING;

INSERT INTO role(id, name, title) values (1, 'ROLE_SUPER_USER', 'Super User') ON CONFLICT DO NOTHING;
INSERT INTO role(id, name, title) values (2, 'ROLE_POWER_USER', 'Power User') ON CONFLICT DO NOTHING;

INSERT INTO roles_privileges (role_id, privilege_id) values (1,1) ON CONFLICT DO NOTHING;
INSERT INTO roles_privileges (role_id, privilege_id) values (1,2) ON CONFLICT DO NOTHING;
INSERT INTO roles_privileges (role_id, privilege_id) values (1,3) ON CONFLICT DO NOTHING;
INSERT INTO roles_privileges (role_id, privilege_id) values (1,4) ON CONFLICT DO NOTHING;
INSERT INTO roles_privileges (role_id, privilege_id) values (2,2) ON CONFLICT DO NOTHING;
