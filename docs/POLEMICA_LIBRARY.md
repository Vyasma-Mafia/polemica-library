# Библиотека Polemica

Kotlin-библиотека для работы с API сервиса [Polemica](https://polemicagame.com/): авторизация, загрузка игр и турниров, модели партии и вспомогательные функции для разбора протокола.

**Зависимости:** Spring WebFlux (`WebClient`), Jackson (Kotlin module), Apache Commons Text (для `GamePointsService`).

**JVM:** 17+.

---

## Подключение

Опубликуемый артефакт (см. `build.gradle`): координаты Maven — `io.github.mralex1810:polemica-library:<version>`.

В Gradle:

```kotlin
dependencies {
    implementation("io.github.mralex1810:polemica-library:1.7.8")
}
```

---

## Клиент API: `PolemicaClient` / `PolemicaClientImpl`

Реализация `PolemicaClientImpl` принимает:

| Параметр | Назначение |
|----------|------------|
| `polemicaBaseUrl` | Базовый URL API (без завершающего `/`) |
| `profileSiteBaseUrl` | Базовый URL публичного сайта для профильных игр (`https://polemicagame.com` по умолчанию) |
| `polemicaUsername`, `polemicaPassword` | Учётные данные для `POST /v1/auth/login` |
| `objectMapper` | Общий `ObjectMapper` для сериализации/десериализации (рекомендуется `ObjectMapper().registerKotlinModule()`) |

### Поведение авторизации

- Ко всем запросам, кроме `/v1/auth/login`, добавляется заголовок `Authorization: Bearer <token>`.
- Токен берётся из ответа логина (`access_token`).
- При ответе **401** клиент обновляет токен и повторяет запрос.

### Методы интерфейса

| Метод | HTTP | Описание |
|-------|------|----------|
| `getGameFromClub(PolemicaClubGameId)` | `GET /v1/clubs/{clubId}/games/{gameId}` | Полная модель игры клуба. Опционально `?version=` |
| `getMatch(PolemicaMatchId)` | `GET /v1/matches/{matchId}` | Полная модель матча. Опционально `?version=` |
| `getGamesFromClub(clubId, offset, limit)` | `GET /v1/clubs/{clubId}/games` | Список ссылок на игры клуба |
| `getProfileGames(userId, page, limit)` | `GET https://polemicagame.com/profile/default/get-games` | Публичные игры пользователя с пагинацией (без Bearer-токена) |
| `getCompetitions()` | `GET /v1/competitions` | Список соревнований |
| `getCompetition(id)` | `GET /v1/competitions/{id}` | Одно соревнование |
| `getGamesFromCompetition(id)` | `GET /v1/competitions/{id}/games` | Игры турнира |
| `getGameFromCompetition(PolemicaCompetitionGameId)` | `GET /v1/competitions/{id}/games/{gameId}` | Полная модель игры турнира, опционально `version` |
| `getCompetitionMembers(id)` | `GET /v1/competitions/{id}/members` | Участники |
| `getCompetitionAdmins(id)` | `GET /v1/competitions/{id}/admins` | Администраторы |
| `getCompetitionResultMetrics(id, scoringType)` | `GET /v1/competitions/{id}/metrics?scoringType=` | Метрики по игрокам турнира |
| `postGameToCompetition(competitionId, game)` | `POST /v1/competitions/{id}/games` | Отправка модели `PolemicaGame` на сервер |

Идентификаторы игр задаются классами-обёртками:

- `PolemicaClubGameId(clubId, gameId, version?)`
- `PolemicaMatchId(matchId, version?)`
- `PolemicaCompetitionGameId(competitionId, gameId, version?)`

Дополнительные DTO интерфейса (ответы API): `PolemicaCompetition`, `PolemicaClubGameReference`, `PolemicaTournamentGameReference`, `PolemicaCompetitionMember`, `PolemicaCompetitionAdmin`, `CompetitionPlayerResult` (внутри — `ResultMetrics` и `Metric` по ролям don/maf/com/civ).

Для метода `getProfileGames` добавлены DTO: `ProfileGamesPage` (поля `rows`, `totalCount`), `ProfileGameRow`, `ProfileGameMode`, `ProfileGameRole`, `ProfileGameResult`.

У `webClient` в `PolemicaClientImpl` модификатор `public` — при необходимости можно вызывать произвольные эндпоинты тем же клиентом.

---

## Парсинг очков с публичной страницы: `GamePointsService`

Класс `GamePointsService` (`client/GamePointsClient.kt`) не использует JSON API Polemica: он запрашивает HTML страницу `https://polemicagame.com/match/{id}` через `RestTemplate`, извлекает JSON из атрибута `:game-data='...'`, парсит очки игроков.

**Конструктор:** `GamePointsService(restTemplate: RestTemplate)`.

**Метод:** `fetchPlayerStats(id: Long): List<PlayerPoints>` — позиция за столом и очки (`PlayerPoints` из пакета `model`).

Используется для сценариев, когда нужны только очки с публичной страницы, без полного API-клиента.

---

## Модели

### Игра: `PolemicaGame`

Центральная модель партии (`model.game`):

| Поле | Тип | Смысл |
|------|-----|--------|
| `id` | `Long?` | Идентификатор (может отсутствовать при создании) |
| `name` | `String?` | Название матча (например, стол и фаза турнира), приходит с API в `GET /v1/matches/{id}` |
| `master`, `referee` | `Long`, `PolemicaUser` | Ведущий и судья |
| `scoringVersion` | `String?` | Версия скоринга |
| `scoringType` | `Int` | Тип скоринга |
| `version` | `Int` | Версия записи игры |
| `zeroVoting` | `ZeroVoting?` | Правило нулевого круга |
| `tags` | `List<String>?` | Теги |
| `players` | `List<PolemicaPlayer>?` | Игроки |
| `checks`, `shots`, `votes` | списки | Проверки, выстрелы, голосования |
| `stage`, `stop` | `Stage?` | Текущий и завершающий этапы |
| `comKiller` | `Position?` | Комиссар-убийца (позиция) |
| `bonuses` | `List<PolemicaBonus>?` | Бонусы |
| `started` | `LocalDateTime` | Начало |
| `isLive` | `Boolean?` | Живая игра |
| `result` | `PolemicaGameResult?` | Исход |
| `num`, `table`, `phase` | `Int?` | Номер игры, стол, фаза турнира |
| `factor` | `Double?` | Коэффициент |

### Игрок и пользователь

- **`PolemicaPlayer`** — позиция, ник, `Role`, тех. фолы и фолы (`List<Foul>`), угадайка `PolemicaGuess?`, поле `player` (`PolemicaUser?`, десериализуется и из числа id, и из объекта), дисквалификация `disqual`, награда `award`.
- **`PolemicaUser`** — `id`, `username`.

### Роли и результат

- **`Role`** (число в JSON): `DON`, `MAFIA`, `PEACE`, `SHERIFF`.
- **`PolemicaGameResult`**: `RED_WIN`, `BLACK_WIN`.

### Позиции и этапы

- **`Position`**: места `ONE` … `TEN` (значения 1–10), есть `fromInt`.
- **`Stage`** — `StageType`, день, опционально игрок и голосование; сравнивается по дню/игроку/типу.
- **`StageType`**: dealing, briefing, speech, voting, shooting, проверки дона/кома, guess, comKill, gameOver и др. (строковые значения API).

### События партии

- **`PolemicaCheck`** — ночь, роль проверки, проверяемая позиция.
- **`PolemicaShot`** — ночь, стрелок, жертва.
- **`PolemicaVote`** — день, номер раунда (`num`), голосующий, кандидат.
- **`PolemicaBonus`** — позиция, текст, очки, базовый бонус.
- **`PolemicaGuess`** — списки мирных/мафии, вице-мэр.
- **`Foul`** — время и этап нарушения.

### Настройки и очки с сайта

- **`ZeroVoting`**: `RESPEECH`, `LIFT_ONLY`, `NONE` (строки API).
- **`PlayerPoints`**, **`PolemicaGamePlayersPoints`** (`model/Points.kt`) — позиция и очки для сценария с публичной страницей.

---

## Утилиты

### `GameUtils` (`utils/GameUtils.kt`)

Расширения для `PolemicaGame` и связанные типы:

| Имя | Назначение |
|-----|------------|
| `getRealComKiller()` | «Реальный» комиссар-убийца с учётом первой ночной жертвы и дона |
| `getFirstKilled()`, `getKilled(beforeGamePhase?)` | Жертвы ночей (с учётом фазы игры) |
| `getDon()`, `getSheriff()`, `getRole(Position)` | Быстрый доступ к ролям |
| `getFinalVotes(beforeGamePhase?)` | Итоги голосований по дням (`FinalVote`) |
| `getVotingParticipants(day, round)` | Участники голосования по дню и раунду |
| `playersOnTable`, `playersWithRoles`, `getBlacksOnTable`, `getKickedFromTable` | Кто за столом, кто в чёрных, кто покинул стол (убийство, голосование, дисквалификация) |
| `isRedWin()`, `isBlackWin()` | Исход |
| `getCriticDay()` | День «критики» по модели красных/чёрных |
| `getPlayerNumStarted(day)`, `getVoteCandidatesOrder(day)` | Порядок начала речи и выставления |
| `check { }` / `InPolemicaGameContext` | DSL для проверок: `Position.role()`, `guess()`, `player()`, `assert` — при нарушении `assert` возвращается 0 из блока |

Вспомогательные типы: `KilledPlayer`, `FinalVote`, `GamePhase`, `Phase` (DAY/NIGHT), `KickedPlayer`, `KickReason`.

`ZeroVoting.withBreak()` — для нулевого круга без «разрыва» в смысле речи (respeech/lift/none).

### `RoleUtils` (`utils/RoleUtils.kt`)

- `Role.isRed()` / `Role.isBlack()` — мирный шериф и мирные vs дон и мафия.

### `RatingUtils` (`utils/RatingUtils.kt`)

Функции для **`PolemicaClient.CompetitionPlayerResult`**:

- `compare(a, b)` — сравнение мест: сумма очков, сумма наград, победы ком+дон, первые ночные убийства, сумма guess-очков (с округлением до 4 знаков где указано).
- `sumScore()`, `sumAward()`, `winAsDonOrSher()`, `firstNightKills()`, `sumQuessScore()` — агрегаты по метрикам четырёх ролей.

### `MetricsUtils` (`utils/MetricsUtils.kt`)

- `MetricsUtils.getRating(List<CompetitionPlayerResult>): List<PolemicaUser>` — сортировка по сумме `totalScores` по всем ролям и проекция в список пользователей.

### Сериализация enum в JSON (`utils/enums`)

- **`IntEnum`**, **`IntEnumSerializer`**, **`IntEnumDeserializer`** — enum с числовым `value` (роли, позиции, результаты игры и т.д.).
- **`StringEnum`**, **`StringEnumSerializer`**, **`StringEnumDeserializer`** — enum со строковым `value` (`StageType`, `ZeroVoting`).

При добавлении своих enum для Jackson можно следовать тому же паттерну.

---

## Краткая схема пакетов

| Пакет / область | Содержимое |
|-----------------|------------|
| `...client` | `PolemicaClient`, `PolemicaClientImpl`, `GamePointsService`, DTO ответов API |
| `...model.game` | `PolemicaGame` и все сущности партии |
| `...model` | `PlayerPoints`, `PolemicaGamePlayersPoints` |
| `...utils` | `GameUtils`, `RoleUtils`, `RatingUtils`, `MetricsUtils` |
| `...utils.enums` | Общие сериализаторы enum для Jackson |
