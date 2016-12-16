SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;


CREATE TABLE `assign_task_to_user` (
  `task_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `project` (
  `project_id` int(11) NOT NULL,
  `project_name` varchar(255) NOT NULL,
  `start_date` varchar(255) NOT NULL,
  `end_date` varchar(255) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `project_memebers` (
  `project_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `restore_password_requests` (
  `user_id` int(11) NOT NULL,
  `token` int(11) NOT NULL,
  `failed_tries_counter` int(11) NOT NULL DEFAULT '0',
  `expire_date` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `tasks` (
  `project_id` int(11) NOT NULL,
  `task_id` int(11) NOT NULL,
  `task_summary` mediumtext NOT NULL,
  `task_start_Date` varchar(255) NOT NULL,
  `task_end_Date` varchar(255) NOT NULL,
  `done_total_hours` double NOT NULL DEFAULT '0',
  `completed` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL,
  `admin` tinyint(1) NOT NULL DEFAULT '0',
  `user_name` varchar(255) NOT NULL,
  `user_email` varchar(255) NOT NULL,
  `user_password` varchar(255) NOT NULL,
  `session` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `assign_task_to_user`
  ADD PRIMARY KEY (`task_id`,`user_id`),
  ADD KEY `task_id` (`task_id`),
  ADD KEY `user_id` (`user_id`);

ALTER TABLE `project`
  ADD PRIMARY KEY (`project_id`);

ALTER TABLE `project_memebers`
  ADD PRIMARY KEY (`project_id`,`user_id`),
  ADD KEY `members2_forign` (`user_id`);

ALTER TABLE `restore_password_requests`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `token` (`token`);

ALTER TABLE `tasks`
  ADD PRIMARY KEY (`task_id`),
  ADD KEY `project_id` (`project_id`),
  ADD KEY `task_id` (`task_id`);

ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `user_email` (`user_email`),
  ADD UNIQUE KEY `session` (`session`),
  ADD KEY `user_email_2` (`user_email`);


ALTER TABLE `project`
  MODIFY `project_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;
ALTER TABLE `tasks`
  MODIFY `task_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;
ALTER TABLE `user`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

ALTER TABLE `assign_task_to_user`
  ADD CONSTRAINT `assign_task_to_user_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`task_id`),
  ADD CONSTRAINT `members2_forign2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`);

ALTER TABLE `project_memebers`
  ADD CONSTRAINT `members2_forign` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  ADD CONSTRAINT `members_forign` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`);

ALTER TABLE `restore_password_requests`
  ADD CONSTRAINT `restore_password_requests_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`);

ALTER TABLE `tasks`
  ADD CONSTRAINT `role_id_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


INSERT INTO `user` (`user_id`, `admin`, `user_name`, `user_email`, `user_password`, `session`) VALUES (NULL, '1', 'admin', 'd0l1@hotmail.com', 'admin123456', NULL);