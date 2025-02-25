-- table: owners
CREATE TABLE `global_multi_tenant`.`owners` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(45) NOT NULL,
  `last_name` VARCHAR(45) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `status` ENUM('ACTIVE', 'DISABLE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),  
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE);
  
  -- table: tenants
  CREATE TABLE `global_multi_tenant`.`tenants` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_id` BIGINT NOT NULL,
  `owner_role` ENUM('OWNER','ADMIN') NOT NULL DEFAULT 'OWNER',
  `name` VARCHAR(100) NOT NULL,
  `db_name` VARCHAR(100) NOT NULL,
  `status` ENUM('ACTIVE', 'DISABLE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `db_name_UNIQUE` (`db_name` ASC) VISIBLE,
  INDEX `fk_owner_id_idx` (`owner_id` ASC) VISIBLE,
  CONSTRAINT `fk_owner_id`
    FOREIGN KEY (`owner_id`)
    REFERENCES `global_multi_tenant`.`owners` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);