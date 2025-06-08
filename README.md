# Alkify Music API 🎵

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-green.svg)](https://spring.io/projects/spring-boot)

Серверная часть музыкальной платформы Alkify с REST API для управления артистами, альбомами и треками. Полностью самописное решение с интеграцией с [React-фронтендом](https://github.com/alkmanistik/alkify-music-react).

> 🌐 **Фронтенд часть**: [alkify-music-react](https://github.com/alkmanistik/alkify-music-react)  
> Реализована на React 19 + TypeScript с адаптивным интерфейсом

## 📌 Основные возможности

- 🎤 Управление артистами (CRUD + загрузка изображений)
- 💿 Полноценная работа с альбомами и треками
- 🔐 JWT аутентификация (интеграция с фронтендом)
- ⚡ Кеширование с Redis для высокой производительности
- 🎨 Загрузка медиа (аудио/изображения) с облачным хранилищем
- 🔄 Полная синхронизация с [фронтенд-приложением](https://github.com/alkmanistik/alkify-music-react)

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
