package com.referencia;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.referencia",
        plugin = {"pretty", "json:target/cucumber.json"},
        tags = ""
)
public class CucumberTestSuite {
}