#!/bin/bash

# Проверяем, что ID игры передано как аргумент
if [ -z "$1" ]; then
  echo "Usage: $0 {id}"
  exit 1
fi

# Загружаем переменные из .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
else
  echo ".env file not found!"
  exit 1
fi

# Проверяем, что токен установлен
if [ -z "$POLEMICA_TOKEN" ]; then
  echo "POLEMICA_TOKEN is not set in .env file"
  exit 1
fi

# Получаем ID игры из аргументов
GAME_ID=$1

# Устанавливаем URL и путь к файлу
URL="https://app.polemicagame.com/v1/matches/${GAME_ID}?version=4"
OUTPUT_DIR="src/test/resources/games"
OUTPUT_FILE="${OUTPUT_DIR}/${GAME_ID}.json"

# Создаем директорию, если она не существует
mkdir -p $OUTPUT_DIR

# Выполняем GET-запрос
response=$(curl -H "Authorization: Bearer $POLEMICA_TOKEN" -s -w "%{http_code}" -o $OUTPUT_FILE $URL)

# Проверяем код ответа
if [ $response -eq 200 ]; then
  echo "Request successful. Response saved to ${OUTPUT_FILE}"
else
  echo "Request failed with status code $response"
  rm -f $OUTPUT_FILE
fi
