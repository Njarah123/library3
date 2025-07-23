-- Script pour réparer la table flyway_schema_history
-- Ce script supprime l'entrée échouée pour la migration V6

-- Supprimer l'entrée échouée pour V6
DELETE FROM flyway_schema_history WHERE version = '6' AND success = 0;

-- Mettre à jour le checksum de la migration V6 si elle existe et a réussi
-- Cela permet d'éviter les erreurs de validation du checksum
UPDATE flyway_schema_history SET checksum = NULL WHERE version = '6' AND success = 1;