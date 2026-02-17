package com.referencia.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class LoginPage extends PageObject {

    @FindBy(id = "j_idt5:correo")
    private WebElementFacade txtUsername;

    @FindBy(id = "j_idt5:password")
    private WebElementFacade txtPassword;

    @FindBy(id = "j_idt5:button")
    private WebElementFacade btnLogin;

    public void abrirPagina() {
        String url = System.getenv("QA_URL");
        if (url == null || url.isEmpty()) {
            throw new RuntimeException("Variable QA_URL no configurada");
        }
        getDriver().get(url);
    }

    public void ingresarCredenciales(String user, String pass) {
        txtUsername.waitUntilVisible().type(user);
        txtPassword.type(pass);
    }

    public void hacerClickEnLogin() {
        btnLogin.click();
    }
}