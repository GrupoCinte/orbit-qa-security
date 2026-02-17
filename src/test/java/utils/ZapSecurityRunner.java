package utils;

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

    // --- CONFIGURACIÓN ---
    private static final String ZAP_ADDRESS = "localhost";
    private static final int ZAP_PORT = 8080;

    private static final String ZAP_API_KEY = System.getenv("ZAP_API_KEY") != null ? System.getenv("ZAP_API_KEY").trim() : null;
    private static final String BASE_URL = "http://node206897-orbitcinte.w1-us.cloudjiffy.net:8080/ORBIT/";

    public static void main(String[] args) {
        if (ZAP_API_KEY == null || ZAP_API_KEY.isEmpty()) {
            throw new RuntimeException("ERROR CRITICO: No se encontro la variable de entorno 'ZAP_API_KEY'. Configure el secreto en GitHub o en su IDE.");
        }

        LOGGER.info("--- PROTOCOLO DE SEGURIDAD ZAP INICIADO ---");

        ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            configurarExclusiones(api);

            // 1. SPIDER (Araña)
            LOGGER.info(">>> [1/3] Ejecutando Spider...");
            ApiResponse resp = api.spider.scan(BASE_URL, null, null, null, null);
            String scanId = ((ApiResponseElement) resp).getValue();
            waitToFinish(api, scanId, "Spider");
            LOGGER.info(">>> Spider completado.");

            // 2. ESCANEO ACTIVO (Ataque)
            LOGGER.info(">>> [2/3] Ejecutando Escaneo Activo...");
            resp = api.ascan.scan(BASE_URL, "true", "false", null, null, null);
            scanId = ((ApiResponseElement) resp).getValue();
            waitToFinish(api, scanId, "Ataque Activo");
            LOGGER.info(">>> Ataque completado.");

            // 3. REPORTE
            generarReporteSeguridad(api);

        } catch (Exception e) {
            LOGGER.error("ERROR DURANTE EL ESCANEO: {}", e.getMessage(), e);
            throw new RuntimeException("La ejecucion de seguridad fallo.", e);
        }
    }

    private static void configurarExclusiones(ClientApi api) {
        String[] urlsProhibidas = {".*logout.*", ".*salir.*", ".*signout.*", ".*borrar.*", ".*eliminar.*"};

        for (String patron : urlsProhibidas) {
            try {
                api.spider.excludeFromScan(patron);
                api.ascan.excludeFromScan(patron);
                LOGGER.info(">> URL Blindada (Excluida): {}", patron);
            } catch (Exception e) {
                LOGGER.warn("No se pudo excluir el patron: {} - {}", patron, e.getMessage());
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

        LOGGER.info("EXITO! Reporte guardado en: {}", nombreArchivo);
    }

    // SOLUCIÓN FINAL: Se suprime la regla S2925 porque el sleep es indispensable para esperar a ZAP
    @SuppressWarnings("java:S2925")
    private static void waitToFinish(ClientApi api, String scanId, String type) throws Exception {
        int progress = 0;
        while (progress < 100) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Espera interrumpida", e);
            }

            if ("Spider".equals(type)) {
                progress = Integer.parseInt(((ApiResponseElement) api.spider.status(scanId)).getValue());
            } else {
                progress = Integer.parseInt(((ApiResponseElement) api.ascan.status(scanId)).getValue());
            }
            LOGGER.info("[{}] Progreso: {}%", type, progress);
        }
    }
}