# Library Management System

Un système de gestion de bibliothèque développé avec Spring Boot, offrant une interface complète pour la gestion des livres, utilisateurs et emprunts.

## 🚀 Fonctionnalités

- **Gestion des utilisateurs** : Étudiants, personnel et bibliothécaires
- **Gestion des livres** : Ajout, modification, suppression et recherche
- **Système d'emprunt** : Gestion des emprunts et retours
- **Tableaux de bord** : Interfaces dédiées par type d'utilisateur
- **Authentification sécurisée** : Spring Security avec gestion des rôles
- **Interface responsive** : Design moderne et adaptatif
- **Génération de rapports** : Export PDF et Excel
- **Codes QR** : Génération automatique pour les livres

## 🛠️ Technologies utilisées

- **Backend** : Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Frontend** : Thymeleaf, HTML5, CSS3, JavaScript
- **Base de données** : MySQL (développement), PostgreSQL (production)
- **Build** : Maven
- **Déploiement** : Docker, Railway

## 📦 Déploiement sur Railway (Gratuit)

### Prérequis
- Compte GitHub
- Compte Railway (gratuit)

### Étapes de déploiement

1. **Créer un repository GitHub**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/votre-username/library-management.git
   git push -u origin main
   ```

2. **Déployer sur Railway**
   - Aller sur [railway.app](https://railway.app)
   - Se connecter avec GitHub
   - Cliquer sur "New Project"
   - Sélectionner "Deploy from GitHub repo"
   - Choisir votre repository
   - Railway détectera automatiquement le Dockerfile

3. **Ajouter une base de données PostgreSQL**
   - Dans votre projet Railway, cliquer sur "New"
   - Sélectionner "Database" → "PostgreSQL"
   - Railway créera automatiquement la variable `DATABASE_URL`

4. **Configurer les variables d'environnement**
   - Dans Railway, aller dans l'onglet "Variables"
   - Ajouter :
     ```
     SPRING_PROFILES_ACTIVE=prod
     PORT=8080
     ```

5. **Déployer**
   - Railway déploiera automatiquement votre application
   - Vous obtiendrez une URL publique gratuite

## 🌐 Autres options de déploiement gratuit

### Render.com
- Jusqu'à 750h/mois gratuit
- Support Docker natif
- Base de données PostgreSQL gratuite

### Heroku (Plan gratuit limité)
- Dyno gratuit avec limitations
- Add-ons PostgreSQL disponibles

### Railway.app (Recommandé)
- $5 de crédit gratuit par mois
- Déploiement automatique depuis GitHub
- Support Docker excellent
- Base de données PostgreSQL incluse

## 🔧 Troubleshooting déploiement

### Problème : Maven wrapper manquant
**Erreur** : `cannot open ./.mvn/wrapper/maven-wrapper.properties: No such file`

**Solution** : Le projet inclut maintenant le fichier `maven-wrapper.properties` requis.

### Problème : Erreur Docker Hub sur Railway
**Erreur** : `failed to do request: Head "https://registry-1.docker.io/v2/library/openjdk/manifests/21-jre-slim": context canceled`

**Solutions** :
1. **Utiliser Nixpacks (Recommandé)** : Railway utilisera automatiquement Nixpacks au lieu de Docker
2. **Dockerfile alternatif** : Renommer `Dockerfile.alternative` en `Dockerfile` pour utiliser Eclipse Temurin
3. **Configuration railway.toml** : Le fichier `railway.toml` force l'utilisation de Nixpacks

### Problème : Repository GitHub non visible sur Railway
**Solutions** :
1. Vérifier les permissions GitHub dans Railway Settings → Integrations → GitHub
2. Attendre la synchronisation et rafraîchir la page
3. Vérifier que le repository est public
4. Utiliser l'URL directe : `https://github.com/joanjarah/bibli.git`

## 🔧 Configuration locale

### Prérequis
- Java 21
- Maven 3.6+
- MySQL 8.0+

### Installation
1. Cloner le repository
2. Configurer MySQL dans `application.properties`
3. Exécuter : `mvn spring-boot:run`
4. Accéder à : `http://localhost:8080`

## 👥 Comptes par défaut

Après le premier démarrage, vous pouvez créer des comptes via l'interface d'inscription ou utiliser les comptes de test si configurés.

## 📱 Fonctionnalités principales

- **Dashboard étudiant** : Consultation des livres, emprunts, objectifs de lecture
- **Dashboard personnel** : Gestion avancée, rapports
- **Dashboard bibliothécaire** : Administration complète du système
- **Recherche avancée** : Filtres multiples pour les livres
- **Notifications** : Système d'alertes pour les échéances
- **Responsive design** : Compatible mobile et desktop

## 🔒 Sécurité

- Authentification Spring Security
- Hashage des mots de passe avec BCrypt
- Protection CSRF
- Validation des données côté serveur
- Gestion des sessions sécurisée

## 📊 Rapports et exports

- Export PDF des listes de livres
- Export Excel des données d'emprunt
- Génération de codes QR pour les livres
- Statistiques d'utilisation

## 🎨 Interface utilisateur

- Design moderne et intuitif
- Thème sombre/clair
- Animations CSS fluides
- Icons Font Awesome
- Police Poppins pour une meilleure lisibilité

## 🚀 URL de déploiement

Une fois déployé sur Railway, votre application sera accessible via une URL du type :
`https://votre-app-name.up.railway.app`

## 📞 Support

Pour toute question ou problème, n'hésitez pas à ouvrir une issue sur GitHub.

---

**Développé avec ❤️ par l'équipe GRP-10**#   b i b l i 
 
 #   b i b l i 
 
 #   b i b l i 
 
 #   b i b l i 
 
 