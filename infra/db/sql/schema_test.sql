USE `schoolday_test`;

CREATE TABLE `user` (
        `id` varchar(36) NOT NULL,
        `email` varchar(100) NOT NULL,
        `password` varchar(255) NOT NULL,
        `first_name` varchar(100) NOT NULL,
        `last_name` varchar(100) NOT NULL,
        `created` datetime NOT NULL,
        `updated` datetime,
        `status` varchar(50) DEFAULT 'ENABLED' NOT NULL,
        PRIMARY KEY (`id`),
        CONSTRAINT `unique_user_email` UNIQUE (`email`));

CREATE TABLE `role` (
        `id` varchar(36) NOT NULL,
        `name` varchar(128) NOT NULL,
        `status` varchar(50) NOT NULL,
        `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
        `updated` timestamp,
        `permissions` mediumtext,
        PRIMARY KEY (`id`),
        CONSTRAINT `unique_role_name` UNIQUE(`name`)
);

CREATE TABLE `child` (
        `id` varchar(36) NOT NULL,
        `first_name` varchar(100) NOT NULL,
        `last_name` varchar(100) NOT NULL,
        `date_of_birth` date NOT NULL,
        `status` varchar(50) NOT NULL,
        `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
        `updated` timestamp,
        PRIMARY KEY (`id`)
);

CREATE TABLE `user_role` (
        `user_id` varchar(36) NOT NULL,
        `role_id` varchar(36) NOT NULL,
        `status` varchar(50) NOT NULL DEFAULT 'ENABLED',
        `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
        `updated` timestamp,
        PRIMARY KEY (`user_id`, `role_id`),
        CONSTRAINT `fk_user_role_role_id_role_id` FOREIGN KEY(`role_id`) REFERENCES `role`(`id`),
        CONSTRAINT `fk_user_role_user_id_user_id` FOREIGN KEY(`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `parent_child` (
        `id` varchar(36) NOT NULL,
        `parent_id` varchar(36) NOT NULL,
        `child_id` varchar(36) NOT NULL,
        `status` varchar(50) NOT NULL DEFAULT 'ENABLED',
        `relation` varchar(50) NOT NULL,
        `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
        `updated` timestamp,
        PRIMARY KEY (`id`),
        CONSTRAINT `fk_parent_child_parent_id_user_id` FOREIGN KEY(`parent_id`) REFERENCES `user`(`id`),
        CONSTRAINT `fk_parent_child_child_id_child_id` FOREIGN KEY(`child_id`) REFERENCES `child`(`id`)
);

CREATE TABLE `user_session` (
        `token` VARCHAR(32) NOT NULL,
        `user_id` VARCHAR(36) NOT NULL,
        `active` TINYINT(1) NOT NULL DEFAULT 1,
        `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        `updated` TIMESTAMP,
        PRIMARY KEY (`token`),
        CONSTRAINT `fk_user_session_user_id_user_id` FOREIGN KEY(`user_id`) REFERENCES `user`(`id`)
);

exit; 