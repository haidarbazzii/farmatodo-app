

# 💊 Farmatodo Challenge - Backend API

API RESTful desarrollada en **Java (Spring Boot)** que simula un ecosistema de compras en línea seguro y escalable. El sistema implementa **Arquitectura Hexagonal**, tokenización de tarjetas de crédito (PCI-DSS simulado), gestión de inventario, pasarela de pagos con reintentos y notificaciones transaccionales.

---

## 🏗️ Arquitectura del Sistema (Hexagonal)

El proyecto sigue estrictamente el patrón de **Puertos y Adaptadores**, garantizando que la lógica de negocio permanezca desacoplada de frameworks y bases de datos.

graph TD
    Client[Cliente / Postman] -->|HTTP REST| InAdapter[Adapter: Controllers]
    
    subgraph "Core del Dominio"
        InAdapter --> InPort[Input Ports (Use Cases)]
        InPort --> Service[Application Services]
        Service --> Domain[Domain Entities]
        Service --> OutPort[Output Ports]
    end
    
    subgraph "Infraestructura"
        OutPort --> DBAdapter[Adapter: JPA Repository]
        OutPort --> EmailAdapter[Adapter: Mailjet Client]
        OutPort --> LogAdapter[Adapter: Transaction Log]
    end
    
    DBAdapter --> Database[(PostgreSQL)]
    EmailAdapter --> Mailjet[Mailjet API]


## 🚀 Características Principales

* **🔒 Tokenización PCI-DSS (Simulado):** Encriptación de datos sensibles (PAN, CVV) con algoritmo **AES** antes de persistir en base de datos.
* **🛒 Carrito de Compras:** Gestión completa de estado, validación de stock en tiempo real y conversión a Orden.
* **⚡ Búsqueda Asíncrona:** Registro de historial de búsquedas en hilo separado (`@Async`) para optimizar tiempos de respuesta.
* **🔄 Resiliencia:** Pasarela de pagos simulada con probabilidad de fallo configurable y sistema de **reintentos automáticos (Backoff)**.
* **📧 Notificaciones:** Envío de correos electrónicos transaccionales (integración con **Mailjet**).
* **⚙️ Configuración Dinámica:** Endpoints administrativos para modificar reglas de negocio en tiempo real (probabilidades de fallo, stocks mínimos, reintentos).

---

## 🛠️ Tecnologías

* **Lenguaje:** Java 25 (Preview Features Enabled)
* **Framework:** Spring Boot 3.4.x.
* **Servidor Web:** Se utilizo Render como servicio de hosting para la aplicacion web y la base de datos en la nube.
* **Base de Datos:** PostgreSQL 16.
* **Containerización:** Docker & Docker Compose
* **Email Provider:** Mailjet Client 5.2.5
* **Seguridad:** API Key Filter & AES Encryption
* **Testing:** JUnit 5, Mockito

---

## ⚙️ Configuración (Variables de Entorno)

Para ejecutar el proyecto, es **obligatorio** configurar las siguientes variables de entorno.
Crea un archivo `.env` en la raíz del proyecto o configúralas en tu entorno de despliegue.

```properties
# --- Base de Datos ---
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/farmatodo_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# --- Seguridad ---
# Header requerido en las peticiones: X-API-KEY
API_KEY_SECRET=farmatodo-secret-key-2025
# Clave AES de 16 caracteres EXACTOS para encriptación de tarjetas
APP_ENCRYPTION_KEY=****************

# --- Email (Mailjet) ---
# Necesario para el envío de notificaciones
MAILJET_API_KEY=tu_public_key_mailjet
MAILJET_SECRET_KEY=tu_secret_key_mailjet
MAIL_FROM_MAIL=tu_correo_verificado@gmail.com

```

---

## 🐳 Ejecución con Docker

La forma más rápida de levantar la API y la Base de Datos es usando Docker Compose.

1. Clona el repositorio.
2. Crea el archivo `.env` con las variables mencionadas arriba.
3. Ejecuta:

```bash
docker-compose up --build

```

