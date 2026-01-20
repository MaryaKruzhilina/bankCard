# Система управления банковскими картами

Приложение Bank Rest для управления банковскими картами.

## Технологический стек:
* Java 17
* Spring Boot 4.0.1
* Spring Security
* JWT
* Spring Data JPA
* PostgreSQL
* Liquibase
* Swagger (OpenAPI)
* Docker / Docker Compose
* JUnit + Mockito


## Безопасность:
* Spring Security, JWT токен
* Разделение доступа по ролям: ADMIN, USER
* В проекте используются предустановленные пользователи для тестирования выдачи JWT.
* Вход осуществлять по адресу
* Для роли: ADMIN, USER:
```
{
  "username": "admin",
  "password": "admin123"
}
```
* роли: USER:
```
{
  "username": "user",
  "password": "user123"
}
```

## Возможности управления картами пользователей:
### ADMIN
* Создаёт, блокирует, активирует, удаляет карты
* Видит все карты

### USER
* Просматривает свои карты (поиск + пагинация)
* Запрашивает блокировку карты
* Делает переводы между своими картами
* Смотрит баланс

## Для запуска нужно:
1. Клонировать репозиторий:
```
git clone https://github.com/AndreySabitov/Bank_REST.git
cd Bank_REST
```
2.Cоздать переменные окружения
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bank_card_db
DB_USER=root
DB_PASSWORD=root
JWT_SECRET=isPxGPpM9eGfCqJd2uM6jL+cuXOqe96uaPU0ZU8w2v4=
```
3. Собрать проект:
```
mvn clean package
```
4. Запустить Docker и выполнить:
```
docker compose up -d --build
```

### API:
```
http://localhost:8080/swagger-ui/index.html
```
```
http://localhost:8080/v3/api-docs
```
Файл docs/openapi.yaml генерируется командой
```
 curl http://localhost:8080/v3/api-docs.yaml -o docs/openapi.yaml
```