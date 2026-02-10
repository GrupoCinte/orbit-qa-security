# 🛡️ Orbit QA Security - DAST Automation Project

Este proyecto es una **implementación de referencia DevSecOps** para la ejecución de **Pruebas de Seguridad Dinámicas (DAST)** automatizadas sobre la aplicación **Orbit**.

El sistema integra **Serenity BDD** para la navegación funcional y **OWASP ZAP** para el escaneo de seguridad, orquestado completamente mediante un pipeline de **CI/CD en GitHub Actions**.

---

## 🏗️ Arquitectura de la Solución

El flujo de trabajo se ejecuta de manera desatendida y consta de tres fases críticas:

### 1. Navegación Funcional (Traffic Generation)
Se utilizan **Serenity BDD + Cucumber** para simular un usuario real navegando por la aplicación.
* **Objetivo:** Generar tráfico HTTP válido y autenticado.
* **Proxy:** Todo el tráfico de las pruebas funcionales pasa a través del puerto `8080`, donde **OWASP ZAP** está escuchando como un proxy pasivo.
* **Credenciales:** Se inyectan de forma segura (sin hardcoding) mediante variables de entorno y GitHub Secrets.

### 2. Análisis de Seguridad (DAST Attack)
Una vez capturado el tráfico, se ejecuta la utilidad personalizada `ZapSecurityRunner` (Java):
* **Spider:** Rastrea la aplicación para descubrir nuevas URLs ocultas.
* **Active Scan:** Lanza ataques controlados contra los endpoints descubiertos.
* **Sanitización:** El runner limpia automáticamente la API Key (`.trim()`) para evitar errores de formato en el entorno CI.

### 3. Reporte y Artefactos
Al finalizar, se generan reportes técnicos que detallan las vulnerabilidades encontradas (XSS, SQLi, Headers, etc.) clasificadas por severidad.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje & Build:**
  * ☕ Java 17 (OpenJDK Temurin)
  * 🐘 Gradle 8.x (Gestión de dependencias y tareas)

* **Framework de Pruebas:**
  * Serenity BDD
  * Cucumber (Gherkin)
  * Selenium WebDriver

* **Seguridad DAST:**
  * ⚡ OWASP ZAP (Imagen Docker: `ghcr.io/zaproxy/zaproxy:stable`)
  * ZAP Client API (Java)

* **Infraestructura CI/CD:**
  * 🐳 Docker
  * GitHub Actions (Ubuntu Latest)

---

## 🔐 Configuración de Secretos (GitHub)

Para que el pipeline funcione, es **obligatorio** configurar los siguientes secretos en el repositorio:

**Ruta:** `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

| Nombre Secreto | Descripción | Ejemplo / Notas |
| :--- | :--- | :--- |
| `ZAP_API_KEY` | Clave de API para controlar ZAP | `qcfou2f1e3uolruhfinhja6cld` |
| `ORBIT_USER` | Usuario válido para login en Orbit | `admin` |
| `ORBIT_PASS` | Contraseña del usuario | `Password123!` |

> **Nota de Seguridad:** El código fuente `ZapSecurityRunner.java` utiliza `.trim()` automáticamente en la API Key para prevenir errores por espacios invisibles al copiar los secretos.

---

## 🚀 Ejecución del Pipeline

El archivo de flujo de trabajo se encuentra en `.github/workflows/security-scan.yml`.

### Disparadores (Triggers)
El pipeline se activa automáticamente en los siguientes eventos:
1.  **Push** a las ramas `develop` o `main`.
2.  **Pull Request** hacia `develop` o `main`.
3.  **Ejecución manual** (Workflow Dispatch).

### Pasos del Workflow
1.  **Checkout & Setup:** Descarga el código y configura Java 17.
2.  **Permisos:** Otorga permisos de ejecución (`chmod +x`) al wrapper de Gradle para evitar errores (Exit 126).
3.  **Docker ZAP:** Descarga e inicia el contenedor de ZAP en modo daemon (Puerto 8080).
  * *Timeout extendido a 60 min para prevenir fallos de red (Exit 124).*
4.  **Tests Serenity:** Ejecuta la navegación funcional inyectando los secretos de login.
5.  **Ataque ZAP:** Ejecuta `runZapRunner` para iniciar el escaneo activo.
6.  **Publicación:** Sube el reporte HTML como un artefacto descargable.

---

## 📊 Reportes Generados

Al finalizar una ejecución exitosa en GitHub Actions, encontrarás el siguiente artefacto en la sección **Summary**:

### 📄 `zap-security-report`
Archivo HTML (`Reporte_Orbit_YYYYMMDD.html`) que contiene:
* Resumen de alertas por nivel de riesgo (Alto, Medio, Bajo, Informativo).
* Descripción detallada de cada vulnerabilidad.
* Evidencia de la petición y respuesta HTTP.
* Recomendaciones de solución.

---

## 💻 Ejecución Local (Para Desarrolladores)

Si deseas correr las pruebas en tu máquina antes de subir cambios:

1.  **Levantar ZAP (Docker):**
    ```bash
    docker run -u zap -p 8080:8080 -i ghcr.io/zaproxy/zaproxy:stable /zap/zap.sh -daemon -host 0.0.0.0 -port 8080 -config api.key=TU_API_KEY
    ```

2.  **Configurar Variables de Entorno (IntelliJ / Terminal):**
  * `ZAP_API_KEY=TU_API_KEY`
  * `ORBIT_USER=tu_usuario`
  * `ORBIT_PASS=tu_pass`

3.  **Ejecutar:**
    ```bash
    # Ejecutar navegación y luego escaneo
    ./gradlew test
    ./gradlew runZapRunner
    ```

---
**Maintained by:** QA Automation Team - Grupo Cinte