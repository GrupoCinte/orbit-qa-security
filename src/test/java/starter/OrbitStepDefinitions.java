package starter;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OrbitStepDefinitions extends PageObject {

    // Logger profesional para cumplir con estándares de Clean Code
    private static final Logger LOGGER = LoggerFactory.getLogger(OrbitStepDefinitions.class);

    // =========================================================================
    // 1. SELECTORES Y CONFIGURACIÓN
    // =========================================================================

    private static final String INPUT_USUARIO = "[id='j_idt5:correo']";
    private static final String INPUT_PASSWORD = "[id='j_idt5:password']";
    private static final String BOTON_LOGIN = "[id='j_idt5:button']";
    private static final String LINKS_OCULTOS = "#intro a";
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
    // 2: INICIAR SESIÓN
    // =========================================================================
    @Cuando("ingresa las credenciales de acceso")
    public void ingresarCredenciales() {
        LOGGER.info(">>> [2/4] Ingresando credenciales...");

        String usuario = "gc__@grupocinte.com";
        String pass = "Estreno32.";

        $(INPUT_USUARIO).waitUntilVisible().clear();
        $(INPUT_USUARIO).type(usuario);

        $(INPUT_PASSWORD).waitUntilVisible().clear();
        $(INPUT_PASSWORD).type(pass);

        $(BOTON_LOGIN).waitUntilClickable().click();
    }

    // =========================================================================
    // 3: DESCUBRIMIENTO DE MÓDULOS (Modo Fantasma)
    // =========================================================================
    @Y("navega por el menu principal para descubrir modulos")
    public void navegarPorModulos() {
        LOGGER.info(">>> [3/4] MODO FANTASMA: Iniciando extraccion masiva de modulos...");

        // Reemplazo de Thread.sleep por waitABit (estándar de Serenity)
        waitABit(5000);

        List<WebElementFacade> enlacesOcultos = findAll(LINKS_OCULTOS);
        List<String> urlsAVisitar = new ArrayList<>();

        LOGGER.info("   -> Total de elementos brutos encontrados: {}", enlacesOcultos.size());

        for (WebElementFacade link : enlacesOcultos) {
            String url = link.getAttribute("href");
            String texto = link.getAttribute("textContent").trim().toLowerCase();

            // Combinación de IFs para cumplir con la regla java:S1066 de SonarQube
            if (isValidLink(url, texto) && !urlsAVisitar.contains(url)) {
                LOGGER.info("      [+] Agregado a la ruta: {} -> {}", texto, url);
                urlsAVisitar.add(url);
            }
        }

        visitarUrlsEncontradas(urlsAVisitar);
    }

    private boolean isValidLink(String url, String texto) {
        return url != null && !url.isEmpty() && !url.contains("javascript") && !url.contains("#") &&
                !texto.contains("salir") && !texto.contains("cerrar") && !texto.contains("olvidaste");
    }

    private void visitarUrlsEncontradas(List<String> urlsAVisitar) {
        int total = urlsAVisitar.size();
        LOGGER.info("   -> Se visitaran {} modulos unicos!", total);

        for (int i = 0; i < total; i++) {
            String urlDestino = urlsAVisitar.get(i);
            try {
                LOGGER.info("   -> [{}/{}] Visitando: {}", (i + 1), total, urlDestino);
                getDriver().get(urlDestino);
                waitABit(2500);
            } catch (Exception e) {
                // Bloque catch con comentario y log para cumplir con java:S108
                LOGGER.error("      Error al intentar visitar: {}", urlDestino, e);
            }
        }

        if (urlsAVisitar.isEmpty()) {
            manejarRutasEmergencia();
        }
    }

    private void manejarRutasEmergencia() {
        LOGGER.warn("ALERTA: No se encontraron enlaces automaticos. Usando rutas de emergencia.");
        getDriver().get(URL_BASE + "GC/index.xhtml");
        waitABit(3000);
        getDriver().get(URL_BASE + "Admin/index.xhtml");
    }

    // =========================================================================
    // 4: VALIDACIÓN FINAL
    // =========================================================================
    @Entonces("deberia ver el dashboard principal")
    public void validarDashboard() {
        LOGGER.info(">>> [4/4] Verificando estado final de la sesion...");

        String urlActual = getDriver().getCurrentUrl();

        if (urlActual.contains("login.xhtml") || urlActual.equals(URL_BASE)) {
            Assert.fail("FALLO CRITICO: La sesion se cerro o nunca inicio. URL: " + urlActual);
        } else {
            LOGGER.info("MISION CUMPLIDA: Recorrido completo finalizado.");
            LOGGER.info("   Sesion activa en: {}", urlActual);
            getDriver().navigate().refresh();
        }
    }
}