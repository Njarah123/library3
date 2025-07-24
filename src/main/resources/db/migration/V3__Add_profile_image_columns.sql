-- Migration pour ajouter les colonnes d'image de profil à la table users
-- Cette migration est nécessaire pour supporter le stockage des images en base de données

-- Ajouter la colonne pour stocker les données binaires de l'image de profil
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_data BYTEA;

-- Ajouter la colonne pour stocker le type de contenu de l'image
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_content_type VARCHAR(100);

-- Créer un index sur profile_image_content_type pour optimiser les requêtes
CREATE INDEX IF NOT EXISTS idx_users_profile_image_content_type ON users(profile_image_content_type);

-- Commentaires pour documenter les nouvelles colonnes
COMMENT ON COLUMN users.profile_image_data IS 'Données binaires de l''image de profil stockées en base pour la persistance en production';
COMMENT ON COLUMN users.profile_image_content_type IS 'Type MIME de l''image de profil (ex: image/jpeg, image/png)';