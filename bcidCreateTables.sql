/**
* SQL for BCID mysql tables
* NOTE: JBD Created these not as the final database, but a place that had some essential information
* so pressing tasks could be completed and demonstrated.  Tom C: when you get your database up & running this
* can be phased out.
*/

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `user_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(50) not null primary key,
  `password` varchar(110) not null,
  `enabled` boolean not null,
  `email` char(64) DEFAULT NULL,
  `fullname` char(128) DEFAULT NULL,
  `institution` char(128) DEFAULT NULL,
  `IDlimit` int(11) DEFAULT NULL,
  `set_password` boolean not null,
  `admin` boolean not null,
   KEY (`USER_ID`)
) ENGINE=Innodb DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorities`;
CREATE TABLE authorities (
      username varchar(50) not null,
      authority varchar(50) not null,
      constraint fk_authorities_users foreign key(username) references users(username));
      create unique index ix_auth_username on authorities (username,authority);

DROP TABLE IF EXISTS `datasets`;

CREATE TABLE `datasets` (
  `datasets_id` int(11) NOT NULL AUTO_INCREMENT,
  `ezidMade` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'indicates if EZID has been made',
  `ezidRequest` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'indicates if we want system to request EZID, all datasets by default get an EZID request',
  `suffixPassthrough` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'indicates if we want to use suffixPassthrough for this identifier',
  `internalID` char(36) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'The internal ID for this dataset',
  `prefix` text NOT NULL DEFAULT '' COMMENT 'ark:/1234/ab1',
  `users_id` int(10) UNSIGNED NOT NULL COMMENT 'who created this data',
  `doi` char(36) COMMENT 'DOI linked to this dataset identifier',
  `title` text COMMENT 'title for this dataset',
  `webaddress` text COLLATE utf8_bin COMMENT 'the target URL for this dataset',
  `graph` text  COMMENT 'A reference to a graph, used by the biocode-fims project for storing graph references for a particular dataset',
  `resourceType` text NOT NULL COMMENT 'default resource type for this dataset, stored as a URI',
  `resourceAlias` text NOT NULL COMMENT 'alias to use for the resource Type',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp of insertion',
  PRIMARY KEY `datasets_datasets_id` (`datasets_id`),
  KEY `datasets_users_id` (`users_id`),
  CONSTRAINT `FK_dataset_users`  FOREIGN KEY (`users_id`) REFERENCES `users` (`USER_ID`)
) ENGINE=Innodb DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `identifiers`;

CREATE TABLE `identifiers` (
  `identifiers_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Internal ID assigned to all BCIDs whether they are UUIDs, new IDs, or non-EZID requests',
  `ezidMade` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'indicates if EZID has been made',
  `ezidRequest` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'indicates if we want system to request EZID to be made here',
  `localid` varchar(255) COMMENT 'localID -- user specified (can be a UUID)',
  `webaddress` text  COLLATE utf8_bin COMMENT 'the target URL -- user specified',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp of insertion',
  `loadedSetUUID` char(36) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'a UUID that is common to all of the identifiers loaded at one time',
  `datasets_id` int NOT NULL COMMENT 'The set of data that this identifier belongs to',
  UNIQUE KEY `identifiers_identifiers_id_idx` (`identifiers_id`),
  KEY `identifiers_loadedSetUUID_idx` (`loadedSetUUID`),
  KEY `identifiers_datasets_idx` (`datasets_id`),
  -- COMMENT 'make sure that localid columns are unique within a dataset'
  UNIQUE   `identifiers_localid_datasets_id_idx` (`localid`,`datasets_id`),
  CONSTRAINT `FK_identifiers_datasets` FOREIGN KEY(`datasets_id`) REFERENCES `datasets` (`datasets_id`)
) ENGINE=Innodb DEFAULT CHARSET=utf8;


/**
* GRM tables
*/
DROP TABLE IF EXISTS `projects`;

CREATE TABLE `projects` (
  `project_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique, internal key for this project',
  `expedition_id` int(11),
  `internalID` char(36) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'The internal ID for this project',
  `project_code` varchar(6) NOT NULL DEFAULT '' COMMENT 'The short name for this project',
  `project_title` varchar(128) NOT NULL DEFAULT '' COMMENT 'Title for this project, will be used to populate group title',
  `abstract` text COMMENT 'The abstract for this particular project',
  `users_id` int(10) DEFAULT NULL COMMENT 'who created this data',
  `ts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp of insertion',
  UNIQUE KEY `project_project_id_idx` (`project_id`),
  UNIQUE KEY `project_projectcode_expedition_idx` (`project_code`,`expedition_id`),
  KEY `project_expedition_id_idx` (`expedition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `projectsBCIDs`;

CREATE TABLE `projectsBCIDs` (
  `projectsBCIDs_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique, internal key for this element',
  `project_id` int(11) NOT NULL COMMENT 'The project_id',
  `datasets_id` int NOT NULL COMMENT 'The dataset_id',
  UNIQUE KEY `projectsBCIDs_projectsBCIDs_id` (`projectsBCIDs_id`),
  KEY `projectsBCIDs_project_id` (`project_id`),
  KEY `datasets_id` (`datasets_id`),
  CONSTRAINT `FK_projectsBCIDs_datasets` FOREIGN KEY(`datasets_id`) REFERENCES `datasets` (`datasets_id`),
  CONSTRAINT `FK_projectsBCIDs_project` FOREIGN KEY(`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;


DROP TABLE IF EXISTS `expeditions`;

CREATE TABLE `expeditions` (
  `expedition_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique, internal key for this expeditions',
  `expedition_code` varchar(6) NOT NULL DEFAULT '' COMMENT 'The short name for this expedition',
  `expedition_title` varchar(128) NOT NULL DEFAULT '' COMMENT 'Title for this expedition',
  `abstract` text COMMENT 'The abstract for this particular expecition',
    `bioValidator_validation_xml` text COMMENT 'The bioValidator XML Validation Specification, published under the id/schemas webservice',
  `ts` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'timestamp of insertion',
  `users_id` int(11) UNSIGNED NOT NULL COMMENT 'The user_id of the expedition admin',
  UNIQUE KEY `expeditions_expedition_id_idx` (`expedition_id`),
  KEY `expeditions_users_id_idx` (`users_id`),
  CONSTRAINT `FK_expeditions_user`  FOREIGN KEY (`users_id`) REFERENCES `users` (`USER_ID`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `usersExpeditions`;

CREATE TABLE `usersExpeditions` (
  `usersExpeditions_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'The unique internal key',
  `expedition_id` int(11) NOT NULL COMMENT 'The expedition Id',
  `users_id` int(11) UNSIGNED NOT NULL COMMENT 'The users id',
  UNIQUE KEY `usersExpeditions_usersExpeditions_id` (`usersExpeditions_id`),
  UNIQUE KEY `usersExpeditions_users_id_expedition_id_idx` (`users_id`, `expedition_id`),
  KEY `usersExpeditions_expeditions_id` (`expedition_id`),
  KEY `usersExpeditions_users_id` (`users_id`),
  CONSTRAINT `FK_usersExpeditions_user`  FOREIGN KEY (`users_id`) REFERENCES `users` (`USER_ID`),
  CONSTRAINT `FK_usersExpeditions_expedition` FOREIGN KEY(`expedition_id`) REFERENCES `expeditions` (`expedition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

