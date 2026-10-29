

from http.server import HTTPServer, BaseHTTPRequestHandler
from pathlib import Path
import json

CLAVE_API = "RiegoIoT-2026-demo"
ARCHIVO_DATOS = Path(__file__).with_name("riego_datos.json")


def cargar_datos():
    if ARCHIVO_DATOS.exists():
        try:
            with open(ARCHIVO_DATOS, "r", encoding="utf-8") as archivo:
                datos = json.load(archivo)

            humedad = int(datos.get("humedad", 35))
            riego = datos.get("riego", False)

            if 0 <= humedad <= 100 and isinstance(riego, bool):
                return {"humedad": humedad, "riego": riego}

        except (OSError, ValueError, TypeError, json.JSONDecodeError):
            print("No fue posible leer los datos guardados.")

    return {"humedad": 35, "riego": False}


estado = cargar_datos()


def guardar_datos():
    temporal = ARCHIVO_DATOS.with_suffix(".tmp")

    with open(temporal, "w", encoding="utf-8") as archivo:
        json.dump(estado, archivo, indent=4)

    temporal.replace(ARCHIVO_DATOS)


class ServidorRiego(BaseHTTPRequestHandler):

    def autorizado(self):
        return self.headers.get("X-API-Key") == CLAVE_API

    def responder(self, codigo, datos):
        respuesta = json.dumps(datos).encode("utf-8")

        self.send_response(codigo)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(respuesta)))
        self.end_headers()
        self.wfile.write(respuesta)

    def do_GET(self):
        if not self.autorizado():
            self.responder(401, {"error": "Acceso no autorizado"})
            return

        self.responder(200, estado)

    def do_POST(self):
        global estado

        if not self.autorizado():
            self.responder(401, {"error": "Acceso no autorizado"})
            return

        try:
            longitud = int(self.headers.get("Content-Length", 0))

            if longitud <= 0 or longitud > 10000:
                self.responder(400, {"error": "Tamaño de solicitud inválido"})
                return

            datos = json.loads(
                self.rfile.read(longitud).decode("utf-8")
            )

            humedad = datos.get("humedad", estado["humedad"])
            riego = datos.get("riego", estado["riego"])

            if isinstance(humedad, bool) or not isinstance(humedad, int):
                self.responder(400, {"error": "Humedad inválida"})
                return

            if not 0 <= humedad <= 100:
                self.responder(400, {"error": "Humedad fuera de rango"})
                return

            if not isinstance(riego, bool):
                self.responder(400, {"error": "Estado de riego inválido"})
                return

            estado_anterior = estado.copy()
            estado = {"humedad": humedad, "riego": riego}

            try:
                guardar_datos()
            except OSError:
                estado = estado_anterior
                self.responder(500, {"error": "No se pudieron guardar los datos"})
                return

            self.responder(200, {
                "mensaje": "Datos guardados correctamente",
                "humedad": estado["humedad"],
                "riego": estado["riego"]
            })

        except (ValueError, json.JSONDecodeError, UnicodeDecodeError):
            self.responder(400, {"error": "Datos JSON inválidos"})


servidor = HTTPServer(("127.0.0.1", 8000), ServidorRiego)

print("Servidor de Riego Inteligente iniciado en el puerto 8000.")
print("Se requiere clave API para acceder a los datos.")
print("Presiona Ctrl+C para detenerlo.")

try:
    servidor.serve_forever()
except KeyboardInterrupt:
    print("\nServidor detenido.")
finally:
    servidor.server_close()
