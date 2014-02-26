-- phpMyAdmin SQL Dump
-- version 4.0.4.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Nov 06, 2013 at 10:05 PM
-- Server version: 5.5.32
-- PHP Version: 5.4.19

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `treasurehuntdb`
--
CREATE DATABASE IF NOT EXISTS `treasurehuntdb` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `treasurehuntdb`;

-- --------------------------------------------------------

--
-- Table structure for table `hunt`
--

CREATE TABLE IF NOT EXISTS `hunt` (
  `HuntId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `HuntName` varchar(15) NOT NULL,
  PRIMARY KEY (`HuntId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `huntquestions`
--

CREATE TABLE IF NOT EXISTS `huntquestions` (
  `HuntQuestionId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `HuntId` int(10) unsigned NOT NULL,
  `QuestionId` int(10) unsigned NOT NULL,
  PRIMARY KEY (`HuntQuestionId`),
  KEY `HuntId` (`HuntId`),
  KEY `QuestionId` (`QuestionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `questions`
--

CREATE TABLE IF NOT EXISTS `questions` (
  `QuestionId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Question` longtext NOT NULL,
  `URL` text NOT NULL,
  PRIMARY KEY (`QuestionId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `role`
--

CREATE TABLE IF NOT EXISTS `role` (
  `RoleId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Name` varchar(15) NOT NULL,
  PRIMARY KEY (`RoleId`),
  KEY `RoleId` (`RoleId`),
  KEY `RoleId_2` (`RoleId`),
  KEY `RoleId_3` (`RoleId`),
  KEY `RoleId_4` (`RoleId`),
  KEY `Name` (`Name`),
  KEY `RoleId_5` (`RoleId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `UserId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Name` varchar(20) NOT NULL,
  `Email` text NOT NULL,
  `Password` varchar(15) NOT NULL,
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `UserId_5` (`UserId`),
  KEY `UserId` (`UserId`),
  KEY `UserId_2` (`UserId`),
  KEY `UserId_3` (`UserId`),
  KEY `UserId_4` (`UserId`),
  KEY `UserId_6` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `userrole`
--

CREATE TABLE IF NOT EXISTS `userrole` (
  `UserRoleId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `RoleId` int(10) unsigned NOT NULL,
  `UserId` int(10) unsigned NOT NULL,
  PRIMARY KEY (`UserRoleId`),
  KEY `RoleId` (`RoleId`),
  KEY `UserId` (`UserId`),
  KEY `RoleId_2` (`RoleId`),
  KEY `UserId_2` (`UserId`),
  KEY `UserId_3` (`UserId`),
  KEY `RoleId_3` (`RoleId`),
  KEY `RoleId_4` (`RoleId`),
  KEY `UserId_4` (`UserId`),
  KEY `RoleId_5` (`RoleId`),
  KEY `UserId_5` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `huntquestions`
--
ALTER TABLE `huntquestions`
  ADD CONSTRAINT `huntquestions_ibfk_1` FOREIGN KEY (`HuntId`) REFERENCES `hunt` (`HuntId`) ON UPDATE CASCADE,
  ADD CONSTRAINT `huntquestions_ibfk_2` FOREIGN KEY (`QuestionId`) REFERENCES `questions` (`QuestionId`) ON UPDATE CASCADE;

--
-- Constraints for table `userrole`
--
ALTER TABLE `userrole`
  ADD CONSTRAINT `userrole_ibfk_2` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON UPDATE CASCADE,
  ADD CONSTRAINT `userrole_ibfk_1` FOREIGN KEY (`RoleId`) REFERENCES `role` (`RoleId`) ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
