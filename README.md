
---

# JSF DAST Security Reference Project

Este proyecto es una **implementación de referencia** para **Pruebas de Seguridad Dinámicas (DAST)** automatizadas sobre aplicaciones **JSF**, integradas directamente en un pipeline **CI/CD con GitHub Actions**.

El objetivo es demostrar cómo aplicar un enfoque **DevSecOps**, donde las pruebas de seguridad forman parte natural del ciclo de vida del desarrollo, sin intervención manual y sin hardcoding de información sensible.

---

##  Arquitectura del Proyecto

El flujo de trabajo se ejecuta **100% en GitHub Actions** y consta de **tres fases principales**:

###  Navegación Autenticada

Se utilizan **Serenity BDD + Selenium + Cucumber** para ejecutar pruebas funcionales reales, incluyendo un **login autenticado** en la aplicación JSF.

Esto permite:

* Acceder a áreas protegidas
* Generar tráfico real y representativo
* Mapear correctamente la superficie de ataque

---

### Análisis de Seguridad (DAST)

Un contenedor **OWASP ZAP** se ejecuta en modo *daemon* y actúa como **proxy de seguridad**:

* Intercepta todo el tráfico generado por Serenity
* Ejecuta **Spider** para descubrir URLs y parámetros
* Lanza un **Active Scan** con ataques controlados

ZAP analiza vulnerabilidades como:

* XSS
* SQL Injection
* CSRF
* Security Headers
* Path Traversal
* Misconfigurations

---

###  Reporte de Evidencias

Al finalizar el pipeline se generan y publican:

* **Reporte técnico de seguridad (ZAP)**
* **Reporte funcional Serenity** con pasos y screenshots

Ambos quedan disponibles como **Artifacts** del pipeline.

---

##  Tecnologías Utilizadas

* **Lenguaje**:

    * Java 17 (tests funcionales)
    * Python 3 (control de ZAP vía API)

* **Framework de Pruebas**:

    * Serenity BDD
    * Cucumber
    * Selenium WebDriver

* **Herramienta de Seguridad**:

    * OWASP ZAP (Zaproxy)

* **Infraestructura**:

    * Docker
    * GitHub Actions

---


##  Variables Requeridas en GitHub

Antes de ejecutar el pipeline, configura los siguientes valores en:

**Settings → Secrets and variables → Actions**

### Variables

| Nombre   | Tipo     | Descripción                            |
| -------- | -------- | -------------------------------------- |
| `QA_URL` | Variable | URL base de la aplicación JSF a probar |

### Secrets

| Nombre     | Tipo   | Descripción                                              |
| ---------- | ------ | -------------------------------------------------------- |
| `ZAP_ADDR` | Secret | Dirección del servidor ZAP (ej: `http://localhost:8080`) |
| `QA_USER`  | Secret | Usuario para el login                                    |
| `QA_PASS`  | Secret | Contraseña para el login                                 |

---

##  Ejecución del Pipeline

El pipeline se activa **automáticamente con cada `push`** y realiza los siguientes pasos:

1. Levanta un contenedor **OWASP ZAP** en modo daemon
2. Ejecuta pruebas de **Serenity**, redirigiendo el tráfico a través del proxy de ZAP
3. Ejecuta **Spider + Active Scan** mediante un script en Python
4. Publica los artefactos de seguridad y pruebas funcionales

---

##  Reportes Generados

### Serenity Report

* Flujo funcional completo
* Pasos ejecutados
* Evidencia visual (screenshots)
* Estado de cada escenario

📂 `target/site/serenity/index.html`

---

###  ZAP Security Report

* Vulnerabilidades detectadas
* Clasificación por riesgo:

    * Alto
    * Medio
    * Bajo
* Evidencia técnica

📄 `zap-report.html`

---

## 🧑‍💻 ¿Cómo usar este proyecto?

1. Haz un **fork** del repositorio
2.  Configura los **Secrets y Variables** en GitHub
3. Realiza cualquier cambio en el código
4.  Haz `push`
5. Observa la ejecución en la pestaña **Actions**
6. Descarga los **Artifacts** con los reportes

---

## Objetivo del Proyecto

Este repositorio sirve como:

* Referencia técnica de **DAST automatizado**
* Base para pipelines **DevSecOps**

* Punto de partida para integrar seguridad en CI/CD

---
