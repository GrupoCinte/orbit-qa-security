# run.ps1
Write-Host "=== EJECUTANDO PROYECTO JSF DAST ===" -ForegroundColor Cyan

# Limpiar
Write-Host "1. Limpiando proyecto..." -ForegroundColor Yellow
mvn clean

# Compilar
Write-Host "2. Compilando proyecto..." -ForegroundColor Yellow
mvn compile test-compile

# Ejecutar tests
Write-Host "3. Ejecutando pruebas..." -ForegroundColor Yellow
mvn verify

# Generar reportes
Write-Host "4. Generando reportes..." -ForegroundColor Yellow
mvn serenity:aggregate

# Verificar resultados
Write-Host "`n=== RESULTADOS ===" -ForegroundColor Green
if (Test-Path "target/site/serenity/index.html") {
    Write-Host "Reporte generado en: target/site/serenity/index.html" -ForegroundColor Green
    Write-Host "Abrir reporte con: Invoke-Item 'target/site/serenity/index.html'" -ForegroundColor White
} else {
    Write-Host "ERROR: No se generó el reporte" -ForegroundColor Red
}

Write-Host "`nProceso completado!" -ForegroundColor Cyan