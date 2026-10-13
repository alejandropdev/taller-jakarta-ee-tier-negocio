#!/usr/bin/env bash
# =============================================================================
# Renderiza los documentos de arquitectura (Markdown con Mermaid) a PDF.
#
# Cadena: mermaid-cli reemplaza cada diagrama por una imagen SVG → pandoc arma
# un HTML autocontenido → Chrome en modo headless imprime el PDF. Se usa Chrome
# en lugar de LaTeX porque no hay motor TeX instalado.
#
# Requisitos: npx (Node), pandoc y Google Chrome.
# =============================================================================
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
ORIGEN="$RAIZ/docs/architecture"
DESTINO="$ORIGEN/pdf"
TEMPORAL="$(mktemp -d)"
trap 'rm -rf "$TEMPORAL"' EXIT

CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"
# Reutiliza el Chrome instalado en lugar de descargar un Chromium para Puppeteer
export PUPPETEER_EXECUTABLE_PATH="$CHROME"

# pandoc cambió el nombre de la opción en la versión 2.19
if pandoc --help | grep -q -- --embed-resources; then
    EMBEBER="--embed-resources"
else
    EMBEBER="--self-contained"
fi

mkdir -p "$DESTINO"
cat > "$TEMPORAL/puppeteer.json" <<'JSON'
{ "args": ["--no-sandbox"] }
JSON

for documento in "$ORIGEN"/*.md; do
    nombre="$(basename "$documento" .md)"
    echo "==> $nombre"

    npx -y @mermaid-js/mermaid-cli@11.4.2 \
        --input "$documento" \
        --output "$TEMPORAL/$nombre.md" \
        --outputFormat svg \
        --puppeteerConfigFile "$TEMPORAL/puppeteer.json" >/dev/null

    # pagetitle en vez de title: evita repetir el encabezado sobre el H1 del documento.
    # markdown-implicit_figures: impide que el texto alterno de la imagen salga como pie de figura.
    pandoc "$TEMPORAL/$nombre.md" --from markdown-implicit_figures \
        --standalone "$EMBEBER" --metadata pagetitle="$nombre" \
        --resource-path "$TEMPORAL" \
        --css "$ORIGEN/pdf/estilo.css" \
        --output "$TEMPORAL/$nombre.html"

    "$CHROME" --headless --disable-gpu --no-pdf-header-footer \
        --print-to-pdf="$DESTINO/$nombre.pdf" "$TEMPORAL/$nombre.html" 2>/dev/null

    echo "    docs/architecture/pdf/$nombre.pdf"
done
