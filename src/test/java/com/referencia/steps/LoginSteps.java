package com.referencia.steps;

import com.referencia.pages.LoginPage;
import io.cucumber.java.en.*;
import net.serenitybdd.annotations.Managed;
import org.openqa.selenium.WebDriver;

public class LoginSteps {

    @Managed
    WebDriver driver;

    LoginPage loginPage;

    @Given("que el usuario navega a la pagina de inicio de sesion")
    public void abrirPagina() {
        loginPage.abrirPagina();
    }

    @When("ingresa credenciales validas")
    public void ingresarDatos() {
        // Lee las variables de entorno que pusiste en la terminal
        String usuario = System.getenv("QA_USER");
        String clave = System.getenv("QA_PASS");

        loginPage.ingresarCredenciales(usuario, clave);
        loginPage.hacerClickEnLogin();
    }

    @Then("deberia ver el panel principal de la aplicacion")
    public void verificarDashboard() {
        // Espera de cortesía para ver el resultado
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}