La API estará disponible en: `https://farmatodo-challenge.onrender.com`
Se pueden hacer peticiones a traves de la siguiente coleccion de Postman: https://haidarbazzi4-674388.postman.co/workspace/Haidar-Bazzi's-Workspace~fc17f60b-f1c3-4e0c-84a0-a9c829b8cacd/collection/50959502-b7b7a243-5e31-4aec-943a-948dff6b7a36?action=share&creator=50959502&active-environment=50959502-7618e041-9884-4671-b60f-aacbfd91af92

---

## 📖 Documentación de la API

🔐 **Autenticación:** Todos los endpoints (excepto `/ping`) requieren el siguiente header:

* **Key:** `X-API-KEY`
* **Value:** `farmatodo-secret-key-2025` (o el valor que configures en `API_KEY_SECRET`)

### 1. Tokenización (Crear Token de Tarjeta)

**POST** `/api/v1/tokens`
El numero de tarjeta (pan) debe ser de 16 digitos de longitud, mientras que el cvv debe ser de 3 digitos para ser valido.
Al realizar a tokenizacion, el token obtenido debe ser guardado por el usuario para realizar compras y es valido por 20 minutos.

```json
{
  "pan": "4111222233334444",
  "cvv": "123",
  "expDate": "12/28"
}

```

### 2. Agregar al Carrito

**POST** `/api/v1/carts/items`
Este endpoint crea el carrito o añade productos al mismo. Se debe utilizar el ID (productId) para añadirlo al carrito.
Se puede consultar el ID de un producto utilizando el endpoint de busquedas.

```json
{
  "email": "cliente@email.com",
  "productId": 1,
  "quantity": 2
}

```

### 3. Checkout (Finalizar Compra)

**POST** `/api/v1/orders/checkout`
Procesa el carrito activo usando el token generado previamente.

```json
{
  "email": "cliente@email.com",
  "cardTokenId": "uuid-del-token-aqui"
}

```

### 4. Búsqueda de Productos

**GET** `/api/v1/products/search?query=acetaminofen`

* Busca productos por nombre.
* Registra la búsqueda asíncronamente en el historial.

**GET** `/api/v1/products`
* Devuelve una lista de productos (devuelve un maximo de 30)

### 5. Administración (Configuración en Caliente)

Modifica las reglas de negocio sin reiniciar el servidor.

* **GET** `/api/v1/admin/config` -> Ver configuración actual.
* **POST** `/api/v1/admin/config/rejection-probability?value=0.5` -> Ajustar la probabilidad de fallo de pago (0.0 - 1.0).
* **POST** `/api/v1/admin/config/max-retries?value=5` -> Ajustar reintentos de pago.
* **POST** `/api/v1/admin/config/min-stock-display?value=3` -> Ajustar el minimo de stock que debe tener un producto para ser mostrado
* **POST** `/api/v1/admin/config/token-rejection-probability?value=0.2` -> Ajustar la probabilidad de que sea rechazada la tokenizacion de una tarjeta (0.0 - 1.0)

---

## 🧪 Ejecutar Pruebas

El proyecto incluye pruebas unitarias para los servicios principales en la clase OrderServiceTest.java validando lógica de negocio, mocks y manejo de excepciones.

```bash
./mvnw clean test

```

---

## 🤖 Uso de IA (Declaración de Transparencia)

Se declara el uso de asistentes de Inteligencia Artificial Generativa durante el desarrollo de esta solución:

* **Herramientas:** Gemini 3.0.
* **Áreas de Apoyo:**
1. **Refactorización Hexagonal:** Asistencia en la separación estricta de capas (Domain vs Infrastructure) y definición de puertos.
2. **Configuración de Docker:** Optimización del `Dockerfile` Multi-Stage para Java 25.
3. **Integración de Email:** Solución a bloqueos de puertos SMTP regionales mediante la implementación de la API HTTP de Mailjet.
4. **Generación de Código:** Creación de esqueletos para Tests Unitarios con Mockito, ayuda con estructura y lógica de negocio.

Dado que el gemini utilizado proviene de una cuenta corporativa, no puede compartirse la conversación. Sin embargo, en el siguiente



```

```
