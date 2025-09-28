FROM openjdk:17-jdk-slim

# Install PostgreSQL client for database connectivity
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY database-migrations/target/database-migrations-1.0.0.jar app.jar

# Run the migration application (one-shot job)
ENTRYPOINT ["java", "-jar", "app.jar"]
