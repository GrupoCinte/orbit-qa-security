package starter;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.List;

public class OrbitStepDefinitions extends PageObject {

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
        System.out.println(">>> [1/4] Iniciando navegador en: " + URL_BASE);
        openAt(URL_BASE);
        getDriver().manage().window().maximize();
    }

    // =========================================================================
    // 2: INICIAR SESIÓN
    // =========================================================================
    @Cuando("ingresa las credenciales de acceso")
    public void ingresarCredenciales() {
        System.out.println(">>> [2/4] Ingresando credenciales...");

        String usuario = "gc__@grupocinte.com";
        String pass = "Estreno32.";

        $(INPUT_USUARIO).waitUntilVisible().clear();
        $(INPUT_USUARIO).type(usuario);

        $(INPUT_PASSWORD).waitUntilVisible().clear();
        $(INPUT_PASSWORD).type(pass);

        $(BOTON_LOGIN).waitUntilClickable().click();
    }

    // =========================================================================
    @Y("navega por el menu principal para descubrir modulos")
    public void navegarPorModulos() {
        System.out.println(">>> [3/4] MODO FANTASMA: Iniciando extracción masiva de módulos...");

        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        List<WebElementFacade> enlacesOcultos = findAll(LINKS_OCULTOS);
        List<String> urlsAVisitar = new ArrayList<>();

        System.out.println("   -> Total de elementos brutos encontrados: " + enlacesOcultos.size());

        for (WebElementFacade link : enlacesOcultos) {
            String url = link.getAttribute("href");
            String texto = link.getAttribute("textContent").trim();

            if (url != null && !url.isEmpty() && !url.contains("javascript") && !url.contains("#")) {
                if (!texto.toLowerCase().contains("salir") &&
                        !texto.toLowerCase().contains("cerrar") &&
                        !texto.toLowerCase().contains("olvidaste")) {

                    if (!urlsAVisitar.contains(url)) {
                        System.out.println("      [+] Agregado a la ruta: " + texto + " -> " + url);
                        urlsAVisitar.add(url);
                    }
                }
            }
        }

        System.out.println("   -> ¡Se visitarán " + urlsAVisitar.size() + " módulos únicos!");

        int total = urlsAVisitar.size();
        for (int i = 0; i < total; i++) {
            String urlDestino = urlsAVisitar.get(i);
            try {
                System.out.println("   -> [" + (i+1) + "/" + total + "] Visitando: " + urlDestino);

                getDriver().get(urlDestino);

                Thread.sleep(2500);

            } catch (Exception e) {
                System.out.println("      ⚠️ Error al intentar visitar: " + urlDestino);
            }
        }

        if (urlsAVisitar.isEmpty()) {
            System.err.println("⚠️ ALERTA: No se encontraron enlaces automáticos. Usando rutas de emergencia.");
            getDriver().get(URL_BASE + "GC/index.xhtml");
            try { Thread.sleep(3000); } catch (Exception e) {}
            getDriver().get(URL_BASE + "Admin/index.xhtml");
        }
    }

    // =========================================================================
    // 4: VALIDACIÓN FINAL
    // =========================================================================
    @Entonces("deberia ver el dashboard principal")
    public void validarDashboard() {
        System.out.println(">>> [4/4] Verificando estado final de la sesión...");

        String urlActual = getDriver().getCurrentUrl();

        if (urlActual.contains("login.xhtml") || urlActual.equals(URL_BASE)) {
            Assert.fail("❌ FALLO CRÍTICO: La sesión se cerró o nunca inició. URL: " + urlActual);
        } else {
            System.out.println("✅ MISIÓN CUMPLIDA: Recorrido completo finalizado.");
            System.out.println("   Sesión activa en: " + urlActual);

            getDriver().navigate().refresh();
        }
    }
}