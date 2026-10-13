#!/usr/bin/env bash
# =============================================================================
# Pruebas de extremo a extremo del taller (requiere el stack levantado con
# `docker compose up -d --build`). Usa curl y jq.
#
#   ./scripts/pruebas-e2e.sh              # todas las pruebas
#   SIN_CLIENTE=1 ./scripts/pruebas-e2e.sh  # omite la invocación remota del EJB
#
# Variables: API (por defecto http://localhost:8080/api).
# =============================================================================
set -uo pipefail

API="${API:-http://localhost:8080/api}"
RUTA="/resource-requests"
EXITOS=0
FALLOS=0

verde() { printf '\033[32m%s\033[0m\n' "$*"; }
rojo() { printf '\033[31m%s\033[0m\n' "$*"; }
titulo() { printf '\n\033[1m== %s ==\033[0m\n' "$*"; }

comprobar() {
    # comprobar "descripción" valor_obtenido valor_esperado
    if [[ "$2" == "$3" ]]; then
        verde "  ✔ $1"
        EXITOS=$((EXITOS + 1))
    else
        rojo "  ✘ $1 (obtenido: '$2', esperado: '$3')"
        FALLOS=$((FALLOS + 1))
    fi
}

contiene() {
    if [[ "$2" == *"$3"* ]]; then
        verde "  ✔ $1"
        EXITOS=$((EXITOS + 1))
    else
        rojo "  ✘ $1 ('$2' no contiene '$3')"
        FALLOS=$((FALLOS + 1))
    fi
}

# Solicitud HTTP: imprime "<cuerpo>\n<código>"
http() {
    local metodo="$1" ruta="$2" cuerpo="${3:-}"
    if [[ -n "$cuerpo" ]]; then
        curl -s -w '\n%{http_code}' -X "$metodo" "$API$ruta" -H 'Content-Type: application/json' -d "$cuerpo"
    else
        curl -s -w '\n%{http_code}' -X "$metodo" "$API$ruta"
    fi
}
codigo() { tail -n1 <<<"$1"; }
cuerpo() { sed '$d' <<<"$1"; }

solicitud_valida() {
    cat <<JSON
{"disasterDescription":"Inundación en la ribera del río","resourceType":"Agua potable",
 "quantity":250,"priority":"ALTA"}
JSON
}

titulo "Disponibilidad del módulo"
R=$(http GET "$RUTA")
comprobar "GET $RUTA responde 200" "$(codigo "$R")" "200"
comprobar "la respuesta es un arreglo JSON" "$(cuerpo "$R" | jq -r 'type')" "array"

titulo "RF-1 · Registrar una solicitud"
R=$(http POST "$RUTA" "$(solicitud_valida)")
comprobar "POST $RUTA responde 201" "$(codigo "$R")" "201"
ID=$(cuerpo "$R" | jq -r '.id')
comprobar "la solicitud nace en estado PENDIENTE" "$(cuerpo "$R" | jq -r '.status')" "PENDIENTE"
LOCATION=$(curl -s -D- -o /dev/null -X POST "$API$RUTA" -H 'Content-Type: application/json' \
    -d "$(solicitud_valida)" | tr -d '\r' | awk '/^[Ll]ocation:/ {print $2}')
contiene "la respuesta incluye la cabecera Location" "$LOCATION" "$RUTA/"

titulo "RF-2 · Consultar solicitudes"
R=$(http GET "$RUTA/$ID")
comprobar "GET $RUTA/{id} responde 200" "$(codigo "$R")" "200"
comprobar "devuelve el tipo de recurso registrado" "$(cuerpo "$R" | jq -r '.resourceType')" "Agua potable"
R=$(http GET "$RUTA")
comprobar "la solicitud aparece en el listado" \
    "$(cuerpo "$R" | jq --argjson id "$ID" 'map(.id) | index($id) != null')" "true"

titulo "RF-4 · Consultar pendientes"
R=$(http GET "$RUTA/pending")
comprobar "la solicitud recién creada está pendiente" \
    "$(cuerpo "$R" | jq --argjson id "$ID" 'map(.id) | index($id) != null')" "true"
