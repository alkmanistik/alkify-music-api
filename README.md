# Alkify Music API 🎵

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-green.svg)](https://spring.io/projects/spring-boot)

Серверная часть музыкальной платформы с REST API для управления артистами, альбомами и треками. Поддержка аутентификации и кеширования.

## 📌 Основные возможности

- 🎤 Управление артистами (CRUD + изображения)
- 💿 Работа с альбомами и треками
- 🔐 JWT аутентификация
- ⚡ Кеширование с Redis
- 🎨 Загрузка медиа (аудио/изображения)

## 🚀 Технологический стек

- **Backend**: 
  - Java 21
  - Spring Boot 3.5.0
  - Spring Data JPA
  - Spring Security
- **Базы данных**:
  - PostgreSQL (основная)
  - Redis (кеширование)
- **Деплой**:
  - Docker
  - Docker Compose

## 📦 Установка и запуск

### Требования
- Java 17+
- Docker и Docker Compose
- Maven

### Файл переменных окружения (.env)
```
DB_URL=
DB_PASSWORD=
DB_USERNAME=
DB_NAME=
JWT_TOKEN=
```
