# Агрегатор данных из REST API

Курсовой проект по дисциплине «Объектно-ориентированное программирование».  
Консольное однопоточное Java-приложение для сбора котировок криптовалют из трёх открытых REST API, приведения ответов к единой модели данных, сохранения результатов в файлы форматов JSON и CSV и последующего чтения с возможностью фильтрации.

## Стек

- Java 21
- Apache Maven 3.8+
- `java.net.http.HttpClient` — выполнение HTTP-запросов
- Jackson (`jackson-databind`, `jackson-datatype-jsr310`) — десериализация ответов API и работа с JSON
- Apache Commons CSV — запись и чтение CSV-файлов

## Выбранные REST API

### 1. CoinGecko (`coingecko`)
- **Эндпоинт:** `https://api.coingecko.com/api/v3/coins/markets`
- **Пример запроса:** `https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=bitcoin&sparkline=true`
- **Query-параметры:**
    - `vs_currency` — базовая валюта котировки (по умолчанию `usd`);
    - `ids` — идентификатор криптовалюты (по умолчанию `bitcoin`);
    - `sparkline=true` — включает вложенный объект `sparkline_in_7d` с массивом цен `price`.
- **Структура ответа:** JSON-массив объектов, содержащий поля `id`, `symbol`, `name`, `current_price`, `market_cap`, `price_change_percentage_24h` и вложенный объект `sparkline_in_7d`.

### 2. KuCoin (`kucoin`)
- **Эндпоинт:** `https://api.kucoin.com/api/v1/market/stats`
- **Пример запроса:** `https://api.kucoin.com/api/v1/market/stats?symbol=BTC-USDT`
- **Query-параметры:**
    - `symbol` — торговая пара (по умолчанию `BTC-USDT`).
- **Структура ответа:** JSON-объект с полем `code` и вложенным объектом `data`, содержащим поля `time`, `symbol`, `buy`, `sell`, `changeRate`, `high`, `low`, `last` и др.

### 3. CoinPaprika (`coinpaprika`)
- **Эндпоинт:** `https://api.coinpaprika.com/v1/tickers/{coin_id}`
- **Пример запроса:** `https://api.coinpaprika.com/v1/tickers/btc-bitcoin?quotes=USD`
- **Параметры:**
    - `coin` — идентификатор монеты в формате CoinPaprika (по умолчанию `btc-bitcoin`);
    - `quotes` — query-параметр валюты котировки (по умолчанию `USD`).
- **Структура ответа:** JSON-объект с полями `id`, `name`, `symbol`, `rank`, `last_updated` и вложенным объектом `quotes`, внутри которого по ключу валюты лежат поля `price`, `volume_24h`, `percent_change_24h`.

## Единая модель данных

Ответы всех источников маппятся в record `AggregatedCryptoData`:

| Поле | Тип | Описание |
| :--- | :--- | :--- |
| `sourceName` | `String` | Идентификатор источника (`coingecko`, `kucoin`, `coinpaprika`) |
| `symbol` | `String` | Тикер криптовалюты (например, `BTC`, `ETH`) |
| `priceUsd` | `double` | Текущая цена актива |
| `change24h` | `double` | Изменение цены за последние 24 часа (%) |
| `timestamp` | `Instant` | Время получения данных (UTC, формат ISO-8601) |

## Архитектура проекта

```text
src/main/java/aggregator/
├── Main.java                      # Точка входа и сборка зависимостей
├── cli/                           # Работа с аргументами командной строки и вывод в консоль
│   ├── CliArgs.java
│   └── CliRunner.java
├── client/                        # HTTP-клиент (интерфейс и реализация на java.net.http)
│   ├── HttpClientService.java
│   └── JavaHttpClient.java
├── model/                         # Единая модель, перечисления и DTO под ответы API
│   ├── AggregatedCryptoData.java
│   ├── OutputFormat.java
│   ├── WriteMode.java
│   └── dto/
│       ├── CoinGeckoDto.java
│       ├── CoinPaprikaDto.java
│       └── KuCoinDto.java
├── service/                       # Бизнес-логика агрегации и реестр источников
│   └── AggregatorService.java
├── source/                        # Общий интерфейс источника и его реализации
│   ├── CryptoSource.java
│   ├── CoinGeckoSource.java
│   ├── CoinPaprikaSource.java
│   └── KuCoinSource.java
└── storage/                       # Интерфейс хранилища и реализации для JSON и CSV
    ├── DataStorage.java
    ├── CsvDataStorage.java
    └── JsonDataStorage.java
```

## Сборка и запуск

### Сборка проекта

```bash
mvn clean compile
```

### Параметры командной строки

| Параметр | Обязательность | Описание и допустимые значения |
| :--- | :--- | :--- |
| `--action` | да | Режим работы: `poll` (опрос API и сохранение) или `read` (чтение из файла) |
| `--sources` | для `poll` | Список источников через запятую: `coingecko`, `kucoin`, `coinpaprika` |
| `--format` | да | Формат файла: `json` (`aggregated_data.json`) или `csv` (`aggregated_data.csv`) |
| `--mode` | для `poll` | Режим записи: `create` (создать/перезаписать файл) или `append` (дописать) |
| `--source` | опционально | Фильтр по имени источника при `--action=read` |
| `--ids`, `--vs_currency`, `--symbol`, `--coin`, `--quotes` | опционально | Дополнительные параметры для запросов к выбранным API |

### Примеры команд запуска

Опрос всех трёх источников с параметрами по умолчанию (BTC) и создание нового JSON-файла:

```bash
mvn exec:java -Dexec.mainClass="aggregator.Main" -Dexec.args="--action=poll --sources=coingecko,kucoin,coinpaprika --format=json --mode=create"
```

Опрос части источников с передачей query-параметров (ETH) и дозаписью в CSV:

```bash
mvn exec:java -Dexec.mainClass="aggregator.Main" -Dexec.args="--action=poll --sources=kucoin,coinpaprika --format=csv --mode=append --symbol=ETH-USDT --coin=eth-ethereum"
```

Вывод всего содержимого JSON-файла в консоль:

```bash
mvn exec:java -Dexec.mainClass="aggregator.Main" -Dexec.args="--action=read --format=json"
```

Вывод записей из CSV-файла только по источнику `kucoin`:

```bash
mvn exec:java -Dexec.mainClass="aggregator.Main" -Dexec.args="--action=read --format=csv --source=kucoin"
```

Запуск без аргументов выводит краткую справку по доступным флагам.

## Особенности работы

- При сохранении в JSON в режиме `append` существующий массив предварительно считывается и объединяется с новыми записями, чтобы сохранить валидность структуры файла.
- При сохранении в CSV в режиме `append` заголовок колонок не дублируется, если файл уже существует и не пуст.
- У публичного API CoinGecko действует ограничение по частоте запросов (rate limit); при слишком частых повторных запусках сервер может вернуть код `429`.