comprobar "el listado de pendientes solo trae estado PENDIENTE" \
    "$(cuerpo "$R" | jq -r 'map(.status) | unique | join(",")')" "PENDIENTE"

titulo "Reglas de negocio · validaciones al registrar"
R=$(http POST "$RUTA" '{"disasterDescription":"Incendio","resourceType":"Extintores","quantity":0,"priority":"ALTA"}')
comprobar "cantidad cero se rechaza con 400" "$(codigo "$R")" "400"
contiene "el mensaje explica la regla de la cantidad" "$(cuerpo "$R" | jq -r '.mensaje')" "mayor que cero"
R=$(http POST "$RUTA" '{"disasterDescription":"Incendio","resourceType":"Extintores","quantity":-5,"priority":"ALTA"}')
comprobar "cantidad negativa se rechaza con 400" "$(codigo "$R")" "400"
R=$(http POST "$RUTA" '{"resourceType":"Extintores","quantity":10,"priority":"ALTA"}')
comprobar "sin descripción del desastre se rechaza con 400" "$(codigo "$R")" "400"
R=$(http POST "$RUTA" '{"disasterDescription":"Incendio","resourceType":"","quantity":10,"priority":"ALTA"}')
comprobar "tipo de recurso vacío se rechaza con 400" "$(codigo "$R")" "400"
R=$(http POST "$RUTA" '{"disasterDescription":"Incendio","resourceType":"Extintores","quantity":10,"priority":"ALTA","status":"ATENDIDA"}')
comprobar "el estado enviado por el cliente se ignora" "$(cuerpo "$R" | jq -r '.status')" "PENDIENTE"

titulo "RF-3 · Atender una solicitud"
R=$(http POST "$RUTA/$ID/fulfillment")
comprobar "atender responde 200" "$(codigo "$R")" "200"
comprobar "el estado pasa a ATENDIDA" "$(cuerpo "$R" | jq -r '.status')" "ATENDIDA"
comprobar "queda registrado el momento de atención" \
    "$(cuerpo "$R" | jq -r '.fulfilledAt != null')" "true"
R=$(http POST "$RUTA/$ID/fulfillment")
comprobar "atender de nuevo se rechaza con 409" "$(codigo "$R")" "409"
R=$(http POST "$RUTA/999999/fulfillment")
comprobar "atender una solicitud inexistente responde 404" "$(codigo "$R")" "404"
R=$(http GET "$RUTA/999999")
comprobar "consultar una solicitud inexistente responde 404" "$(codigo "$R")" "404"
R=$(http GET "$RUTA/pending")
comprobar "la solicitud atendida sale del listado de pendientes" \
    "$(cuerpo "$R" | jq --argjson id "$ID" 'map(.id) | index($id) == null')" "true"
R=$(http GET "$RUTA/$ID")
comprobar "el cambio persistió en el tier de datos" "$(cuerpo "$R" | jq -r '.status')" "ATENDIDA"

if [[ "${SIN_CLIENTE:-0}" != "1" ]]; then
    titulo "Invocación remota del EJB de sesión (JNDI)"
    SALIDA=$(cd "$(dirname "$0")/.." && mvn -B -q -pl rm-client exec:java 2>&1)
    contiene "el cliente remoto registra una solicitud PENDIENTE" "$SALIDA" "en estado PENDIENTE"
    contiene "el cliente remoto la atiende" "$SALIDA" "nuevo estado ATENDIDA"
    contiene "el negocio rechaza atenderla dos veces por la vía remota" "$SALIDA" "Rechazado atender de nuevo"
    contiene "el negocio rechaza la solicitud inexistente por la vía remota" "$SALIDA" "Rechazada la solicitud inexistente"
fi

titulo "Resumen"
verde "$EXITOS comprobaciones exitosas"
[[ $FALLOS -gt 0 ]] && rojo "$FALLOS comprobaciones fallidas"
exit $((FALLOS > 0))
