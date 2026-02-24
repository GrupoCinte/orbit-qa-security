package com.grupocinte.qa.security.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ZapSecurityRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZapSecurityRunner.class);

    // 👇 CORRECCIÓN: Usamos 127.0.0.1 en lugar de localhost para evitar fallos de red en GitHub Actions
    private static final String ZAP_ADDRESS = "127.0.0.1";
    private static final int ZAP_PORT = 8090;
    private static final String BASE_URL = "http://node206897-orbitcinte.w1-us.cloudjiffy.net:8080/ORBIT/";

    // MODIFICACIÓN HÍBRIDA: No lanzamos error si es null, solo avisamos
    private static final String ZAP_API_KEY = System.getenv("ZAP_API_KEY") != null ? System.getenv("ZAP_API_KEY").trim() : "qcfou2f1e3uolruhfinhja6cld";

    public static void main(String[] args) {
        LOGGER.info("--- PROTOCOLO DE SEGURIDAD ZAP INICIADO ---");

        if (ZAP_API_KEY.isEmpty() || ZAP_API_KEY.equals("qcfou2f1e3uolruhfinhja6cld")) {
            LOGGER.warn("[MODO LOCAL] Se está usando la clave por defecto o no se encontró ZAP_API_KEY.");
        }

        ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            // Validamos conexión antes de atacar
            validarConexion(api);

            configurarExclusiones(api);

            // 1. SPIDER
            LOGGER.info(">>> [1/3] Ejecutando Spider...");
            ApiResponse resp = api.spider.scan(BASE_URL, null, null, null, null);
            String scanId = ((ApiResponseElement) resp).getValue();
            waitToFinish(api, scanId, "Spider");

            // 2. ESCANEO ACTIVO
            LOGGER.info(">>> [2/3] Ejecutando Escaneo Activo...");
            resp = api.ascan.scan(BASE_URL, "true", "false", null, null, null);
            scanId = ((ApiResponseElement) resp).getValue();
            waitToFinish(api, scanId, "Ataque Activo");

            // 3. REPORTE
            generarReporteSeguridad(api);

        } catch (Exception e) {
            // 👇 CORRECCIÓN: El mensaje de error ahora dice 8090
            LOGGER.error("FALLO CRÍTICO: {}. Asegúrate de que ZAP esté escuchando en el puerto {}.", e.getMessage(), ZAP_PORT);
            System.exit(1);
        }
    }

    private static void validarConexion(ClientApi api) throws Exception {
        try {
            api.core.version();
            LOGGER.info("Conexión establecida con éxito con OWASP ZAP.");
        } catch (Exception e) {
            // 👇 CORRECCIÓN: El texto ahora dice 8090
            throw new Exception("No se pudo conectar a ZAP en " + ZAP_ADDRESS + ":" + ZAP_PORT + ". Verifique que el contenedor esté corriendo.");
        }
    }

    private static void configurarExclusiones(ClientApi api) {
        String[] urlsProhibidas = {".*logout.*", ".*salir.*", ".*signout.*", ".*borrar.*", ".*eliminar.*"};
        for (String patron : urlsProhibidas) {
            try {
                api.spider.excludeFromScan(patron);
                api.ascan.excludeFromScan(patron);
                LOGGER.info(">> URL Excluida: {}", patron);
            } catch (Exception e) {
                LOGGER.warn("No se pudo excluir el patrón: {}", patron);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void generarReporteSeguridad(ClientApi api) throws Exception {
        LOGGER.info(">>> [3/3] Generando Reporte...");
        byte[] report = api.core.htmlreport();
        Files.createDirectories(Paths.get("target/zap-reports"));
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String nombreArchivo = "target/zap-reports/Reporte_Orbit_" + fecha + ".html";
        Files.write(Paths.get(nombreArchivo), report);
        LOGGER.info("¡ÉXITO! Reporte guardado en: {}", nombreArchivo);
    }

    @SuppressWarnings("java:S2925")
    private static void waitToFinish(ClientApi api, String scanId, String type) throws Exception {
        int progress = 0;
        while (progress < 100) {
            TimeUnit.SECONDS.sleep(5);
            if ("Spider".equals(type)) {
                progress = Integer.parseInt(((ApiResponseElement) api.spider.status(scanId)).getValue());
            } else {
                progress = Integer.parseInt(((ApiResponseElement) api.ascan.status(scanId)).getValue());
            }
            LOGGER.info("[{}] Progreso: {}%", type, progress);
        }
    }
}