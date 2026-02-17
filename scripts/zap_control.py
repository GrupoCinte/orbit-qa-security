import requests
import time
import os

# ZAP_ADDR se toma del Secret de GitHub y QA_URL de las variables.
ZAP_URL = os.getenv("ZAP_ADDR")
TARGET = os.getenv("QA_URL")

def wait_for_task(task_id, api_path):
    """
    Consulta el estado del escaneo (Spider o Active Scan) hasta que llegue al 100%.
    """
    while True:
        try:
            # Consulta el progreso a la API de ZAP usando la URL dinámica
            base_url = f"{ZAP_URL}/JSON/{api_path}/view/status/"
            response = requests.get(base_url, params={'scanId': task_id})
            status = response.json().get('status')

            print(f"Progreso {api_path}: {status}%")

            if status is not None and int(status) >= 100:
                break
        except Exception as e:
            print(f"Error consultando estado en {api_path}: {e}")
            break
        time.sleep(5)

def run_zap():
    # Validación de seguridad: el script no arranca si faltan datos
    if not TARGET or not ZAP_URL:
        print("Error: Las variables de entorno QA_URL o ZAP_ADDR no están definidas.")
        return

    print(f"Iniciando análisis de seguridad en: {TARGET}")
    print(f"Usando instancia de ZAP en: {ZAP_URL}")

    try:
        # 1. Spider Scan (Mapeo de la aplicación)
        print(" Iniciando Spider...")
        spider_resp = requests.get(f"{ZAP_URL}/JSON/spider/action/scan/", params={'url': TARGET})
        spider_id = spider_resp.json().get('scan')
        wait_for_task(spider_id, "spider")

        # 2. Active Scan (Ataque de vulnerabilidades)
        print("Iniciando Active Scan...")
        ascan_resp = requests.get(f"{ZAP_URL}/JSON/ascan/action/scan/", params={'url': TARGET})
        scan_id = ascan_resp.json().get('scan')
        wait_for_task(scan_id, "ascan")

        # 3. Generación de Reporte HTML
        print("Generando reporte final...")
        report_url = f"{ZAP_URL}/OTHER/core/other/htmlreport/"
        report_data = requests.get(report_url).text

        with open("zap-report.html", "w", encoding="utf-8") as f:
            f.write(report_data)
        print(" Reporte 'zap-report.html' creado con éxito.")

    except requests.exceptions.ConnectionError:
        print(f"Error: No se pudo conectar con ZAP en {ZAP_URL}.")
    except Exception as e:
        print(f" Ocurrió un error inesperado: {e}")

if __name__ == "__main__":
    run_zap()