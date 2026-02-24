package com.grupocinte.qa.security.starter;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OrbitStepDefinitions extends PageObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrbitStepDefinitions.class);

    // =========================================================================
    // 1. SELECTORES Y CONFIGURACIÓN
    // =========================================================================
    private static final String INPUT_USUARIO = "[id='j_idt5:correo']";
    private static final String INPUT_PASSWORD = "[id='j_idt5:password']";
    private static final String BOTON_LOGIN = "[id='j_idt5:button']";

    private static final String LINKS_OCULTOS = "a[href*='.xhtml']";
    private static final String URL_BASE = "http://node206897-orbitcinte.w1-us.cloudjiffy.net:8080/ORBIT/";

    // =========================================================================
    // 1: ABRIR EL NAVEGADOR
    // =========================================================================
    @Dado("que el usuario abre la pagina de inicio de Orbit")
    public void abrirPaginaOrbit() {
        LOGGER.info(">>> [1/4] Iniciando navegador en: {}", URL_BASE);
        openAt(URL_BASE);
        getDriver().manage().window().maximize();
    }

    // =========================================================================
    // 2: INICIAR SESIÓN (CON ESPERA DE ZAP)
    // =========================================================================
    @Cuando("ingresa las credenciales de acceso")
    public void ingresarCredenciales() {
        LOGGER.info(">>> [2/4] Ingresando credenciales...");

        String usuario = System.getenv("ORBIT_USER") != null ? System.getenv("ORBIT_USER").trim() : "gc__@grupocinte.com";
        String pass = System.getenv("ORBIT_PASS") != null ? System.getenv("ORBIT_PASS").trim() : "Estreno32.";

        LOGGER.info("   -> Usuario detectado: {}", usuario);

        $(INPUT_USUARIO).withTimeoutOf(Duration.ofSeconds(30)).waitUntilVisible().clear();
        $(INPUT_USUARIO).type(usuario);

        $(INPUT_PASSWORD).clear();
        $(INPUT_PASSWORD).type(pass);

        LOGGER.info("   -> Haciendo click en Login...");
        $(BOTON_LOGIN).waitUntilClickable().click();

        // LA CLAVE ESTÁ AQUÍ: Obligamos al robot a sentarse y esperar 15 segundos
        LOGGER.info("   -> Esperando redireccion (Latency ZAP)...");
        waitABit(15000);
    }

    // =========================================================================
    // 3: DESCUBRIMIENTO DE MÓDULOS
    // =========================================================================
    @Y("navega por el menu principal para descubrir modulos")
    public void navegarPorModulos() {
        LOGGER.info(">>> [3/4] MODO FANTASMA: Buscando enlaces internos...");

        if (getDriver().getCurrentUrl().contains("login")) {
            LOGGER.error("!!! ERROR: Seguimos en la pagina de login. Revisa las credenciales.");
        }

        List<WebElementFacade> enlacesOcultos = findAll(LINKS_OCULTOS);
        List<String> urlsAVisitar = new ArrayList<>();

        LOGGER.info("   -> Total de enlaces encontrados en el DOM: {}", enlacesOcultos.size());

        for (WebElementFacade link : enlacesOcultos) {
            String url = link.getAttribute("href");
            if (url != null && !url.isEmpty() && !urlsAVisitar.contains(url)) {
                if(url.contains("ORBIT") && !url.contains("#")) {
                    urlsAVisitar.add(url);
                }
            }
        }

        visitarUrlsEncontradas(urlsAVisitar);
    }

    private void visitarUrlsEncontradas(List<String> urlsAVisitar) {
        int total = urlsAVisitar.size();

        if (total == 0) {
            LOGGER.warn("   -> No se encontraron enlaces en el Dashboard. Intentando rutas directas...");
            manejarRutasEmergencia();
            return;
        }

        LOGGER.info("   -> Se visitaran {} modulos unicos!", total);
        for (int i = 0; i < total; i++) {
            String urlDestino = urlsAVisitar.get(i);
            try {
                LOGGER.info("   -> [{}/{}] Visitando: {}", (i + 1), total, urlDestino);
                getDriver().get(urlDestino);
                waitABit(2000);
            } catch (Exception e) {
                LOGGER.error("      Error al intentar visitar: {}", urlDestino);
            }
        }
    }

    private void manejarRutasEmergencia() {
        String[] rutasBackup = {
                URL_BASE + "GC/index.xhtml",
                URL_BASE + "Admin/index.xhtml",
                URL_BASE + "dashboard.xhtml"
        };

        for (String ruta : rutasBackup) {
            LOGGER.info("   -> Probando ruta de emergencia: {}", ruta);
            getDriver().get(ruta);
            waitABit(3000);
        }
    }

    // =========================================================================
    // 4: VALIDACIÓN FINAL
    // =========================================================================
    @Entonces("deberia ver el dashboard principal")
    public void validarDashboard() {
        LOGGER.info(">>> [4/4] Verificando estado final de la sesion...");

        String urlActual = getDriver().getCurrentUrl();
        LOGGER.info("   URL Final: {}", urlActual);

        if (urlActual.contains("login.xhtml") || urlActual.equalsIgnoreCase(URL_BASE)) {
            LOGGER.error("FALLO: El login no persistio.");
            Assert.fail("FALLO CRITICO: La sesion se cerro o nunca inicio. URL: " + urlActual);
        } else {
            LOGGER.info("MISION CUMPLIDA: Sesion activa y navegacion completada.");
        }
    }
}