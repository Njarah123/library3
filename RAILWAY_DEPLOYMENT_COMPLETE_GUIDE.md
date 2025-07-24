# Complete Railway Deployment Guide

## Issues Fixed

### 1. Docker Image Issue ✅
- **Problem**: Railway couldn't pull `openjdk:21-slim`
- **Solution**: Replaced with stable `eclipse-temurin:21-jre-alpine`

### 2. Application Startup Issue ✅
- **Problem**: App configured for MySQL, Railway uses PostgreSQL
- **Solution**: Created `application-prod.properties` with PostgreSQL configuration

### 3. Database Migration Issue ✅
- **Problem**: Flyway migrations contain MySQL-specific syntax
- **Solution**: Disabled Flyway initially, will use JPA auto-creation

## Deployment Steps

### Step 1: Set Up Database on Railway

1. **Add PostgreSQL Database**:
   - In Railway dashboard, click "New" → "Database" → "PostgreSQL"
   - Railway will automatically create database and provide connection details

2. **Get Database Connection Info**:
   - Go to your PostgreSQL service in Railway
   - Copy the connection details (Railway provides these as environment variables)

### Step 2: Configure Environment Variables

In your Railway project settings, ensure these environment variables are set:

**Automatically provided by Railway:**
- `DATABASE_URL` - Full PostgreSQL connection string
- `PGHOST` - Database host
- `PGPORT` - Database port (usually 5432)
- `PGDATABASE` - Database name
- `PGUSER` - Database username
- `PGPASSWORD` - Database password
- `PORT` - Application port (Railway sets this automatically)

**You may need to add:**
- `SPRING_PROFILES_ACTIVE=prod` (if not already set)

### Step 3: Deploy Application

1. **Commit your changes**:
   ```bash
   git add .
   git commit -m "Fix Railway deployment - PostgreSQL support and stable Docker image"
   git push
   ```

2. **Deploy on Railway**:
   - Railway will automatically detect the push and start building
   - The build should now succeed with Eclipse Temurin
   - The app should start and connect to PostgreSQL

### Step 4: Verify Deployment

1. **Check Build Logs**: Ensure Docker build completes successfully
2. **Check Deploy Logs**: Verify application starts without database connection errors
3. **Test Health Check**: Railway will check the root path `/` for health
4. **Access Application**: Use the Railway-provided URL to access your app

## Configuration Details

### Database Configuration
- **Development**: MySQL (localhost)
- **Production**: PostgreSQL (Railway-provided)
- **Migration**: JPA auto-creation (`spring.jpa.hibernate.ddl-auto=update`)

### Key Files Modified
- [`Dockerfile`](Dockerfile) - Uses Eclipse Temurin, optimized for Railway
- [`railway.json`](railway.json) - Proper configuration for Railway deployment
- [`application-prod.properties`](src/main/resources/application-prod.properties) - PostgreSQL configuration
- [`.railwayignore`](.railwayignore) - Optimizes build by excluding unnecessary files

## Troubleshooting

### If Application Still Fails to Start:

1. **Check Railway Logs**:
   - Go to Railway dashboard → Your service → Logs
   - Look for specific error messages

2. **Common Issues**:
   - **Database Connection**: Verify `DATABASE_URL` is set correctly
   - **Port Binding**: Ensure app uses `$PORT` environment variable
   - **Memory Issues**: Railway provides limited memory, app is configured for 512MB max

3. **Database Issues**:
   - If you need the original data, you'll need to migrate from MySQL to PostgreSQL
   - For fresh start, JPA will create tables automatically

### If You Need MySQL-Specific Features:

1. **Option 1**: Use Railway's MySQL addon (if available)
2. **Option 2**: Convert Flyway migrations to PostgreSQL syntax
3. **Option 3**: Use external MySQL service (like PlanetScale)

## Next Steps After Successful Deployment

1. **Enable Flyway** (optional):
   - Create PostgreSQL-compatible migration files
   - Set `spring.flyway.enabled=true` in production config

2. **Set up Production Data**:
   - Import any necessary seed data
   - Configure admin users

3. **Configure Domain** (optional):
   - Set up custom domain in Railway dashboard
   - Configure SSL (Railway provides this automatically)

## File Upload Configuration

The app is configured to handle file uploads in production:
- Upload directory: `/app/uploads`
- Max file size: 10MB (reduced from development 50MB)
- Files will be stored in the container (consider using external storage for production)

## Security Notes

- Diagnostic mode is disabled in production
- Logging levels reduced for performance
- Robust security configuration enabled
- Non-root user in Docker container