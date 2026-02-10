package utils;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ZapSecurityRunner {

    // --- CONFIGURACIÓN ---
    private static final String ZAP_ADDRESS = "localhost";
    private static final int ZAP_PORT = 8080;

    // LÓGICA HÍBRIDA: Si hay variable de entorno (GitHub), úsala. Si no, usa la fija (Local).
    private static final String ZAP_API_KEY = System.getenv("ZAP_API_KEY") != null
            ? System.getenv("ZAP_API_KEY")
            : "qcfou2f1e3uolruhfinhja6cld"; // <--- Tu clave local actual

    private static final String BASE_URL = "http://node206897-orbitcinte.w1-us.cloudjiffy.net:8080/ORBIT/";

    public static void main(String[] args) {
        // Validación de seguridad para confirmar qué llave estamos usando
        System.out.println("--- CONFIGURACIÓN DE SEGURIDAD ---");
        System.out.println("Target: " + BASE_URL);
        System.out.println("API Key en uso: " + (ZAP_API_KEY.length() > 5 ? "******" + ZAP_API_KEY.substring(ZAP_API_KEY.length() - 4) : "INVALIDA"));

        ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            System.out.println("=== INICIANDO PROTOCOLO DE SEGURIDAD ROBUSTO ===");

            String[] urlsProhibidas = {
                    ".*logout.*",
                    ".*salir.*",
                    ".*signout.*",
                    ".*borrar.*",
                    ".*eliminar.*"
            };

            for (String patron : urlsProhibidas) {
                try {
                    api.spider.excludeFromScan(patron);
                    api.ascan.excludeFromScan(patron);
                    System.out.println(">> URL Blindada (Excluida): " + patron);
                } catch (Exception e) {
                    // Ignoramos si ya estaba excluida
                }
            }

            // 1. SPIDER
            System.out.println(">>> [1/3] Ejecutando Spider...");
            ApiResponse resp = api.spider.scan(BASE_URL, null, null, null, null);
            String scanId = ((ApiResponseElement) resp).getValue();
            waitToFinish(api, scanId, "Spider");
            System.out.println(">>> Spider completado.");

            // 2. ESCANEO ACTIVO (ATAQUE)
            System.out.println(">>> [2/3] Ejecutando Escaneo Activo...");
            // scan(url, recurse, inScopeOnly, scanPolicyName, method, postData)
            resp = api.ascan.scan(BASE_URL, "true", "false", null, null, null);
            scanId = ((ApiResponseElement) resp).getValue();
            waitToFinish(api, scanId, "Ataque Activo");
            System.out.println(">>> Ataque completado.");

            // 3. REPORTE HTML
            System.out.println(">>> [3/3] Generando Reporte...");
            byte[] report = api.core.htmlreport();

            // Aseguramos que la carpeta target exista
            Files.createDirectories(Paths.get("target/zap-reports"));

            String fecha = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
            String nombreArchivo = "target/zap-reports/Reporte_Orbit_" + fecha + ".html";
            Files.write(Paths.get(nombreArchivo), report);

            System.out.println("¡ÉXITO! Reporte guardado en: " + nombreArchivo);

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void waitToFinish(ClientApi api, String scanId, String type) throws Exception {
        int progress = 0;
        while (progress < 100) {
            Thread.sleep(5000); // Revisamos cada 5 segundos
            if (type.equals("Spider")) {
                progress = Integer.parseInt(((ApiResponseElement) api.spider.status(scanId)).getValue());
            } else {
                progress = Integer.parseInt(((ApiResponseElement) api.ascan.status(scanId)).getValue());
            }
            System.out.print("\r[" + type + "] Progreso: " + progress + "%");
        }
        System.out.println();
    }
}