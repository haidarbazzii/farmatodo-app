

# 💊 Farmatodo Challenge - Backend API

API RESTful desarrollada en **Java (Spring Boot)** que simula un ecosistema de compras en línea seguro y escalable. El sistema implementa **Arquitectura Hexagonal**, tokenización de tarjetas de crédito (PCI-DSS simulado), gestión de inventario, pasarela de pagos con reintentos y notificaciones transaccionales.

---

## 🏗️ Arquitectura del Sistema (Hexagonal)

El proyecto sigue estrictamente el patrón de **Puertos y Adaptadores**, garantizando que la lógica de negocio permanezca desacoplada de frameworks y bases de datos.

Flujo de peticiones:
    
Cliente --> Postman --> Render --> Docker --> Github --> De vuelta al cliente

Estructura General del proyecto:
    
    Dominio
        Modelo --> Componentes (Modelado) del Proyecto
        Puertos
            In (Input Ports) --> Casos de uso
            Out (Output Ports) --> Puertos de Salida (Repositorios)
    
    Infraestructura
        Adapter --> Adaptadores (Conexion con los puertos)
        Controller --> Conexion externa con la API y el Cliente
        Persistence --> Persistencia en Base de Datos
            Entity --> Modelado de Entidades (Tablas) en la BD
            Repository --> Representacion de almacenamiento (repositorio) de datos
        Exception --> Para manejar excepciones
        Security --> Filtro de Autencticacion de la API

    Application
        Service --> Servicios de conexion entre Infraestructura y Aplicacion
        

## 🚀 Características Principales

* **🔒 Tokenización PCI-DSS (Simulado):** Encriptación de datos sensibles (PAN, CVV) con algoritmo **AES** antes de persistir en base de datos.
* **🛒 Carrito de Compras:** Gestión completa de estado, validación de stock en tiempo real y conversión a Orden.
* **⚡ Búsqueda Asíncrona:** Registro de historial de búsquedas en hilo separado (`@Async`) para optimizar tiempos de respuesta.
* **🔄 Resiliencia:** Pasarela de pagos simulada con probabilidad de fallo configurable y sistema de **reintentos automáticos (Backoff)**.
* **📧 Notificaciones:** Envío de correos electrónicos transaccionales (integración con **Mailjet**). NOTA: cuando se realicen pruebas, revisar el SPAM del correo.
* **⚙️ Configuración Dinámica:** Endpoints administrativos para modificar reglas de negocio en tiempo real (probabilidades de fallo, stocks mínimos, reintentos).

---

## 🛠️ Tecnologías

* **Lenguaje:** Java 25 (Preview Features Enabled)
* **Framework:** Spring Boot 4.0.0
* **Servidor Web:** Se utilizo Render como servicio de hosting para la aplicacion web y la base de datos en la nube.
* **Base de Datos:** PostgreSQL 16 en BD de Render.
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
La imagen de Docker, la cual se conecta y actuliza la nube en Render, se encuentra publicada en Docker Hub: https://hub.docker.com/r/haidarb4/farmatodo-challenge

---
## Despliegue en la Nube
Dado que no se cuenta con una suscripcion a GCP, se decidio utilizar el proveedor en la nube Render de sencilla implementacion. A continuacion se muestran los pasos para su despliegue:

### Desplegar la Base de Datos (PostgreSQL)

* En el Dashboard de Render, haz clic en New + y selecciona PostgreSQL.
* Name: El que prefieras (farmatodo-db en este caso).
* Region: Elige la misma región donde desplegarás la API (ej: Ohio).
* Plan: Selecciona el plan que requieras.

Dale a Create Database. Una vez creada, copia el valor de "Internal Connection URL".

### Desplegar el Servidor Web (Spring Boot)

* En el Dashboard, haz clic en New + y selecciona Web Service.
* Conecta tu contenedor/repositorio de docker (farmatodo-challenge).
* Configura los detalles básicos:
    * Name: farmatodo-api
    * Region: La misma que tu base de datos.

### 3. Configurar Variables de Entorno (Environment)

