# Kokodi Test Project

## Описание

REST приложение для карточной игры на Kotlin + Spring Boot + Hibernate,
за безопасный доступ к ресурсам отвечают Spring Security + JWT.
Сделаны основные юнит-тесты для игровой механики и сквозные интеграционные тесты
для игровых эндпоинтов-сервисов-репозиториев-БД.

---

## Стек технологий

- Kotlin
- Spring Boot
- Spring Security
- JWT
- PostgreSQL
- Jackson
- Hibernate
- JUnit 5
- Mockito
- REST API на Spring Web

---

## Структура проекта

```bash
src/main/kotlin/com/vlasenko/kokodi_test/
├── config/            # Configurations
├── controllers/       # REST controllers for Authentification, Basic actions, Error handling
├── domain/            # Classes (Card, GameSession, User, Enums, Turn)
├── repository/        # Repositories
├── dto/               # DTOs
├── exceptions/        # Custom errors
├── services/          # Services
├── util/              # Custom utilities
└── KokodiTestApplication.kt
```
---

## Как запустить проект

1. Запустите контейнер с PostgreSQL:

```bash
./docker compose up
```

2. Соберите и запустите проект:

```bash
./gradlew bootRun
```

## Работа с API

Базовый URL: `http://localhost:8080/games`

| Метод | Endpoint                | Описание                                                                                                               |
|-------|-------------------------|------------------------------------------------------------------------------------------------------------------------|
| POST  | `/auth/sign-up`         | Зарегистрировать нового пользователя.                                                                                  |
| POST  | `/auth/sign-in`         | Залогиниться.                                                                                                          |
| GET   | `/games`                | Создать новую игру.                                                                                                    |
| POST  | `/games/{gameId}/join`  | Присоединиться к существующей игре по game ID, где gameId: положительный Long.                                         |
| GET   | `/games/{gameId}/start` | Начать существующую игру. Может вызвать только игрок зарегистрированный в этой партии, где gameId: положительный Long. |
| PUT   | `/games/{gameId}/turn`  | Сделать свой ход, для активного игрока, где gameId: положительный Long.                                                |


## Контакты

Разработчик: [Vladimir Vlasenko]

Telegram: [https://t.me/vladimvlas]

Email: [v.g.vlasenko@yandex.ru]