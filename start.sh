#!/bin/bash

# Script de démarrage pour Railway
echo "Starting Library Management System..."

# Définir les variables d'environnement
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xmx512m -Xms256m"

# Lancer l'application
exec java $JAVA_OPTS -Dserver.port=$PORT -jar target/PROJECT_GRP-10-0.0.1-SNAPSHOT.jar