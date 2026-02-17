# 🛡️ Pruebas de Seguridad DAST - Orbit QA Security Framework

Framework de automatización de pruebas de seguridad dinámica (DAST) para el aplicativo **Orbit**.
Esta solución implementa un enfoque híbrido: utiliza **Serenity BDD** para la navegación y descubrimiento de rutas (Crawling), y **OWASP ZAP** para el análisis y ataque de vulnerabilidades.

Diseñado para integrarse en pipelines de CI/CD, permitiendo escaneos de seguridad continuos sin intervención manual.
## Tech Stack

* **Lenguaje:** Java 17 (OpenJDK)
* **Build Tool:** Gradle 9.0
* **Crawler:** Serenity BDD 4.x + Cucumber (Gherkin)
* **Security Core:** OWASP ZAP Client API
* **Browser Driver:** Selenium WebDriver (Chrome Headless)
* **CI/CD:** GitHub Actions

## Estructura del Proyecto

```text
orbit-qa-security/
├── .github/
│   └── workflows/
│       └── security-scan.yml      # Pipeline de Seguridad (Navegación + Ataque)
├── src/test/
│   ├── java/
│   │   ├── starter/
│   │   │   ├── CucumberTestSuite.java   # Ejecutor de los tests de navegación
│   │   │   └── OrbitStepDefinitions.java # Lógica de Login y recorrido de módulos
│   │   └── utils/
│   │       └── ZapSecurityRunner.java   # ⚡ MOTOR HÍBRIDO: Conecta con ZAP y lanza el ataque
│   └── resources/
│       ├── features/
│       │   └── login.feature      # Escenario Gherkin para "enseñar" rutas a ZAP
│       ├── serenity.conf          # Configuración del navegador (Headless/Proxy)
│       └── cucumber.properties    # Configuración de glue y reportes
├── build.gradle                   # Dependencias y tareas personalizadas (runZapRunner)
└── README.md                      # Documentación del proyecto
```
##  Requisitos Previos
* **Java JDK 17** instalado y configurado en el PATH.

* **OWASP ZAP Desktop** instalado (para ejecución local).

* **Google Chrome** instalado (para ejecución local).

* Acceso a la red/VPN donde reside el ambiente de QA de Orbit.

## Instalación
1. Clona este repositorio:
   ```bash
   git clone <repository-url>
   cd orbit-qa-security
    ```
2. Descarga las dependencias del proyecto usando Gradle:
   ```bash
   ./gradlew clean build -x test 
   ```
## Ejecución de Pruebas
El proceso consta de dos fases: Navegación (para capturar tráfico) y Ataque (para buscar fallos).
### 1. Ejecución Local(paralela)
- **Paso 1: Iniciar ZAP y Navegación**
  Abre OWASP ZAP en tu PC (Puerto 8080) y ejecuta la navegación automatizada:
```bash
./gradlew clean test
```
Esto abrirá Chrome, navegará por Orbit y todo el tráfico quedará registrado en ZAP.
- **Paso 2: Lanzar el Ataque de Seguridad**
  Una vez terminada la navegación, ejecuta el Runner de seguridad:
```bash
./gradlew runZapRunner
```
Esto iniciará el Spider y el Active Scan sobre las URLs capturadas.
## Configuración de Seguridad (Híbrida)
El proyecto utiliza una Lógica Híbrida de Autenticación en **ZapSecurityRunner.java**:
- **Local:** Detecta si no hay API Key y permite la conexión (útil para pruebas rápidas en tu PC).
- **CI/CD:** Extrae automáticamente la **ZAP_API_KEY** de los Secrets de GitHub Actions.

Para que el Pipeline funcione correctamente, asegúrate de configurar los siguientes Secrets en tu repositorio de GitHub:
- `ORBIT_USER : Usuario de prueba`
- `ORBIT_PASS : Contraseña de prueba`
- `ZAP_API_KEY : (Opcional) API Key de ZAP si el servidor lo requiere.`

## Reportes de Pruebas (Allure)
### En Github Actions
1. Al finalizar el pipeline, el reporte se publica automáticamente en **GitHub Pages**.
2. Puedes consultarlo en la URL del repositorio (Settings -> Pages).

### Localmente
El reporte HTML se genera automáticamente al finalizar el runZapRunner:
* **Ruta:** target/zap-reports/
* **Archivo:** Reporte_Orbit_YYYYMMDD_HHMM.html
## Características Avanzadas
1. **Runner de Seguridad Inteligente:**
- Código robusto que valida la conexión con ZAP ( `api.core.version()`) antes de iniciar.

   
- Manejo de excepciones para no romper el pipeline si ZAP no responde inmediatamente.

2. **Protección de Sesión (Exclusiones Regex):**
- El escáner está configurado para ignorar automáticamente URLs de cierre de sesión:
- * `.*logout.*`, `.*salir.*`, `.*signout.*`


- Si una prueba falla por intermitencia, se reintenta hasta 2 veces antes de marcarse como error.

3. **Navegación Headless (CI Ready):**
- Configuración optimizada en `serenity.conf` (`--headless=new`) para ejecutarse en servidores Linux sin interfaz gráfica.

## Troubleshooting
### ERROR: "Connection Refused" / "Exit Value 1"
- **Causa probable:** ZAP Desktop no está abierto o no escucha en el puerto 8080.


- **Solución:** Abre ZAP y verifica en Tools > Options > Local Proxies que esté en `localhost:8080`.

### ERROR: "SessionNotCreatedException"
- **Causa probable:** La versión de Chrome y ChromeDriver no coinciden, o falta el modo headless en servidor.
- **Solución:** Asegúrate de tener `autodownload = true` en `serenity.conf` y `--headless=new` activado para CI.

### ERROR: "Cannot find symbol variable system"
- **Causa probable:** Versión antigua de la librería `zap-clientapi`.
- **Solución:** El código ya implementa el fix usando `api.core.version()` en lugar de `api.system`.


##  Archivos Clave
| Archivo                     | Propósito |
|-----------------------------|-----------|
| `ZapSecurityRunner.java`    | Inicia el Spider y el Active Scan una vez que Serenity termina de navegar. |
| `serenity.conf`     | Configura Chrome para interceptar el tráfico. |
| `login.feature`          | Contiene los pasos Gherkin (Dado/Cuando/Entonces) para loguearse y visitar los módulos. |
| `security-scan.yml`     | Orquestador de GitHub Actions. |