Antes de darle a "Deploy", baja a la sección Environment Variables y agrega las siguientes claves. Render inyectará estos valores en tu contenedor, sobrescribiendo el application.properties.
* SPRING_DATASOURCE_URL: la Internal Connection URL del paso 1. Cambia el inicio de la URL: postgres:// por jdbc:postgresql://.
* SPRING_DATASOURCE_USERNAME: Usuario de la DB.
* SPRING_DATASOURCE_PASSWORD: Contraseña de la DB.
* API_KEY_SECRET: clave secreta para el Header de la API.
* APP_ENCRYPTION_KEY: Deben ser 16 digitos exactos.
* MAILJET_API_KEY: Tu Public Key de Mailjet.
* MAILJET_SECRET_KEY: Tu Secret Key de Mailjet.
* MAIL_FROM_MAIL: Tu correo verificado en Mailjet.

### Finalizar y Probar

* Haz clic en Create Web Service.
* Espera a que termine el proceso de Build (descargará la imagen de Java 25 y compilará con Maven).
* Cuando veas "Your service is live", copia la URL que te da Render (ej: https://farmatodo-api.onrender.com).
* Prueba de humo: Abre https://tu-url.onrender.com/ping en el navegador. Deberías ver pong.

Nota sobre la conexión JDBC: Render te da la URL como postgres://.... Spring Boot necesita jdbc:postgresql://.... Si tu URL interna es: postgres://usuario:password@dpg-xxx-a/farmatodo_db Tu variable SPRING_DATASOURCE_URL debe ser: jdbc:postgresql://dpg-xxx-a:5432/farmatodo_db (Asegúrate de quitar el usuario/pass de la URL si los pones en las variables separadas USERNAME y PASSWORD).

---
## 📖 Documentación de la API
Se accede mediante una coleccion en Postman. Al no disponer de una suscripcion de Postman, para proteger la API y su acceso, la coleccion de Postman fue compartida junto a sus variables de entorno al correo de quien envio esta prueba tecnica. Antes de correrla, se debe seleccionar Env-Variables como el entorno utilizado y se pueden correr pruebas en cada una de las pestañas visibles en la coleccion, cambiando a conveniencia los parametros de la URL y el cuerpo JSON de las peticiones.

🔐 **Autenticación:** Todos los endpoints (excepto `/ping`) requieren el siguiente header:

* **Key:** `X-API-KEY`
* **Value:** `**********` (el valor que configures en `API_KEY_SECRET`)

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

### 4. Búsqueda de Productos e Historial

**GET** `/api/v1/products/search?query=acetaminofen`

* Busca productos por nombre.
* Registra la búsqueda asíncronamente en el historial.

**GET** `/api/v1/products`
* Devuelve una lista de productos (devuelve un maximo de 30)

**GET** `/api/v1/products/history`
* Devuelve una lista busquedas de productos pasada (devuelve un maximo de 20)

### 5. Administración (Configuración en Caliente)

Modifica las reglas de negocio sin reiniciar el servidor.

* **GET** `/api/v1/admin/config` -> Ver configuración actual.
* **POST** `/api/v1/admin/config/rejection-probability?value=0.5` -> Ajustar la probabilidad de fallo de pago (0.0 - 1.0).
* **POST** `/api/v1/admin/config/max-retries?value=5` -> Ajustar reintentos de pago.
* **POST** `/api/v1/admin/config/min-stock-display?value=3` -> Ajustar el minimo de stock que debe tener un producto para ser mostrado
* **POST** `/api/v1/admin/config/token-rejection-probability?value=0.2` -> Ajustar la probabilidad de que sea rechazada la tokenizacion de una tarjeta (0.0 - 1.0)
* **POST** `/api/v1/admin/config/products/{id}/restock` -> Aumenta el stock segun el ID del producto ubicado en el URL de la peticion

### 6. Logs de Transacciones

* **GET** `/api/v1/audit` -> Obtener log de ultimas transacciones (devuelve un maximo de 30)
* **GET** `/api/v1/audit/{transactionId}` -> Obtener log de una transaccion segun su ID
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
