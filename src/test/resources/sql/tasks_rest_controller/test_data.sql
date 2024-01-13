insert into t_application_user(id, c_username, c_password)
values ('5e9a2498-b21b-11ee-a776-3f5e948ed997', 'user1', '{noop}password1'),
       ('5fbc4f2c-b21b-11ee-a7ef-6f53420645e2', 'user2', '{noop}password2');

insert into t_task(id, c_details, c_completed,id_application_user)
values ('cb8e1f2c-b140-11ee-bfa0-2bfffbc562af', 'First task', false,'5e9a2498-b21b-11ee-a776-3f5e948ed997'),
       ('d8a71876-b140-11ee-a438-c349ef386ab1', 'Second task', true,'5e9a2498-b21b-11ee-a776-3f5e948ed997'),
       ('aa1c9574-b21e-11ee-9805-03b15bac052e','Third task',false,'5fbc4f2c-b21b-11ee-a7ef-6f53420645e2');

