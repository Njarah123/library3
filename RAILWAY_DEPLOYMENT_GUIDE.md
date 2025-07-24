# Railway Deployment Fix Guide

## Problem Fixed
The original deployment failed because Railway couldn't pull the `openjdk:21-slim` Docker image. This has been resolved by:

1. **Switched to Eclipse Temurin**: Using `eclipse-temurin:21-jre-alpine` which is more stable on Railway
2. **Multi-stage build**: Optimized Docker build process
3. **Updated main Dockerfile**: Replaced problematic `openjdk:21-slim` with stable Eclipse Temurin
4. **Removed conflicts**: Deleted `railway.toml` to avoid configuration conflicts
5. **Added optimizations**: Created `.railwayignore` for faster builds

## Changes Made

### 1. Updated `railway.json`
- Uses main `Dockerfile` (now fixed)
- Updated start command to match new jar naming: `app.jar`
- Kept the same health check configuration

### 2. Fixed main `Dockerfile`
- Multi-stage build for smaller final image
- Uses Eclipse Temurin (more reliable than OpenJDK on Railway)
- Added security with non-root user
- Optimized memory settings with `JAVA_OPTS`
- Added health check
- Dynamic port binding with `$PORT` environment variable

### 3. Created `.railwayignore`
- Excludes unnecessary files from deployment
- Reduces build time and image size

## Deployment Steps

1. **Commit your changes**:
   ```bash
   git add .
   git commit -m "Fix Railway deployment with stable Docker image"
   git push
   ```

2. **Deploy to Railway**:
   - Go to your Railway dashboard
   - Trigger a new deployment
   - The build should now succeed using Eclipse Temurin

3. **Monitor the deployment**:
   - Check build logs for any issues
   - Verify the application starts successfully
   - Test the health check endpoint: `/actuator/health`

## Environment Variables Needed

Make sure these environment variables are set in Railway:

- `SPRING_PROFILES_ACTIVE=prod`
- `PORT` (automatically set by Railway)
- Database connection variables (if using external DB)

## Troubleshooting

If you still encounter issues:

1. **Check Railway logs** for specific error messages
2. **Verify environment variables** are properly set
3. **Ensure database connectivity** if using external database
4. **Check memory limits** - the app is configured for 512MB max heap

## Testing Locally

To test the Docker build locally:

```bash
# Build the image
docker build -f Dockerfile.alternative -t library-app .

# Run the container
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod library-app
```

The application should be accessible at `http://localhost:8080`