-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: fdb1030.awardspace.net
-- Generation Time: May 20, 2025 at 11:39 PM
-- Server version: 8.0.32
-- PHP Version: 8.1.32

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `4632858_asca`
--

-- --------------------------------------------------------

--
-- Table structure for table `blocked_numbers`
--

CREATE TABLE `blocked_numbers` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `block_calls` tinyint(1) DEFAULT '0',
  `block_sms` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `blocked_numbers`
--

INSERT INTO `blocked_numbers` (`id`, `user_id`, `phone_number`, `block_calls`, `block_sms`, `created_at`) VALUES
(6, 3, '646754545', 1, 0, '2025-05-12 13:18:10');

-- --------------------------------------------------------

--
-- Table structure for table `call_log`
--

CREATE TABLE `call_log` (
  `id` int NOT NULL,
  `phone_number_id` int DEFAULT NULL,
  `caller_id` int DEFAULT NULL,
  `call_time` datetime DEFAULT NULL,
  `picked_up` tinyint(1) DEFAULT '0',
  `denied` tinyint(1) DEFAULT '0',
  `duration` int DEFAULT '0',
  `source` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `call_log`
--

INSERT INTO `call_log` (`id`, `phone_number_id`, `caller_id`, `call_time`, `picked_up`, `denied`, `duration`, `source`, `created_at`) VALUES
(4, 3, 16, NULL, 0, 0, 0, NULL, '2025-05-13 09:01:38'),
(5, 4, NULL, '2025-05-13 12:21:12', 0, 0, 0, 'screened_check', '2025-05-13 09:21:12'),
(6, 4, NULL, '2025-05-13 12:26:55', 0, 0, 0, 'screened_check', '2025-05-13 09:26:55'),
(7, 4, NULL, '2025-05-13 14:38:10', 0, 0, 0, 'screened_check', '2025-05-13 11:38:10'),
(8, 4, NULL, '2025-05-13 14:38:10', 0, 1, 0, 'blocked_by_user', '2025-05-13 11:38:10'),
(9, 4, NULL, '2025-05-13 14:42:35', 0, 0, 0, 'screened_check', '2025-05-13 11:42:34'),
(10, 4, NULL, '2025-05-13 14:42:35', 0, 1, 0, 'scam_blocked', '2025-05-13 11:42:35'),
(11, 4, NULL, '2025-05-13 14:44:10', 0, 0, 0, 'screened_check', '2025-05-13 11:44:10'),
(12, 4, NULL, '2025-05-13 14:44:10', 0, 1, 0, 'screened_frequency', '2025-05-13 11:44:10'),
(13, 5, NULL, '2025-05-13 20:18:19', 0, 0, 0, 'screened_check', '2025-05-13 17:18:18'),
(14, 5, NULL, '2025-05-13 20:18:19', 0, 1, 0, 'blocked_by_user', '2025-05-13 17:18:18'),
(15, 4, NULL, '2025-05-13 21:05:26', 0, 0, 0, 'screened_check', '2025-05-13 18:05:26'),
(16, 4, NULL, '2025-05-13 21:05:26', 0, 1, 0, 'blocked_by_user', '2025-05-13 18:05:26'),
(17, 4, NULL, '2025-05-15 09:21:36', 0, 0, 0, 'screened_check', '2025-05-15 06:21:36'),
(18, 4, NULL, '2025-05-15 09:23:04', 0, 0, 0, 'screened_check', '2025-05-15 06:23:04'),
(19, 4, NULL, '2025-05-15 09:23:46', 0, 0, 0, 'screened_check', '2025-05-15 06:23:46'),
(20, 4, NULL, '2025-05-15 09:52:23', 0, 0, 0, 'screened_check', '2025-05-15 06:52:24'),
(21, 4, NULL, '2025-05-15 09:52:44', 0, 0, 0, 'screened_check', '2025-05-15 06:52:44'),
(22, 4, NULL, '2025-05-15 15:30:05', 0, 0, 0, 'screened_check', '2025-05-15 12:30:05'),
(23, 4, NULL, '2025-05-15 15:35:57', 0, 0, 0, 'screened_check', '2025-05-15 12:35:57'),
(24, 6, NULL, '2025-05-15 20:55:05', 0, 0, 0, 'screened_check', '2025-05-15 17:55:05'),
(25, 7, NULL, '2025-05-16 10:33:16', 0, 0, 0, 'screened_check', '2025-05-16 07:33:16'),
(26, 8, NULL, '2025-05-17 10:40:06', 0, 0, 0, 'screened_check', '2025-05-17 07:40:07'),
(27, 9, NULL, '2025-05-17 17:56:34', 0, 0, 0, 'screened_check', '2025-05-17 14:56:36'),
(28, 4, NULL, '2025-05-19 09:35:16', 0, 0, 0, 'screened_check', '2025-05-19 06:35:17'),
(29, 10, NULL, '2025-05-19 09:35:23', 0, 0, 0, 'screened_check', '2025-05-19 06:35:23'),
(30, 10, NULL, '2025-05-19 09:35:23', 0, 1, 0, 'blocked_by_user', '2025-05-19 06:35:24');

-- --------------------------------------------------------

--
-- Table structure for table `phone_numbers`
--

CREATE TABLE `phone_numbers` (
  `id` int NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `phone_numbers`
--

INSERT INTO `phone_numbers` (`id`, `phone_number`, `created_at`) VALUES
(1, '+37125645458', '2025-05-13 08:24:26'),
(2, '+37125646546', '2025-05-13 08:53:01'),
(3, '+3712546464', '2025-05-13 08:55:30'),
(4, '+37120939902', '2025-05-13 09:21:12'),
(5, '+37128870015', '2025-05-13 17:18:18'),
(6, '26190313', '2025-05-15 17:55:05'),
(7, '25677751', '2025-05-16 07:33:16'),
(8, '+37129641469', '2025-05-17 07:40:07'),
(9, '+37125559610', '2025-05-17 14:56:36'),
(10, '+37120489983', '2025-05-19 06:35:23'),
(11, '265646464', '2025-05-20 16:50:47');

-- --------------------------------------------------------

--
-- Table structure for table `scam_numbers`
--

CREATE TABLE `scam_numbers` (
  `id` int NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `source` varchar(255) DEFAULT NULL,
  `added_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `phone_number_id` int DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `full_name`, `username`, `email`, `phone_number_id`, `password`, `token`) VALUES
(1, 'test1', 'jdhshs', 'jshshs', NULL, '$2y$10$zpQps6ID5weSpWMukvZrM.qVaSq6pvUjSteJCcXcxiCf6Rwh/Ysp6', NULL),
(2, 'test2', 'hdhshs', 'hdhdhz', NULL, '$2y$10$j8zPM1n2uFxJG2B9A94pOurfapp1.yQ8svCxAY1495/9qAbO..s1e', NULL),
(3, 'jdjdj', 'test1', 'bxbxhx', NULL, '$2y$10$hdgQgDdystsl9GqFMpWpGuXe4BeWy1fUbnBZSgE5zwUhEgJYApEZW', 'aa6e218d00c8fd50c0e56ee7a89e8eb96b2304c7f104c0c8b7afe66956e1cd83'),
(7, 'jdjdj', 'jdjsj', 'test2', NULL, '$2y$10$4B2AJu/p8g7nxuPXLpSfgODq9S7hNVDTTvY8XgtXlEdWoPKWcJxby', NULL),
(13, 'jdjdjsj', 'truba', 'stshdj', NULL, '$2y$10$RuvVK/Vs1LLC4SDl176r/OT6VHSmDDhkAj72KV53Wka7NzkyOj.DW', 'ac184383458d69a944c265b7057f916157709672410a53b09e65932ff6634f75'),
(16, 'test5', 'test5', 'test5', 3, '$2y$10$PNn/cCdYFL9kYBTqZCpewuNR5Pp6Aagy41Ep5NIrwE39oknUm7LKW', '6520e684affe114ffb77f3cd5d0d196ef98c163edb07d61c7cb53a5142b8ec00'),
(17, 'krists', 'krists1', 'straupeniekskgmail.com', 11, '$2y$10$oXX1TguRx4Rwr4/rPvyV5.1bLNLYv04DnicQxQ9W1jthNtDhEXs5a', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `blocked_numbers`
--
ALTER TABLE `blocked_numbers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `call_log`
--
ALTER TABLE `call_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `phone_number_id` (`phone_number_id`);

--
-- Indexes for table `phone_numbers`
--
ALTER TABLE `phone_numbers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `scam_numbers`
--
ALTER TABLE `scam_numbers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `fk_users_phone_number_id` (`phone_number_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `blocked_numbers`
--
ALTER TABLE `blocked_numbers`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `call_log`
--
ALTER TABLE `call_log`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `phone_numbers`
--
ALTER TABLE `phone_numbers`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `scam_numbers`
--
ALTER TABLE `scam_numbers`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `blocked_numbers`
--
ALTER TABLE `blocked_numbers`
  ADD CONSTRAINT `blocked_numbers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `call_log`
--
ALTER TABLE `call_log`
  ADD CONSTRAINT `call_log_ibfk_1` FOREIGN KEY (`phone_number_id`) REFERENCES `phone_numbers` (`id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_user_phone` FOREIGN KEY (`phone_number_id`) REFERENCES `phone_numbers` (`id`),
  ADD CONSTRAINT `fk_users_phone_number_id` FOREIGN KEY (`phone_number_id`) REFERENCES `phone_numbers` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
