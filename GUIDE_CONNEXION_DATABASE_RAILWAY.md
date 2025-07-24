# Guide : Comment Connecter la Base de Données Railway PostgreSQL

## Étape 1 : Créer la Base de Données PostgreSQL sur Railway

### 1.1 Dans votre tableau de bord Railway
1. Cliquez sur **"New"**
2. Sélectionnez **"Deploy PostgreSQL"** (l'icône avec l'éléphant bleu)
3. Railway va créer automatiquement une base PostgreSQL

### 1.2 Vérifier la création
- Vous devriez voir un nouveau service PostgreSQL dans votre projet
- Il aura un nom comme "PostgreSQL" ou "postgres-xxxx"

## Étape 2 : Obtenir les Variables de Connexion

### 2.1 Cliquer sur votre service PostgreSQL
- Dans le tableau de bord Railway, cliquez sur le service PostgreSQL que vous venez de créer

### 2.2 Aller dans l'onglet "Variables"
- Vous verrez plusieurs variables automatiquement créées :
  - `DATABASE_URL` - URL complète de connexion
  - `PGHOST` - Adresse du serveur
  - `PGPORT` - Port (généralement 5432)
  - `PGDATABASE` - Nom de la base
  - `PGUSER` - Nom d'utilisateur
  - `PGPASSWORD` - Mot de passe

### 2.3 Copier DATABASE_URL
- La variable la plus importante est `DATABASE_URL`
- Elle ressemble à : `postgresql://username:password@host:port/database`

## Étape 3 : Connecter votre Application à la Base

### 3.1 Variables d'environnement automatiques
Railway va automatiquement fournir ces variables à votre application :
- Votre application utilise déjà `${DATABASE_URL}` dans la configuration
- Pas besoin de configuration manuelle !

### 3.2 Vérifier la configuration
Dans [`application-prod.properties`](src/main/resources/application-prod.properties), nous avons :
```properties
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/library_db}
```

Cela signifie :
- Si `DATABASE_URL` existe → utilise la base Railway
- Sinon → utilise localhost (pour le développement)

## Étape 4 : Lier les Services (IMPORTANT)

### 4.1 Connecter l'application à la base
1. Dans Railway, allez sur votre service **application** (pas PostgreSQL)
2. Cliquez sur l'onglet **"Variables"**
3. Cherchez s'il y a une section **"Service Variables"** ou **"Connected Services"**
4. **Connectez votre service PostgreSQL** à votre application

### 4.2 Alternative : Variables manuelles
Si la connexion automatique ne fonctionne pas :
1. Copiez la valeur de `DATABASE_URL` depuis le service PostgreSQL
2. Allez dans votre service application → Variables
3. Ajoutez manuellement :
   - Nom : `DATABASE_URL`
   - Valeur : `postgresql://username:password@host:port/database`

## Étape 5 : Déployer et Tester

### 5.1 Déployer
```bash
git add .
git commit -m "Connect to Railway PostgreSQL database"
git push
```

### 5.2 Vérifier les logs
1. Dans Railway, allez sur votre service application
2. Cliquez sur l'onglet **"Logs"**
3. Cherchez des messages comme :
   - ✅ `HikariPool-1 - Start completed` (connexion réussie)
   - ❌ `Connection refused` (problème de connexion)

## Étape 6 : Résolution des Problèmes

### 6.1 Si l'erreur "Connection refused" persiste
1. **Vérifiez que PostgreSQL est bien créé** dans Railway
2. **Vérifiez que DATABASE_URL est définie** dans votre application
3. **Redémarrez votre application** dans Railway

### 6.2 Vérifier DATABASE_URL
Dans les logs de votre application, vous devriez voir :
```
Using database URL: postgresql://username:password@host:port/database
```

### 6.3 Si DATABASE_URL n'apparaît pas
1. Allez dans PostgreSQL service → Variables
2. Copiez la valeur de `DATABASE_URL`
3. Allez dans Application service → Variables
4. Ajoutez manuellement `DATABASE_URL` avec la valeur copiée

## Étape 7 : Vérification Finale

### 7.1 Application qui démarre
- Votre application devrait démarrer sans erreur de base de données
- Les tables seront créées automatiquement par JPA

### 7.2 Accès à l'application
- Utilisez l'URL fournie par Railway pour accéder à votre application
- Vous devriez voir "Library Management System is running!"

## Notes Importantes

### Sécurité
- Ne jamais exposer DATABASE_URL dans le code
- Railway gère automatiquement les credentials

### Performance
- La base PostgreSQL Railway est partagée en plan gratuit
- Pour la production, considérez un plan payant

### Données
- `spring.jpa.hibernate.ddl-auto=create-drop` recrée les tables à chaque démarrage
- Pour conserver les données, changez vers `update` après le premier démarrage réussi

## Commandes Utiles

### Voir les variables d'environnement (dans les logs)
Ajoutez temporairement dans votre application :
```java
System.out.println("DATABASE_URL: " + System.getenv("DATABASE_URL"));
```

### Tester la connexion localement
```bash
# Si vous avez psql installé
psql "postgresql://username:password@host:port/database"