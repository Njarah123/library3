# Utiliser OpenJDK 21 comme image de base
FROM openjdk:21-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier pom.xml et télécharger les dépendances
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Donner les permissions d'exécution au wrapper Maven
RUN chmod +x ./mvnw

# Télécharger les dépendances (pour optimiser le cache Docker)
RUN ./mvnw dependency:go-offline -B

# Copier le code source
COPY src ./src

# Construire l'application
RUN ./mvnw clean package -DskipTests

# Exposer le port
EXPOSE 8080

# Définir les variables d'environnement
ENV SPRING_PROFILES_ACTIVE=prod

# Lancer l'application
CMD ["java", "-jar", "target/PROJECT_GRP-10-0.0.1-SNAPSHOT.jar"]