# Decision Log

This file records architectural and implementation decisions using a list format.
2025-10-20 19:30:39 - Log of updates made.

*

## Decision

*

## Rationale 

*

## Implementation Details

*

[2025-10-20 19:33:20] - Выбор MockWebServer для тестирования HTTP-клиента

## Decision

Использовать OkHttp MockWebServer для тестирования механизма обновления токена в PolemicaClientImpl

## Rationale 

- Легковесная библиотека, специально созданная для тестирования HTTP-клиентов
- Отлично работает с Spring WebFlux и реактивными потоками
- Позволяет точно контролировать HTTP-ответы (включая статус-коды 401)
- Не требует изменений в production коде
- Поддерживает сложные сценарии с последовательными запросами

## Implementation Details

- Добавить зависимость: `testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'`
- Создать тестовый класс `PolemicaClientImplTest`
- Использовать MockWebServer для имитации Полемика API
- Тестировать различные сценарии: успешное обновление, ошибки, concurrency
