FROM amazoncorretto:17-alpine As build1

WORKDIR /app

COPY . .

Run chmod +x ./gradlew

Run ./gradlew clean build -x test

from amazoncorretto:17-alpine

ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=build1 /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
