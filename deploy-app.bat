@echo off
REM Enable delayed variable expansion for better error handling
setlocal enabledelayedexpansion

echo [INFO] Starting Deployment Script...

REM Step 1: Clean and build the project using Gradle
echo [INFO] Starting Gradle build...
CALL .\gradlew clean bootJar -x test > gradle_build.log 2>&1
echo [DEBUG] Gradle exited with errorlevel %errorlevel%
if %errorlevel% neq 0 (
    echo [ERROR] Gradle build failed. Check gradle_build.log for details.
    pause
    exit /b %errorlevel%
)
echo [INFO] Gradle build completed successfully.

REM Step 2: Build the Docker image
echo [INFO] Building Docker image...
docker build -t pubsub-cloudrun-app . > docker_build.log 2>&1
echo [DEBUG] Docker build exited with errorlevel %errorlevel%
if %errorlevel% neq 0 (
    echo [ERROR] Docker build failed. Check docker_build.log for details.
    pause
    exit /b %errorlevel%
)
echo [INFO] Docker image built successfully.

REM Step 3: Tag the Docker image
echo [INFO] Tagging Docker image...
docker tag pubsub-cloudrun-app gcr.io/my-projectdemo-446217/pubsub-cloudrun-app > docker_tag.log 2>&1
echo [DEBUG] Docker tag exited with errorlevel %errorlevel%
if %errorlevel% neq 0 (
    echo [ERROR] Docker tag failed. Check docker_tag.log for details.
    pause
    exit /b %errorlevel%
)
echo [INFO] Docker image tagged successfully.

REM Step 4: Push the Docker image to Google Container Registry
echo [INFO] Pushing Docker image to GCR...
docker push gcr.io/my-projectdemo-446217/pubsub-cloudrun-app > docker_push.log 2>&1
echo [DEBUG] Docker push exited with errorlevel %errorlevel%
if %errorlevel% neq 0 (
    echo [ERROR] Docker push failed. Check docker_push.log for details.
    pause
    exit /b %errorlevel%
)
echo [INFO] Docker image pushed successfully.

REM Step 5: Deploy to Google Cloud Run
echo [INFO] Deploying to Google Cloud Run...
gcloud run deploy pubsub-cloudrun-app --image gcr.io/my-projectdemo-446217/pubsub-cloudrun-app --platform managed --region asia-south1 --allow-unauthenticated --memory 512Mi --timeout 600s --set-env-vars SPRING_CLOUD_GCP_PROJECT_ID=my-projectdemo-446217,PUBSUB_PULL_SUBSCRIPTION=Topic_MAO-sub_1,PUBSUB_PUSH_SUBSCRIPTION_PUSH=Topic_MAO-sub_1-push,PUBSUB_TARGET_TOPIC=Topic_MAO_Target > gcloud_deploy.log 2>&1
echo [DEBUG] GCloud deploy exited with errorlevel %errorlevel%
if %errorlevel% neq 0 (
    echo [ERROR] GCloud deployment failed. Check gcloud_deploy.log for details.
    pause
    exit /b %errorlevel%
)
echo [INFO] Deployment to Google Cloud Run completed successfully!

echo [SUCCESS] Deployment completed successfully!
pause
