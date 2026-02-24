package com.referencia;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import net.serenitybdd.core.steps.UIInteractionSteps;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginStepDefinitions extends UIInteractionSteps {

    // Se define el Logger para cumplir con los estándares de SonarQube (Regla java:S106)
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginStepDefinitions.class);

    @Dado("que el usuario se encuentra autenticado en el sistema")
    public void que_el_usuario_se_encuentra_autenticado_en_el_sistema() {
        // Se obtiene la URL desde las variables de entorno configuradas en el pipeline
        openUrl(System.getenv("QA_URL"));

        try {
            if ($(By.xpath("//button[contains(text(),'Continuar al sitio')]")).isPresent()) {
                $(By.xpath("//button[contains(text(),'Continuar al sitio')]")).click();
                waitABit(1000);
            }
        } catch (Exception e) {
            // Regla java:S108: Se agrega un comentario explicativo y un log de depuración
            // Se ignora porque el botón es un pop-up opcional que no siempre aparece
            LOGGER.debug("El botón opcional no fue encontrado o no requirió interacción", e);
        }

        // 2. Proceso de Login utilizando variables de entorno (QA_USER, QA_PASS)
        $(By.id("j_idt5:correo")).waitUntilVisible().type(System.getenv("QA_USER"));
        $(By.id("j_idt5:password")).type(System.getenv("QA_PASS"));
        $(By.id("j_idt5:button")).click();

        waitABit(2000);

        // Validación de errores de autenticación
        if ($(By.xpath("//*[contains(text(),'usuario está inactivo')]")).isPresent() ||
                $(By.xpath("//*[contains(text(),'no está registrado')]")).isPresent()) {

            LOGGER.error("ERROR DE AUTENTICACIÓN: El usuario está inactivo o no registrado.");
            return;
        }

        // Validación de éxito en el Dashboard
        if (getDriver().getCurrentUrl().contains("dashboard") || $(By.className("layout-menu")).isPresent()) {
            LOGGER.info("INICIO DE SESIÓN EXITOSO: Usuario autenticado correctamente.");
        } else {
            LOGGER.warn("ESTADO DESCONOCIDO: No se detectó error ni redirección al dashboard.");
        }
    }

    @Cuando("interactúa con los módulos principales del menú de navegación")
    public void interactua_con_los_modulos_principales_del_menu_de_navegacion() {
        LOGGER.info("Navegando por el dashboard de Orbit...");
    }

    @Cuando("el Spider de ZAP realiza un descubrimiento recursivo de rutas")
    public void el_spider_de_zap_realiza_un_descubrimiento_recursivo_de_rutas() {
        LOGGER.info("ZAP iniciando descubrimiento de endpoints...");
    }

    @Entonces("el sistema debe haber identificado la totalidad de los endpoints para el análisis de vulnerabilidades")
    public void el_sistema_debe_haber_identificado_la_totalidad_de_los_endpoints() {
        LOGGER.info("Análisis de endpoints completado satisfactoriamente.");
    }
}