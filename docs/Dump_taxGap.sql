-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: localhost    Database: taxgap
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `detail_json` text,
  `event_type` enum('INGESTION','RULE_EXECUTION','TAX_COMPUTATION') NOT NULL,
  `event_time` datetime(6) DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_txn` (`transaction_id`),
  KEY `idx_audit_event` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tax_computations`
--

DROP TABLE IF EXISTS `tax_computations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax_computations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `compliance_status` enum('COMPLIANT','UNDERPAID','OVERPAID','NON_COMPLIANT') NOT NULL,
  `expected_tax` decimal(19,2) DEFAULT NULL,
  `tax_gap` decimal(19,2) DEFAULT NULL,
  `transaction_ref_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_aqyakj7002ej2q92mmqyp424p` (`transaction_ref_id`),
  KEY `idx_comp_status` (`compliance_status`),
  CONSTRAINT `FKitoknq6ok1758oklr2wxnars3` FOREIGN KEY (`transaction_ref_id`) REFERENCES `transactions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tax_computations`
--

LOCK TABLES `tax_computations` WRITE;
/*!40000 ALTER TABLE `tax_computations` DISABLE KEYS */;
/*!40000 ALTER TABLE `tax_computations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tax_exceptions`
--

DROP TABLE IF EXISTS `tax_exceptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax_exceptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` varchar(100) DEFAULT NULL,
  `message` varchar(500) DEFAULT NULL,
  `rule_name` varchar(100) DEFAULT NULL,
  `severity` enum('HIGH','MEDIUM','LOW') NOT NULL,
  `event_time` datetime(6) DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_exc_customer` (`customer_id`),
  KEY `idx_exc_severity` (`severity`),
  KEY `idx_exc_rule` (`rule_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tax_exceptions`
--

LOCK TABLES `tax_exceptions` WRITE;
/*!40000 ALTER TABLE `tax_exceptions` DISABLE KEYS */;
/*!40000 ALTER TABLE `tax_exceptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tax_rules`
--

DROP TABLE IF EXISTS `tax_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_json` text,
  `enabled` bit(1) NOT NULL,
  `rule_name` varchar(100) NOT NULL,
  `rule_type` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fnqpc3081ur4er72ua7tr7u3a` (`rule_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tax_rules`
--

LOCK TABLES `tax_rules` WRITE;
/*!40000 ALTER TABLE `tax_rules` DISABLE KEYS */;
INSERT INTO `tax_rules` VALUES (1,'{\"threshold\": 1000000, \"severity\": \"HIGH\"}',_binary '','High Value Transaction Rule','HIGH_VALUE'),(2,'{\"severity\": \"HIGH\"}',_binary '','Refund Validation Rule','REFUND_VALIDATION'),(3,'{\"slabs\": [{\"above\": 50000, \"minRate\": 0.12}, {\"above\": 100000, \"minRate\": 0.18}], \"severity\": \"MEDIUM\"}',_binary '','GST Slab Violation Rule','GST_SLAB');
/*!40000 ALTER TABLE `tax_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(19,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_id` varchar(100) DEFAULT NULL,
  `txn_date` date DEFAULT NULL,
  `failure_reasons` varchar(1000) DEFAULT NULL,
  `original_transaction_id` varchar(100) DEFAULT NULL,
  `raw_payload` text,
  `reported_tax` decimal(19,2) DEFAULT NULL,
  `tax_rate` decimal(9,4) DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `transaction_type` enum('SALE','REFUND','EXPENSE') DEFAULT NULL,
  `validation_status` enum('SUCCESS','FAILURE') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_txn_customer` (`customer_id`),
  KEY `idx_txn_txn_id` (`transaction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'$2a$10$zsQffDCNYi1LLmZuedMKqesVmHDSJYJP7eMei.K80MClgEcUR.RPC','ADMIN','admin'),(2,'$2a$10$Sr.oy.BHpcDE7kF7r9Kbgez2Q6EAnivWgeWCkx7shVdtrGKzU7HKe','AUDITOR','auditor');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'taxgap'
--

--
-- Dumping routines for database 'taxgap'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-29 13:50:35
