# Instructions Très Simples - Connecter PostgreSQL

## Ce Que Je Vois Dans Votre Capture

✅ **PARFAIT !** Vous êtes dans le bon endroit ! Je vois que vous avez :
- `DATABASE_URL` 
- `PGHOST`, `PGPORT`, `PGDATABASE`, etc.

## Étape 1 : Copier DATABASE_URL

1. **Dans votre capture actuelle** (service PostgreSQL)
2. **Cliquez sur la ligne `DATABASE_URL`** (la 2ème ligne)
3. **Copiez sa valeur** (les étoiles cachent la vraie valeur)
   - Cliquez sur les 3 points `...` à droite de `DATABASE_URL`
   - Ou cliquez sur l'icône "copier" si elle apparaît
   - La valeur ressemble à : `postgresql://username:password@host:port/database`

## Étape 2 : Aller sur Votre Application

1. **Fermez cette fenêtre PostgreSQL** (cliquez sur le X en haut à droite)
2. **Vous revenez au tableau de bord principal**
3. **Cliquez sur votre service APPLICATION** (pas PostgreSQL)
   - Il s'appelle probablement "PROJECT_GRP-10" ou similaire

## Étape 3 : Ajouter la Variable

1. **Dans votre service APPLICATION**, cliquez sur l'onglet **"Variables"**
2. **Cherchez un bouton** qui dit :
   - "Add Variable" OU
   - "New Variable" OU 
   - "+" OU
   - "Add"
3. **Cliquez sur ce bouton**
4. **Remplissez** :
   - **Name** : `DATABASE_URL`
   - **Value** : Collez la valeur que vous avez copiée

## Étape 4 : Sauvegarder

1. **Cliquez sur "Save"** ou "Add" ou "Create"
2. **Railway va automatiquement redéployer** votre application

## Résultat

Votre application va maintenant utiliser PostgreSQL au lieu de localhost !

---

**QUESTION : Arrivez-vous à copier la valeur de `DATABASE_URL` depuis votre capture actuelle ?**