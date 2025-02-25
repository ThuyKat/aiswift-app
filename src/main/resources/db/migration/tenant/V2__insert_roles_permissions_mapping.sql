
INSERT INTO `roles` (`id`, `name`) VALUES ('1', 'ADMIN');
INSERT INTO `roles` (`id`, `name`) VALUES ('2', 'SUPERVISOR');
INSERT INTO `roles` (`id`, `name`) VALUES ('3', 'STAFF_LEVEL_2');
INSERT INTO `roles` (`id`, `name`) VALUES ('4', 'STAFF_LEVEL_1');

INSERT INTO `permissions` (`id`, `name`) VALUES ('1', 'view_order');
INSERT INTO `permissions` (`id`, `name`) VALUES ('2', 'take_order');
INSERT INTO `permissions` (`id`, `name`) VALUES ('3', 'edit_order');
INSERT INTO `permissions` (`id`, `name`) VALUES ('4', 'request_refund');
INSERT INTO `permissions` (`id`, `name`) VALUES ('5', 'manage_refund');
INSERT INTO `permissions` (`id`, `name`) VALUES ('6', 'view_report');
INSERT INTO `permissions` (`id`, `name`) VALUES ('7', 'manage_staff');

INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '1');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '2');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '3');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '4');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '5');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '6');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('1', '7');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('2', '1');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('2', '2');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('2', '3');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('2', '4');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('2', '5');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('2', '6');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('3', '1');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('3', '2');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('3', '3');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('3', '4');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('4', '1');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('4', '2');
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES ('4', '4');
