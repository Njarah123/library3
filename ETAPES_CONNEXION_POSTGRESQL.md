# Étapes pour Connecter PostgreSQL à votre Application

Vous avez déjà un service PostgreSQL, mais les variables ne sont pas visibles dans votre application. Voici comment les connecter :

## Étape 1 : Récupérer DATABASE_URL depuis PostgreSQL

### 1.1 Cliquer sur votre service PostgreSQL
- Dans Railway, cliquez sur le service **PostgreSQL** (pas votre application)

### 1.2 Aller dans l'onglet "Variables"
- Vous devriez voir plusieurs variables :
  - `DATABASE_URL`
  - `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

### 1.3 Copier DATABASE_URL
- Cliquez sur `DATABASE_URL`
- Copiez sa valeur (elle ressemble à : `postgresql://username:password@host:port/database`)

## Étape 2 : Ajouter DATABASE_URL à votre Application

### 2.1 Aller sur votre service Application
- Cliquez sur votre service application (PROJECT_GRP-10)

### 2.2 Aller dans l'onglet "Variables"
- C'est là où vous avez pris la capture d'écran

### 2.3 Ajouter la variable manuellement
1. Cherchez un bouton **"Add Variable"** ou **"New Variable"** ou **"+"**
2. Ajoutez :
   - **Name/Nom** : `DATABASE_URL`
   - **Value/Valeur** : La valeur que vous avez copiée depuis PostgreSQL

## Étape 3 : Vérifier et Redéployer

### 3.1 Vérifier que DATABASE_URL apparaît
- Dans les variables de votre application, vous devriez maintenant voir `DATABASE_URL`

### 3.2 Redéploiement automatique
- Railway va automatiquement redéployer votre application
- Elle va maintenant utiliser PostgreSQL au lieu de localhost

## Étape 4 : Vérifier les Logs

### 4.1 Aller dans les logs
- Service Application → Onglet "Logs"

### 4.2 Chercher les messages de connexion
- ✅ Succès : `HikariPool-1 - Start completed`
- ❌ Échec : `Connection to localhost:5432 refused`

## Alternative : Connexion Automatique

Si vous ne trouvez pas le bouton "Add Variable", essayez :

1. **Service PostgreSQL** → Onglet "Connect"
2. Cherchez votre service application dans la liste
3. Cliquez pour connecter automatiquement

## Résultat Attendu

Après avoir ajouté `DATABASE_URL` :
- Votre application va se connecter à PostgreSQL Railway
- Plus d'erreur "Connection to localhost:5432 refused"
- L'application va démarrer correctement

**Dites-moi si vous trouvez le bouton pour ajouter une variable dans votre service application !**