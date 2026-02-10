package com.referencia;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class) // Esto requiere JUnit 4
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.referencia", // <--- CAMBIO: Apunta directo al paquete steps
        plugin = {"pretty", "json:target/cucumber.json"},
        tags = ""
)
public class CucumberTestSuite {
}