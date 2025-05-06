#!/bin/bash

BASE_URL=http://localhost:8080
MAX_TURNS=10
NUM_PLAYERS=2
TURN_COUNT=0
TOKENS=()
USERS=()
GAME_ID=""

# === Формирование массива USERS ===
for ((i=1; i<=NUM_PLAYERS; i++)); do
  USERS+=("user$i")
done

register_users() {
  echo "🧾 Registering users..."
  for USER in "${USERS[@]}"; do
    RESPONSE=$(curl -s -X POST $BASE_URL/auth/sign-up \
      -H "Content-Type: application/json" \
      -d "{\"username\": \"$USER\", \"password\": \"password\", \"name\": \"$USER\"}")
    echo "👤 $USER → $RESPONSE"
  done
}

login_users() {
  echo "🔑 Logging in users..."
  for USER in "${USERS[@]}"; do
    TOKEN=$(curl -s -X POST $BASE_URL/auth/sign-in \
      -H "Content-Type: application/json" \
      -d "{\"username\": \"$USER\", \"password\": \"password\"}" | grep -o '"token":"[^"]*"' | cut -d':' -f2 | tr -d '"')
    TOKENS+=("$TOKEN")
    echo "🔐 $USER token: $TOKEN"
  done
}

create_game() {
  echo "🎮 Creating game..."
  RESPONSE=$(curl -s -X POST $BASE_URL/games \
    -H "Authorization: Bearer ${TOKENS[0]}")
  echo "📦 Create game response: $RESPONSE"
  GAME_ID=$RESPONSE
}

join_game() {
  echo "🙋‍♂️ Joining game..."
  for INDEX in "${!USERS[@]}"; do
    USER=${USERS[$INDEX]}
    TOKEN=${TOKENS[$INDEX]}
    RESPONSE=$(curl -s -X POST $BASE_URL/games/$GAME_ID/join \
      -H "Authorization: Bearer $TOKEN")
    echo "🤝 $USER joined → $RESPONSE"
  done
}

start_game() {
  echo "🚀 Starting game..."
  RESPONSE=$(curl -s -X POST $BASE_URL/games/$GAME_ID/start \
    -H "Authorization: Bearer ${TOKENS[0]}")
  echo "🎬 Start game response: $RESPONSE"
}

simulate_turns() {
  echo "🎲 Simulating alternating turns (limit: $MAX_TURNS)..."

  while [[ "$TURN_COUNT" -lt "$MAX_TURNS" ]]; do
    INDEX=$(( TURN_COUNT % ${#USERS[@]} ))
    USER=${USERS[$INDEX]}
    TOKEN=${TOKENS[$INDEX]}

    echo "🎯 $USER attempts a turn..."

    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$BASE_URL/games/$GAME_ID/turn" \
      -H "Authorization: Bearer $TOKEN")

    BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')
    STATUS=$(echo "$RESPONSE" | sed -n 's/.*HTTPSTATUS://p')

    if [[ "$STATUS" == "200" ]]; then
      echo "✅ Turn accepted ($STATUS): $BODY"
    elif [[ "$STATUS" == "409" ]]; then
      echo "⚠️  Turn rejected ($STATUS): $BODY"
      if [[ "$BODY" == *"Game is finished."* ]]; then
        echo "🏁 Game is finished. Exiting..."
        break
      fi
    else
      echo "❌ Error ($STATUS): $BODY"
      exit 1
    fi

    ((TURN_COUNT++))
    sleep 0.5
  done

  echo "🛑 Simulation ended after $TURN_COUNT turns."
}

register_users
login_users
create_game
join_game
start_game
simulate_turns
