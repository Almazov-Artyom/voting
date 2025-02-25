# Используем официальный образ OpenJDK для Java
FROM openjdk:23-jdk-slim

# Указываем рабочую директорию в контейнере
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY target/netty-1.0-SNAPSHOT.jar app.jar

# Открываем нужный порт (например, порт 8080)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]