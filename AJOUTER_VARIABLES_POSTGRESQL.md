# URGENT : Ajouter Toutes les Variables PostgreSQL

## Problème

L'application essaie encore de se connecter à `localhost:5432` parce que les variables `PGHOST`, `PGPORT`, etc. ne sont pas disponibles dans votre service application.

## Solution : Ajouter TOUTES les Variables Manuellement

### Étape 1 : Aller sur PostgreSQL et Copier TOUTES les Variables

1. **Cliquez sur votre service PostgreSQL** dans Railway
2. **Onglet "Variables"**
3. **Copiez ces 5 variables** (cliquez sur chacune et copiez sa valeur) :
   - `PGHOST` 
   - `PGPORT`
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`

### Étape 2 : Aller sur Votre Application

1. **Cliquez sur votre service APPLICATION** (PROJECT_GRP-10)
2. **Onglet "Variables"**

### Étape 3 : Ajouter les 5 Variables Une Par Une

**Ajoutez chaque variable** avec le bouton "Add Variable" :

1. **Variable 1** :
   - Name : `PGHOST`
   - Value : (la valeur copiée depuis PostgreSQL)

2. **Variable 2** :
   - Name : `PGPORT`
   - Value : (la valeur copiée depuis PostgreSQL)

3. **Variable 3** :
   - Name : `PGDATABASE`
   - Value : (la valeur copiée depuis PostgreSQL)

4. **Variable 4** :
   - Name : `PGUSER`
   - Value : (la valeur copiée depuis PostgreSQL)

5. **Variable 5** :
   - Name : `PGPASSWORD`
   - Value : (la valeur copiée depuis PostgreSQL)

### Étape 4 : Vérifier

Après avoir ajouté les 5 variables, votre service application devrait avoir :
- `DATABASE_URL` (déjà ajoutée)
- `PGHOST`
- `PGPORT`
- `PGDATABASE`
- `PGUSER`
- `PGPASSWORD`
- Les variables Railway système

### Résultat

Railway va automatiquement redéployer et votre application utilisera les vraies valeurs PostgreSQL au lieu de localhost !

---

**IMPORTANT : Vous devez ajouter les 5 variables PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD dans votre service APPLICATION.**