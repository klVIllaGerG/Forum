/*
 Navicat MySQL Data Transfer

 Source Server         : 本地测试环境
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 07/08/2023 00:03:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for db_account
-- ----------------------------
DROP TABLE IF EXISTS `db_account`;
CREATE TABLE `db_account` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `username` varchar(255) NOT NULL,
                              `email` varchar(255) NOT NULL,
                              `password` varchar(255) NOT NULL,
                              `role` varchar(255) NOT NULL,
                              `avatar` varchar(255) DEFAULT NULL,
                              `register_time` datetime NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `unique_email` (`email`),
                              UNIQUE KEY `unique_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_account_details
-- ----------------------------
DROP TABLE IF EXISTS `db_account_details`;
CREATE TABLE `db_account_details` (
                                      `id` int NOT NULL,
                                      `gender` int NOT NULL,
                                      `phone` varchar(255) DEFAULT NULL,
                                      `qq` varchar(255) DEFAULT NULL,
                                      `wx` varchar(255) DEFAULT NULL,
                                      `desc` text,
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_account_privacy
-- ----------------------------
DROP TABLE IF EXISTS `db_account_privacy`;
CREATE TABLE `db_account_privacy` (
                                      `id` int NOT NULL AUTO_INCREMENT,
                                      `phone` boolean NOT NULL,
                                      `email` boolean NOT NULL,
                                      `wx` boolean NOT NULL,
                                      `qq` boolean NOT NULL,
                                      `gender` boolean NOT NULL,
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_notification
-- ----------------------------
DROP TABLE IF EXISTS `db_notification`;
CREATE TABLE `db_notification` (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `uid` int NOT NULL,
                                   `title` varchar(255) NOT NULL,
                                   `content` text NOT NULL,
                                   `type` varchar(50) NOT NULL,
                                   `url` varchar(255) DEFAULT NULL,
                                   `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_image_store
-- ----------------------------
DROP TABLE IF EXISTS `db_image_store`;
CREATE TABLE `db_image_store` (
                                  `uid` int NOT NULL,
                                  `name` varchar(255) NOT NULL,
                                  `time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_topic
-- ----------------------------
DROP TABLE IF EXISTS `db_topic`;
CREATE TABLE `db_topic` (
                            `id` int NOT NULL AUTO_INCREMENT,
                            `title` varchar(255) NOT NULL,
                            `content` text NOT NULL,
                            `type` int NOT NULL,
                            `time` datetime NOT NULL,
                            `uid` int NOT NULL,
                            `top` boolean DEFAULT FALSE,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_topic_comment
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_comment`;
CREATE TABLE `db_topic_comment` (
                                    `id` int NOT NULL AUTO_INCREMENT,
                                    `uid` int NOT NULL,
                                    `tid` int NOT NULL,
                                    `content` text NOT NULL,
                                    `time` datetime NOT NULL,
                                    `quote` int DEFAULT NULL,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_topic_type
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_type`;
CREATE TABLE `db_topic_type` (
                                 `id` int NOT NULL AUTO_INCREMENT,
                                 `name` varchar(255) NOT NULL,
                                 `desc` text,
                                 `color` varchar(10) NOT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_topic_interact_collect
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_interact_collect`;
CREATE TABLE `db_topic_interact_collect` (
                                             `uid` int NOT NULL,
                                             `tid` int NOT NULL,
                                             `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (`uid`, `tid`),
                                             UNIQUE KEY `unique_uid_tid` (`uid`, `tid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for db_topic_interact_like
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_interact_like`;
CREATE TABLE `db_topic_interact_like` (
                                          `uid` int NOT NULL,
                                          `tid` int NOT NULL,
                                          `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`uid`, `tid`),
                                          UNIQUE KEY `unique_uid_tid` (`uid`, `tid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
