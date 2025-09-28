# Database Migrations Module

This standalone module handles database schema migrations and Elasticsearch index management for the Flight Booking System. It's designed to run independently before deploying application services, following production deployment best practices.

## 🎯 Purpose

- **PostgreSQL Schema Migrations**: Uses Flyway for versioned database schema changes
- **Elasticsearch Index Management**: Creates and manages search indices with proper mappings
- **Deployment Safety**: Runs migrations before application deployment to ensure schema consistency
- **CI/CD Integration**: Designed for automated deployment pipelines

## 📁 Structure

```
database-migrations/
├── src/main/
│   ├── kotlin/com/airline/migration/
│   │   ├── DatabaseMigrationApplication.kt    # Main application
│   │   ├── service/
│   │   │   ├── PostgreSQLMigrationService.kt  # Flyway migrations
│   │   │   └── ElasticsearchMigrationService.kt # ES index management
│   │   └── controller/
│   │       └── MigrationStatusController.kt   # Status endpoints
│   └── resources/
│       ├── application.yml                    # Configuration
│       ├── db/migration/                      # SQL migration scripts
│       │   ├── V1__Create_airports_table.sql
│       │   ├── V2__Create_routes_table.sql
│       │   ├── V3__Create_flights_table.sql
│       │   └── V4__Create_flight_schedules_table.sql
│       └── elasticsearch/
│           └── flight-routes-mapping.json     # ES index mappings
├── scripts/
│   └── run-migrations.sh                     # Deployment script
├── docker-compose.migration.yml              # Docker setup
├── Dockerfile                                # Container image
└── pom.xml                                   # Maven configuration
```

## 🚀 Usage

### Local Development

```bash
# Start infrastructure
docker-compose -f docker-compose.migration.yml up -d postgres elasticsearch

# Run migrations
cd database-migrations
mvn spring-boot:run

# Check status
curl http://localhost:8080/migration/status
```

### Production Deployment

```bash
# 1. Build the migration module
mvn clean package

# 2. Run migrations before deploying services
./scripts/run-migrations.sh

# 3. Deploy application services after migrations complete
```

### Docker Deployment

```bash
# Build image
docker build -t flight-booking-migrations .

# Run with environment variables
docker run --rm \
  -e DB_HOST=your-postgres-host \
  -e DB_USER=your-db-user \
  -e DB_PASSWORD=your-db-password \
  -e ELASTICSEARCH_URIS=http://your-es-host:9200 \
  flight-booking-migrations
```

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | flightdb | Database name |
| `DB_USER` | flightuser | Database user |
| `DB_PASSWORD` | flightpass | Database password |
| `ELASTICSEARCH_URIS` | http://localhost:9200 | Elasticsearch URLs |
| `POSTGRESQL_MIGRATION_ENABLED` | true | Enable PostgreSQL migrations |
| `ELASTICSEARCH_MIGRATION_ENABLED` | true | Enable Elasticsearch migrations |
| `POSTGRESQL_VALIDATE_ONLY` | false | Only validate, don't execute |
| `ELASTICSEARCH_RECREATE_INDICES` | false | Recreate existing indices |

### Application Properties

```yaml
migration:
  postgresql:
    enabled: true
    validate-only: false
  elasticsearch:
    enabled: true
    recreate-indices: false
    indices:
      flight-routes: flight-routes-v1
```

## 📊 Database Schema

### PostgreSQL Tables

1. **airports** - Airport master data (50 airports)
2. **routes** - Flight routes with pricing (1500+ routes)
3. **flights** - Flight schedules and details (4500+ flights)
4. **flight_schedules** - Daily flight instances with availability

### Elasticsearch Indices

1. **flight-routes-v1** - Precomputed route data for fast search
   - Nested route segments
   - Pricing and duration optimization
   - Real-time availability updates

## 🔍 Monitoring

### Health Check Endpoints

```bash
# Application health
GET /migration/health

# Migration status
GET /migration/status
```

### Migration Status Response

```json
{
  "postgresql": {
    "current": "4",
    "pending": 0,
    "applied": 4,
    "failed": 0
  },
  "elasticsearch": {
    "flight-routes-v1": {
      "exists": true,
      "status": "ready"
    }
  },
  "timestamp": 1640995200000
}
```

## 🔄 CI/CD Integration

### GitHub Actions Pipeline

The module includes a complete CI/CD pipeline that:

1. **Validates migrations** against test databases
2. **Tests Elasticsearch** index creation
3. **Builds Docker images** for deployment
4. **Runs on every** push to main/develop branches

### Deployment Flow

```mermaid
graph LR
    A[Code Push] --> B[Validate Migrations]
    B --> C[Build Migration Image]
    C --> D[Deploy to Staging]
    D --> E[Run Migrations]
    E --> F[Deploy Application Services]
    F --> G[Production Ready]
```

## 🛡️ Safety Features

- **Flyway Validation**: Ensures migration integrity
- **Rollback Support**: Database versioning for rollbacks
- **Health Checks**: Monitor migration status
- **Idempotent Operations**: Safe to run multiple times
- **Environment Isolation**: Separate configs per environment

## 📝 Adding New Migrations

### PostgreSQL Migration

1. Create new file: `V5__Your_migration_description.sql`
2. Add SQL DDL/DML statements
3. Test locally with validation mode
4. Deploy through CI/CD pipeline

### Elasticsearch Index Changes

1. Update mapping in `elasticsearch/` directory
2. Set `ELASTICSEARCH_RECREATE_INDICES=true` for breaking changes
3. Test index creation locally
4. Deploy with proper downtime planning

## 🚨 Troubleshooting

### Common Issues

1. **Migration Fails**: Check database connectivity and permissions
2. **Index Creation Fails**: Verify Elasticsearch cluster health
3. **Version Conflicts**: Ensure migration files are properly versioned
4. **Connection Timeouts**: Increase health check intervals

### Debug Commands

```bash
# Check database connection
pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME

# Check Elasticsearch health
curl $ELASTICSEARCH_URIS/_cluster/health

# Validate migrations only
java -jar database-migrations-1.0.0.jar --migration.postgresql.validate-only=true
```

## 🎯 Production Deployment Best Practices

1. **Run migrations first** before deploying application code
2. **Monitor migration status** during deployment
3. **Use validation mode** in staging environments
4. **Backup databases** before major schema changes
5. **Plan downtime** for breaking Elasticsearch changes
6. **Test rollback procedures** in staging

This migration module ensures your Flight Booking System database schema is always consistent and up-to-date across all environments! 🚀
