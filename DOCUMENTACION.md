# Documentación completa del trabajo

## Sistema de Gestión de Estudiantes

| Dato | Valor |
|---|---|
| Proyecto | Sistema de Gestión de Estudiantes |
| Versión | 1.0.0 |
| Tipo | Aplicación web Jakarta EE |
| Fecha de documentación | 10 de noviembre de 2026 |
| Entorno de verificación | Windows, XAMPP MariaDB 10.4.32, Payara Server 6.2025.11, JDK 21 |
| Estado | Implementado, compilado, desplegado y probado contra MariaDB real |

---

## Índice

1. [Resumen ejecutivo](#1-resumen-ejecutivo)
2. [Objetivos](#2-objetivos)
3. [Alcance y restricciones](#3-alcance-y-restricciones)
4. [Tecnologías y herramientas](#4-tecnologías-y-herramientas)
5. [Arquitectura del sistema](#5-arquitectura-del-sistema)
6. [Base de datos](#6-base-de-datos)
7. [Configuración de Payara Server](#7-configuración-de-payara-server)
8. [Desarrollo de la aplicación](#8-desarrollo-de-la-aplicación)
9. [Funcionalidades](#9-funcionalidades)
10. [Manual de usuario](#10-manual-de-usuario)
11. [Manual de instalación y despliegue](#11-manual-de-instalación-y-despliegue)
12. [Pruebas y evidencias](#12-pruebas-y-evidencias)
13. [Problemas encontrados y soluciones](#13-problemas-encontrados-y-soluciones)
14. [Seguridad](#14-seguridad)
15. [Limitaciones y mejoras futuras](#15-limitaciones-y-mejoras-futuras)
16. [Conclusiones](#16-conclusiones)
17. [Anexos](#17-anexos)

---

## 1. Resumen ejecutivo

Se desarrolló una aplicación web completa para la gestión académica de estudiantes, utilizando exclusivamente las tecnologías solicitadas: **Java 21**, **Maven**, **Jakarta Faces**, **PrimeFaces**, **MariaDB** (XAMPP/phpMyAdmin), **JDBC**, **Payara Server** y un **JDBC Connection Pool** expuesto por **JNDI**.

La aplicación permite registrar, listar, buscar, editar y eliminar estudiantes, con validaciones de datos en servidor, confirmación de eliminación, mensajes de éxito/error y un dashboard con indicadores reales. La persistencia se implementó con JDBC puro y `PreparedStatement`, cerrando conexiones, sentencias y `ResultSet` con `try-with-resources`. Las credenciales de base de datos no están en el código: se administran en el pool de Payara.

El sistema fue compilado, desplegado y probado contra una instancia real de MariaDB y Payara. No se simularon conexiones ni resultados. Los registros creados durante las pruebas se eliminaron al finalizar; la base queda lista para registrar estudiantes reales.

---

## 2. Objetivos

### 2.1 Objetivo general

Desarrollar un sistema web funcional para la gestión de estudiantes, con CRUD completo, persistencia real en MariaDB mediante JDBC y despliegue en Payara Server usando un pool de conexiones JNDI, cumpliendo las restricciones tecnológicas del proyecto.

### 2.2 Objetivos específicos

- Crear la base de datos `gestion_estudiantes` y la tabla `estudiantes` con restricciones de integridad.
- Implementar el modelo de datos y las validaciones con Jakarta Bean Validation.
- Implementar el acceso a datos con JDBC y `PreparedStatement`.
- Configurar un `DataSource` JNDI en Payara y un pool de conexiones hacia MariaDB.
- Desarrollar una interfaz web profesional, responsive y fácil de usar con Jakarta Faces y PrimeFaces.
- Implementar dashboard, navegación, tabla, diálogos de alta/edición, confirmación de eliminación y mensajes.
- Compilar el proyecto con Maven y generar un WAR desplegable.
- Verificar el funcionamiento real de todas las operaciones contra MariaDB.
- Documentar la instalación, configuración, uso, pruebas y mantenimiento.

---

## 3. Alcance y restricciones

### 3.1 Incluido en el alcance

- CRUD completo de estudiantes.
- Búsqueda por nombres, apellidos o DNI.
- Validaciones de obligatoriedad, formato y unicidad.
- Dashboard con métricas reales.
- Interfaz responsive con menú lateral.
- Persistencia JDBC con pool JNDI.
- Script SQL de creación de base y tabla.
- Script de PowerShell para configurar Payara.
- Documentación técnica y manual de usuario.

### 3.2 Fuera del alcance

- Autenticación y autorización de usuarios.
- Roles o permisos por usuario.
- Auditoría de cambios.
- Exportación de datos a CSV/PDF.
- Pruebas automatizadas con JUnit/Arquillian/Testcontainers.
- Aplicación móvil nativa.
- Integración con servicios externos.

### 3.3 Restricciones tecnológicas respetadas

- **No** se usó Spring Boot.
- **No** se usó Hibernate ni JPA.
- **No** se usó React, Angular ni Vue.
- La persistencia es JDBC directo con `PreparedStatement`.
- El servidor de aplicaciones es Payara Server 6.
- La base de datos es MariaDB mediante XAMPP.
- La administración de la base se realizó con phpMyAdmin y consola MySQL.
- El IDE recomendado es Visual Studio Code.

---

## 4. Tecnologías y herramientas

| Componente | Versión / uso |
|---|---|
| Java | JDK 21 (Temurin 21.0.13+9 LTS) |
| Jakarta EE | 10.0 Web Profile |
| Jakarta Faces | 4.0, incluido en Payara |
| PrimeFaces | 14.0.18, clasificador `jakarta` |
| Tema PrimeFaces | Saga |
| MariaDB | 10.4.32 (XAMPP) |
| MariaDB Connector/J | 3.5.11 |
| Payara Server Community | 6.2025.11 |
| Maven | 3.9.9 mediante Maven Wrapper |
| XAMPP | Apache + MariaDB + phpMyAdmin |
| Visual Studio Code | Editor recomendado |
| Navegador | Chrome/Edge moderno |
| Sistema operativo de verificación | Windows |
| Terminal | PowerShell |

### 4.1 Dependencias principales de Maven

- `jakarta.platform:jakarta.jakartaee-web-api:10.0.0` con scope `provided`.
- `org.primefaces:primefaces:14.0.18` con clasificador `jakarta`.
- `org.mariadb.jdbc:mariadb-java-client:3.5.11` con scope `provided`.

### 4.2 Plugins de Maven

- `maven-compiler-plugin` 3.13.0, release 21, encoding UTF-8, parámetros.
- `maven-war-plugin` 3.4.0.
- `maven-surefire-plugin` 3.5.2.
- `maven-enforcer-plugin` 3.5.0, exige Java 21 o superior.
- `maven-dependency-plugin` 3.8.1, copia el driver a `target/payara-lib`.

---

## 5. Arquitectura del sistema

### 5.1 Vista general

```text
┌─────────────────────────────────────────────────────────────┐
│ Navegador web                                               │
│ XHTML + PrimeFaces + CSS/JS                                 │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / AJAX
┌──────────────────────────────▼──────────────────────────────┐
│ Payara Server 6                                             │
│                                                             │
│  Jakarta Faces                                              │
│       │                                                     │
│       ▼                                                     │
│  EstudianteBean / DashboardBean  (@Named, CDI)              │
│       │                                                     │
│       ▼                                                     │
│  EstudianteDAO  (JDBC, PreparedStatement)                   │
│       │                                                     │
│       ▼                                                     │
│  DatabaseConfig  (@Resource JNDI)                           │
│       │                                                     │
│       ▼                                                     │
│  JDBC Connection Pool: GestionEstudiantesPool               │
│       │                                                     │
└───────┼─────────────────────────────────────────────────────┘
        │ JDBC
┌───────▼─────────────────────────────────────────────────────┐
│ MariaDB 10.4 (XAMPP)                                        │
│ Base: gestion_estudiantes                                   │
│ Tabla: estudiantes                                          │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Capas y responsabilidades

| Capa | Componentes | Responsabilidad |
|---|---|---|
| Presentación | `layout.xhtml`, `dashboard.xhtml`, `estudiantes.xhtml`, `styles.css`, `app.js` | Interfaz, navegación, tabla, formularios, mensajes y estilos. |
| Controlador | `EstudianteBean`, `DashboardBean` | Estado de la vista, acciones CRUD, búsqueda, mensajes y coordinación. |
| Modelo | `Estudiante` | Datos del estudiante y validaciones Bean Validation. |
| Persistencia | `EstudianteDAO` | SQL JDBC con `PreparedStatement`, mapeo de resultados y cierre de recursos. |
| Configuración | `DatabaseConfig`, `web.xml` | Obtención del `DataSource` JNDI y referencia al recurso. |
| Infraestructura | Payara, pool JDBC, MariaDB | Servidor de aplicaciones, conexiones y almacenamiento. |

### 5.3 Flujo de una operación CRUD

Ejemplo: registrar un estudiante.

1. El usuario abre el diálogo y completa el formulario.
2. PrimeFaces envía la petición AJAX a Jakarta Faces.
3. JSF aplica conversión y validación (requeridos, formatos, Bean Validation).
4. `EstudianteBean.guardar()` normaliza los datos.
5. El bean verifica duplicados de DNI y correo con `EstudianteDAO`.
6. Si no hay duplicados, `EstudianteDAO.insertar()` ejecuta el `INSERT` con `PreparedStatement`.
7. La conexión se obtiene del pool JNDI y se devuelve al cerrarla.
8. El bean agrega un `FacesMessage` de éxito, recarga la lista y oculta el diálogo.
9. PrimeFaces actualiza la tabla, el contador y el growl.

### 5.4 Estructura de paquetes

```text
com.gestionestudiantes
├── config
│   └── DatabaseConfig
├── controller
│   ├── DashboardBean
│   └── EstudianteBean
├── dao
│   └── EstudianteDAO
└── model
    └── Estudiante
```

### 5.5 Estructura de archivos del proyecto

```text
sistema-gestion-estudiantes/
├── .mvn/wrapper/maven-wrapper.properties
├── .vscode/
│   ├── extensions.json
│   └── settings.json
├── scripts/
│   └── configurar-payara.ps1
├── src/main/java/com/gestionestudiantes/
│   ├── config/DatabaseConfig.java
│   ├── controller/DashboardBean.java
│   ├── controller/EstudianteBean.java
│   ├── dao/EstudianteDAO.java
│   └── model/Estudiante.java
├── src/main/webapp/
│   ├── WEB-INF/
│   │   ├── beans.xml
│   │   ├── faces-config.xml
│   │   ├── templates/layout.xhtml
│   │   └── web.xml
│   ├── resources/css/styles.css
│   ├── resources/js/app.js
│   ├── views/dashboard.xhtml
│   ├── views/estudiantes.xhtml
│   └── index.xhtml
├── database.sql
├── DOCUMENTACION.md
├── README.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---

## 6. Base de datos

### 6.1 Modelo de datos

Tabla `estudiantes`:

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | `INT` | `NOT NULL`, `AUTO_INCREMENT`, `PRIMARY KEY` |
| `nombres` | `VARCHAR(100)` | `NOT NULL` |
| `apellidos` | `VARCHAR(100)` | `NOT NULL` |
| `dni` | `VARCHAR(8)` | `NOT NULL`, `UNIQUE`, `CHECK` de 8 dígitos |
| `correo` | `VARCHAR(120)` | `NOT NULL`, `UNIQUE` |
| `telefono` | `VARCHAR(15)` | `NOT NULL` |
| `fecha_registro` | `TIMESTAMP` | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` |

Índices:

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_estudiantes_dni (dni)`
- `UNIQUE KEY uk_estudiantes_correo (correo)`
- `INDEX idx_estudiantes_apellidos_nombres (apellidos, nombres)`
- `INDEX idx_estudiantes_fecha_registro (fecha_registro)`

Restricción de formato:

```sql
CONSTRAINT chk_estudiantes_dni_formato CHECK (dni REGEXP '^[0-9]{8}$')
```

### 6.2 Script de creación

El archivo `database.sql` contiene:

```sql
CREATE DATABASE IF NOT EXISTS gestion_estudiantes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gestion_estudiantes;

CREATE TABLE IF NOT EXISTS estudiantes (
    id             INT             NOT NULL AUTO_INCREMENT,
    nombres        VARCHAR(100)    NOT NULL,
    apellidos      VARCHAR(100)    NOT NULL,
    dni            VARCHAR(8)      NOT NULL,
    correo         VARCHAR(120)    NOT NULL,
    telefono       VARCHAR(15)     NOT NULL,
    fecha_registro TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_estudiantes_dni UNIQUE (dni),
    CONSTRAINT uk_estudiantes_correo UNIQUE (correo),
    CONSTRAINT chk_estudiantes_dni_formato CHECK (dni REGEXP '^[0-9]{8}$'),
    INDEX idx_estudiantes_apellidos_nombres (apellidos, nombres),
    INDEX idx_estudiantes_fecha_registro (fecha_registro)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

### 6.3 Usuario de aplicación

Se creó un usuario con permisos mínimos, sin usar `root`:

```sql
CREATE USER 'gestion_user'@'localhost' IDENTIFIED BY '<TU_CLAVE_SEGURA>';
CREATE USER 'gestion_user'@'127.0.0.1' IDENTIFIED BY '<TU_CLAVE_SEGURA>';

GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_estudiantes.* TO 'gestion_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_estudiantes.* TO 'gestion_user'@'127.0.0.1';

FLUSH PRIVILEGES;
```

Se crearon ambos hosts porque el pool se conecta por TCP a `127.0.0.1`. El usuario no tiene permisos `ALTER`, `DROP`, `CREATE` ni acceso a otras bases. Esto se comprobó durante la limpieza final: al intentar `ALTER TABLE ... AUTO_INCREMENT` con `gestion_user`, MariaDB respondió `ALTER command denied`; la limpieza se realizó con `root`. Este comportamiento confirma el principio de mínimo privilegio.

### 6.4 Verificación de la estructura

Se ejecutó:

```sql
SHOW CREATE TABLE estudiantes;
```

Resultado relevante:

```text
CREATE TABLE `estudiantes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombres` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `dni` varchar(8) NOT NULL,
  `correo` varchar(120) NOT NULL,
  `telefono` varchar(15) NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_estudiantes_dni` (`dni`),
  UNIQUE KEY `uk_estudiantes_correo` (`correo`),
  KEY `idx_estudiantes_apellidos_nombres` (`apellidos`,`nombres`),
  KEY `idx_estudiantes_fecha_registro` (`fecha_registro`),
  CONSTRAINT `chk_estudiantes_dni_formato` CHECK (`dni` regexp '^[0-9]{8}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```

---

## 7. Configuración de Payara Server

### 7.1 Instalación y arranque

Payara Server Community 6.2025.11 se descomprimió en una carpeta local. El dominio por defecto es `domain1`.

```powershell
& "C:\payara6\bin\asadmin.bat" version
& "C:\payara6\bin\asadmin.bat" start-domain
```

- Aplicación: <http://localhost:8080/gestion-estudiantes/>
- Administración: <http://localhost:4848>

### 7.2 Driver MariaDB

El WAR declara el driver como `provided`, por lo que Payara debe tenerlo en el classpath del dominio:

```powershell
.\mvnw.cmd clean package

Copy-Item .\target\payara-lib\mariadb-java-client.jar `
  C:\payara6\glassfish\domains\domain1\lib\ -Force

& "C:\payara6\bin\asadmin.bat" restart-domain
```

Ruta final del driver:

```text
C:\payara6\glassfish\domains\domain1\lib\mariadb-java-client.jar
```

### 7.3 JDBC Connection Pool

Nombre: `GestionEstudiantesPool`

Comando usado:

```powershell
& "C:\payara6\bin\asadmin.bat" create-jdbc-connection-pool `
  --datasourceclassname org.mariadb.jdbc.MariaDbDataSource `
  --restype javax.sql.DataSource `
  --property "url=jdbc\:mariadb\://127.0.0.1\:3306/gestion_estudiantes:user=gestion_user:password=<TU_CLAVE_SEGURA>" `
  GestionEstudiantesPool
```

Características:

- `datasourceclassname`: `org.mariadb.jdbc.MariaDbDataSource`
- `restype`: `javax.sql.DataSource`
- URL: `jdbc:mariadb://127.0.0.1:3306/gestion_estudiantes`
- Usuario: `gestion_user`
- Contraseña: administrada por Payara, no incluida en el código.

Notas:

- Los `:` de la URL se escaparon con `\:` porque `asadmin` los usa como separador de propiedades.
- No se agregaron parámetros con `&` porque `asadmin.bat` en Windows puede interpretarlos como separadores de comandos. MariaDB Connector/J 3.x usa UTF-8/utf8mb4 por defecto.
- Para contraseñas con caracteres especiales, es preferible usar la consola de administración de Payara.

### 7.4 Recurso JNDI

Nombre: `jdbc/GestionEstudiantes`

```powershell
& "C:\payara6\bin\asadmin.bat" create-jdbc-resource `
  --connectionpoolid GestionEstudiantesPool `
  jdbc/GestionEstudiantes
```

La referencia se declara en `web.xml`:

```xml
<resource-ref>
    <res-ref-name>jdbc/GestionEstudiantes</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
    <lookup-name>jdbc/GestionEstudiantes</lookup-name>
</resource-ref>
```

`DatabaseConfig` inyecta el recurso:

```java
@Resource(lookup = "jdbc/GestionEstudiantes")
private DataSource dataSource;
```

### 7.5 Verificación

```powershell
& "C:\payara6\bin\asadmin.bat" ping-connection-pool GestionEstudiantesPool
& "C:\payara6\bin\asadmin.bat" list-jdbc-connection-pools
& "C:\payara6\bin\asadmin.bat" list-jdbc-resources
```

Resultado real obtenido:

```text
Command ping-connection-pool executed successfully.
```

Listado de recursos JNDI:

```text
jdbc/__TimerPool
jdbc/__default
jdbc/GestionEstudiantes
```

### 7.6 Script de configuración

Se creó `scripts/configurar-payara.ps1` para automatizar:

- Verificación de que Payara está en ejecución.
- Copia del driver si no existe en `domain1/lib`.
- Eliminación y recreación del pool y del recurso JNDI.
- `ping-connection-pool`.
- Despliegue opcional del WAR con `-Deploy`.
- Solicitud segura de contraseña si no se pasa `-DbPassword`.
- Redacción de la contraseña en mensajes de error.

Uso:

```powershell
.\scripts\configurar-payara.ps1 -PayaraHome "C:\payara6" -Deploy
```

El script fue probado: recreó el pool y el recurso, el `ping` fue exitoso y la aplicación se redesplegó y siguió consultando MariaDB correctamente.

---

## 8. Desarrollo de la aplicación

### 8.1 Modelo `Estudiante`

Archivo: `src/main/java/com/gestionestudiantes/model/Estudiante.java`

Responsabilidades:

- Representar un estudiante.
- Almacenar `id`, `nombres`, `apellidos`, `dni`, `correo`, `telefono` y `fechaRegistro`.
- Aplicar Bean Validation.
- Calcular nombre completo e iniciales para la interfaz.
- Implementar `equals`, `hashCode` y `toString`.

Validaciones:

```java
@NotBlank(message = "Los nombres son obligatorios.")
@Size(max = 100, ...)
private String nombres;

@NotBlank(message = "Los apellidos son obligatorios.")
@Size(max = 100, ...)
private String apellidos;

@NotBlank(message = "El DNI es obligatorio.")
@Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener exactamente 8 dígitos.")
private String dni;

@NotBlank(message = "El correo electrónico es obligatorio.")
@Email(message = "Ingrese un correo electrónico válido.")
@Size(max = 120, ...)
private String correo;

@NotBlank(message = "El teléfono es obligatorio.")
@Size(max = 15, ...)
@Pattern(regexp = "^[0-9+()\\-\\s]{6,15}$", ...)
private String telefono;
```

### 8.2 Configuración JNDI `DatabaseConfig`

Archivo: `src/main/java/com/gestionestudiantes/config/DatabaseConfig.java`

- Es un bean CDI `@ApplicationScoped`.
- Inyecta el `DataSource` con `@Resource(lookup = "jdbc/GestionEstudiantes")`.
- Expone `getConnection()`.
- Si el `DataSource` no fue inyectado, lanza un `SQLException` con un mensaje explicativo.
- No contiene credenciales.

### 8.3 DAO `EstudianteDAO`

Archivo: `src/main/java/com/gestionestudiantes/dao/EstudianteDAO.java`

Es un bean `@ApplicationScoped` que usa JDBC puro. Métodos:

| Método | Descripción |
|---|---|
| `listar()` | Lista todos los estudiantes ordenados por apellidos y nombres. |
| `buscar(String termino)` | Busca por nombres, apellidos o DNI con `LIKE ?`. |
| `listarRecientes(int limite)` | Obtiene los últimos registros para el dashboard. |
| `contar()` | Cuenta el total de estudiantes. |
| `contarNuevosEsteMes()` | Cuenta las altas del mes actual. |
| `existeDni(String dni, Integer idExcluir)` | Verifica duplicados de DNI, excluyendo el registro editado. |
| `existeCorreo(String correo, Integer idExcluir)` | Verifica duplicados de correo, excluyendo el registro editado. |
| `insertar(Estudiante e)` | Inserta con `PreparedStatement` y recupera la clave generada. |
| `actualizar(Estudiante e)` | Actualiza los datos por `id`. |
| `eliminar(int id)` | Elimina por `id`. |

Todas las operaciones usan `try-with-resources`:

```java
try (Connection conexion = databaseConfig.getConnection();
     PreparedStatement sentencia = conexion.prepareStatement(sql);
     ResultSet resultado = sentencia.executeQuery()) {
    // ...
}
```

Esto garantiza que la conexión vuelva al pool y que no queden recursos abiertos.

### 8.4 Controlador `EstudianteBean`

Archivo: `src/main/java/com/gestionestudiantes/controller/EstudianteBean.java`

- `@Named("estudianteBean")`
- `@ViewScoped`
- `Serializable`

Responsabilidades:

- Cargar la lista inicial en `@PostConstruct`.
- Gestionar el filtro de búsqueda.
- Abrir el formulario en modo nuevo o edición.
- Normalizar datos antes de guardar (trim y correo en minúsculas).
- Verificar duplicados de DNI y correo.
- Insertar o actualizar.
- Eliminar con confirmación.
- Recargar la lista respetando el filtro activo.
- Generar mensajes de éxito y error.
- Exponer textos de la interfaz (título del diálogo, etiqueta del botón, contador).

Métodos principales:

| Método | Descripción |
|---|---|
| `init()` | Carga inicial. |
| `cargarTodos()` | Lista todos los estudiantes. |
| `buscar()` | Aplica el filtro. |
| `limpiarFiltro()` | Limpia el filtro y recarga. |
| `nuevo()` | Prepara un estudiante vacío. |
| `editar(Estudiante)` | Copia los datos del estudiante seleccionado al formulario. |
| `guardar()` | Valida duplicados, inserta o actualiza y recarga. |
| `eliminar(Estudiante)` | Elimina y recarga. |
| `getTextoContador()` | Texto singular/plural del contador. |

### 8.5 Controlador `DashboardBean`

Archivo: `src/main/java/com/gestionestudiantes/controller/DashboardBean.java`

- `@Named("dashboardBean")`
- `@ViewScoped`

Consulta:

- Total de estudiantes.
- Nuevos estudiantes del mes.
- Últimos 5 registros.
- Estado de la conexión.
- Último registro y su fecha.

Si ocurre un `SQLException`, marca la conexión como no disponible y agrega un mensaje de error.

### 8.6 Vistas XHTML

| Archivo | Contenido |
|---|---|
| `WEB-INF/templates/layout.xhtml` | Plantilla base: sidebar, topbar, contenido, growl, footer y recursos CSS/JS. |
| `views/dashboard.xhtml` | Hero, tarjetas de indicadores, tabla de últimos registros y banner informativo. |
| `views/estudiantes.xhtml` | Panel de listado, búsqueda, tabla PrimeFaces, diálogo de alta/edición y confirmación de eliminación. |
| `index.xhtml` | Pantalla de carga y redirección al dashboard. |

Componentes PrimeFaces usados:

- `p:dataTable`
- `p:dialog`
- `p:confirmDialog`
- `p:growl`
- `p:message`
- `p:commandButton`
- `p:inputText`
- `p:outputPanel`
- `p:defaultCommand`

### 8.7 Estilos y JavaScript

- `resources/css/styles.css`: variables CSS, layout, sidebar, topbar, tarjetas, tabla, diálogos, formularios, botones, badges, mensajes y responsive.
- `resources/js/app.js`: cierra el sidebar al navegar en móvil, con Escape y al redimensionar.
- Breakpoints responsive: `1240px`, `980px`, `720px` y `480px`.
- Tema base PrimeFaces: Saga.
- Tipografía: `Inter`, `Segoe UI`, system-ui, sans-serif.

### 8.8 Configuración web

`web.xml`:

- `jakarta.faces.PROJECT_STAGE=Development`.
- Tema `saga`.
- `primefaces.FONT_AWESOME=false`.
- Zona horaria del converter por defecto del sistema.
- Faces Servlet mapeado a `*.xhtml`.
- Tipos MIME para `eot`, `ttf`, `woff`, `woff2` y `svg`.
- `welcome-file` `index.xhtml`.
- `resource-ref` JNDI.
- Sesión de 30 minutos, cookie `http-only`, tracking por cookie.

`faces-config.xml`:

- Versión 4.0.
- Locale por defecto `es`.

`beans.xml`:

- Versión 4.0.
- `bean-discovery-mode="annotated"`.

### 8.9 Maven

`pom.xml`:

- Packaging `war`.
- Java release 21.
- Codificación UTF-8.
- Dependencias Jakarta EE, PrimeFaces Jakarta y driver MariaDB.
- Enforcer para exigir Java 21+.
- Copia automática del driver a `target/payara-lib`.
- Nombre final del WAR: `gestion-estudiantes.war`.

---

## 9. Funcionalidades

### 9.1 Dashboard

- Total de estudiantes.
- Nuevos estudiantes del mes actual.
- Último registro y fecha.
- Estado de la conexión a MariaDB.
- Tabla con los últimos 5 estudiantes.
- Enlace directo a la gestión de estudiantes.

### 9.2 Registrar estudiante

- Botón **Nuevo estudiante**.
- Formulario modal con nombres, apellidos, DNI, teléfono y correo.
- Validación en servidor.
- Verificación de DNI y correo duplicados.
- Mensaje de éxito y actualización de tabla/contador.
- Cierre automático del diálogo.

### 9.3 Buscar

- Campo de búsqueda por nombres, apellidos o DNI.
- Búsqueda parcial e insensible a mayúsculas/minúsculas.
- Botón **Buscar** y **Limpiar**.
- Contador de resultados.
- `p:defaultCommand` para buscar con Enter.

### 9.4 Editar

- Icono de lápiz en cada fila.
- Carga de datos actuales en el formulario.
- Título y botón cambian a **Editar estudiante** / **Actualizar datos**.
- Verificación de duplicados excluyendo el propio registro.
- Mensaje de éxito y actualización de la tabla.

### 9.5 Eliminar

- Icono de papelera en cada fila.
- Diálogo de confirmación con el nombre del estudiante.
- **Sí, eliminar** ejecuta la eliminación.
- **Cancelar** conserva el registro.
- Mensaje de éxito y actualización de tabla/contador.

### 9.6 Validaciones

| Campo | Regla |
|---|---|
| Nombres | Obligatorio, máximo 100 caracteres. |
| Apellidos | Obligatorio, máximo 100 caracteres. |
| DNI | Obligatorio, exactamente 8 dígitos, único. |
| Correo | Obligatorio, formato válido, máximo 120 caracteres, único. |
| Teléfono | Obligatorio, entre 6 y 15 caracteres: números, `+`, `-`, paréntesis o espacios. |

### 9.7 Mensajes

- Growl global para éxito y error.
- Mensajes junto a cada campo para errores de validación.
- Mensajes amigables para duplicados.
- Mensajes de error de base de datos con indicación de revisar MariaDB y el pool JNDI.

### 9.8 Diseño responsive

- Sidebar fijo en escritorio.
- Sidebar deslizante en móvil.
- Tabla con scroll horizontal en pantallas pequeñas.
- Tarjetas y formularios adaptables.
- Botones y diálogos responsive.

---

## 10. Manual de usuario

### 10.1 Abrir la aplicación

1. Inicie MySQL en XAMPP.
2. Inicie Payara:
   ```powershell
   & "C:\payara6\bin\asadmin.bat" start-domain
   ```
3. Abra:
   ```text
   http://localhost:8080/gestion-estudiantes/
   ```

### 10.2 Dashboard

Al ingresar se muestra el panel de control con:

- Total de estudiantes.
- Nuevos este mes.
- Último registro.
- Estado de la base de datos.
- Últimos registros.

Si la base no está disponible, el estado cambia y se muestra un mensaje de error.

### 10.3 Registrar un estudiante

1. Vaya a **Estudiantes** en el menú lateral.
2. Pulse **Nuevo estudiante**.
3. Complete los campos.
4. Pulse **Registrar estudiante**.
5. Verifique el mensaje de éxito y la aparición del registro en la tabla.

### 10.4 Buscar

1. Escriba nombres, apellidos o DNI en el campo de búsqueda.
2. Pulse **Buscar** o Enter.
3. Para restablecer, pulse **Limpiar**.

### 10.5 Editar

1. Pulse el icono del lápiz en la fila del estudiante.
2. Modifique los datos.
3. Pulse **Actualizar datos**.
4. Verifique el mensaje y los cambios en la tabla.

### 10.6 Eliminar

1. Pulse el icono de la papelera.
2. Revise el nombre en el diálogo de confirmación.
3. Pulse **Sí, eliminar** para borrar o **Cancelar** para conservar.

### 10.7 Errores comunes del usuario

- **DNI duplicado:** ya existe un estudiante con ese DNI.
- **Correo duplicado:** ya existe un estudiante con ese correo.
- **DNI inválido:** debe tener exactamente 8 dígitos.
- **Correo inválido:** debe tener formato de correo.
- **Campos obligatorios:** complete todos los campos marcados con `*`.

---

## 11. Manual de instalación y despliegue

### 11.1 Requisitos

- JDK 21.
- XAMPP con MariaDB.
- Payara Server Community 6.
- Maven Wrapper incluido.
- Navegador moderno.
- Visual Studio Code (opcional).

### 11.2 Base de datos

1. Inicie MySQL en XAMPP.
2. Abra phpMyAdmin: <http://localhost/phpmyadmin>.
3. Ejecute `database.sql` en la pestaña SQL.
4. Cree el usuario `gestion_user` y otorgue permisos DML.
5. Verifique la tabla.

Desde PowerShell:

```powershell
cmd /c "C:\xampp\mysql\bin\mysql.exe -u root -p < database.sql"
```

### 11.3 Payara

1. Descomprima Payara en `C:\payara6`.
2. Inicie el dominio:
   ```powershell
   & "C:\payara6\bin\asadmin.bat" start-domain
   ```
3. Compile el proyecto para obtener el driver:
   ```powershell
   .\mvnw.cmd clean package
   ```
4. Copie el driver:
   ```powershell
   Copy-Item .\target\payara-lib\mariadb-java-client.jar `
     C:\payara6\glassfish\domains\domain1\lib\ -Force
   ```
5. Reinicie Payara:
   ```powershell
   & "C:\payara6\bin\asadmin.bat" restart-domain
   ```
6. Cree el pool y el recurso JNDI, o ejecute:
   ```powershell
   .\scripts\configurar-payara.ps1 -PayaraHome "C:\payara6"
   ```

### 11.4 Compilación

```powershell
.\mvnw.cmd clean package
```

Resultado:

- `target\gestion-estudiantes.war`
- `target\payara-lib\mariadb-java-client.jar`

### 11.5 Despliegue

```powershell
& "C:\payara6\bin\asadmin.bat" deploy --force .\target\gestion-estudiantes.war
```

También se puede copiar el WAR a:

```text
C:\payara6\glassfish\domains\domain1\autodeploy\
```

### 11.6 Verificación

```powershell
& "C:\payara6\bin\asadmin.bat" list-applications
& "C:\payara6\bin\asadmin.bat" ping-connection-pool GestionEstudiantesPool
```

Abrir:

```text
http://localhost:8080/gestion-estudiantes/
```

### 11.7 Recompilar y redesplegar

```powershell
.\mvnw.cmd clean package
& "C:\payara6\bin\asadmin.bat" deploy --force .\target\gestion-estudiantes.war
```

### 11.8 Detener servicios

```powershell
& "C:\payara6\bin\asadmin.bat" stop-domain
```

MariaDB se detiene desde el Panel de Control de XAMPP.

---

## 12. Pruebas y evidencias

### 12.1 Metodología

Las pruebas se realizaron de forma manual y real:

- Compilación con Maven Wrapper.
- Despliegue con `asadmin`.
- Verificación del pool con `ping-connection-pool`.
- Operaciones CRUD desde la interfaz web.
- Consultas SQL directas a MariaDB para confirmar los cambios.
- Inspección del DOM y de las respuestas AJAX en el navegador.
- Revisión de logs de Payara.

No se usaron mocks, datos simulados ni respuestas falsas.

### 12.2 Matriz de pruebas

| ID | Caso | Datos / acción | Resultado esperado | Resultado real | Estado |
|---|---|---|---|---|---|
| CP-01 | Compilación | `mvnw.cmd clean package` | WAR generado | `BUILD SUCCESS`, 5 clases, WAR generado | ✅ |
| CP-02 | Despliegue | `asadmin deploy --force` | Aplicación desplegada | `Application deployed with name gestion-estudiantes` | ✅ |
| CP-03 | Pool JNDI | `ping-connection-pool` | Conexión a MariaDB | `Command ping-connection-pool executed successfully` | ✅ |
| CP-04 | Alta | María Fernanda García López, DNI 12345678 | Registro creado | Fila en tabla y `SELECT` en MariaDB | ✅ |
| CP-05 | Búsqueda | `García` | 1 resultado | 1 fila, contador `1 estudiante encontrado` | ✅ |
| CP-06 | Edición | Nuevo correo y teléfono | Datos actualizados | Tabla y `SELECT` reflejan cambios | ✅ |
| CP-07 | Requeridos | Formulario vacío | 5 mensajes | 5 mensajes, diálogo abierto | ✅ |
| CP-08 | DNI inválido | `1234` | Error de formato | `El DNI debe contener exactamente 8 dígitos.` | ✅ |
| CP-09 | Correo inválido | `correo-invalido` | Error de formato | `Ingrese un correo electrónico válido.` | ✅ |
| CP-10 | DNI duplicado | DNI 12345678 | Rechazo | Mensaje de DNI duplicado, sin inserción | ✅ |
| CP-11 | Correo duplicado | Correo existente | Rechazo | Mensaje de correo duplicado, sin inserción | ✅ |
| CP-12 | Cancelar eliminación | Cancelar | Registro conservado | Diálogo cerrado, registro presente | ✅ |
| CP-13 | Confirmar eliminación | Sí, eliminar | Registro borrado | Fila eliminada, contador actualizado | ✅ |
| CP-14 | Dashboard | Abrir dashboard | Datos reales | Total, altas, último registro y estado Operativa | ✅ |
| CP-15 | Paginación | 1 y 2 registros | Texto correcto | `Mostrando 1 a 1 de 1` y `1 a 2 de 2` | ✅ |
| CP-16 | Redirección | Abrir URL raíz | Dashboard | Redirección a `views/dashboard.xhtml` | ✅ |
| CP-17 | MIME fonts | Cargar página | Sin JSF1091 nuevos | Sin nuevas advertencias tras redespliegue | ✅ |
| CP-18 | Script Payara | Ejecutar script | Pool/recurso recreados | Ping exitoso y app redeployada | ✅ |
| CP-19 | Limpieza | Eliminar registros de prueba | Tabla vacía | `COUNT(*) = 0`, auto_increment reiniciado | ✅ |

### 12.3 Evidencia de base de datos

Registro creado durante la prueba:

| id | nombres | apellidos | dni | correo | teléfono | fecha_registro |
|---|---|---|---|---|---|---|
| 1 | María Fernanda | García López | 12345678 | maria.garcia@example.com | 987654321 | 2026-11-10 15:53:46 |

Después de la edición:

| id | nombres | apellidos | dni | correo | teléfono | fecha_registro |
|---|---|---|---|---|---|---|
| 1 | María Fernanda | García López | 12345678 | maria.actualizada@example.com | 999-888-777 | 2026-11-10 15:53:46 |

Consulta usada:

```sql
SELECT id, nombres, apellidos, dni, correo, telefono, fecha_registro
FROM estudiantes
ORDER BY id;
```

Al finalizar las pruebas se ejecutó la limpieza:

```sql
DELETE FROM estudiantes;
ALTER TABLE estudiantes AUTO_INCREMENT = 1;
```

Resultado final:

```text
total
0
```

### 12.4 Evidencia de interfaz

Se verificó en el navegador:

- La tabla mostró la fila creada con avatar, nombre, correo, DNI, teléfono y fecha.
- El growl mostró `El estudiante se registró correctamente.`
- La búsqueda por `García` mostró 1 resultado.
- El diálogo de edición se cargó con los datos actuales.
- El diálogo de confirmación mostró el nombre del estudiante.
- El dashboard mostró `Operativa` y los totales reales.
- El paginador mostró el texto correcto.
- La URL raíz redirigió al dashboard.
- La base quedó vacía después de la limpieza y el dashboard mostró `0`.

### 12.5 Evidencia de logs

- No se observaron líneas `SEVERE` ni excepciones en las últimas 200 líneas del log durante las pruebas.
- Las últimas advertencias `JSF1091` de fuentes PrimeIcons ocurrieron antes del redespliegue que agregó los `mime-mapping`; después del redespliegue no se generaron nuevas.
- Payara mostró una advertencia de JDK no listado oficialmente, pero el dominio arrancó con Java 21 y la aplicación funcionó correctamente. La cabecera HTTP `X-Powered-By` confirmó `Payara Server 6.2025.11 Java/Eclipse Adoptium/21`.

### 12.6 Resultado global

Todas las pruebas planificadas finalizaron correctamente. El sistema cumple los requisitos funcionales y técnicos solicitados y quedó desplegado y operativo.

---

## 13. Problemas encontrados y soluciones

### 13.1 `NoClassDefFoundError: javax/servlet/ServletRequestListener`

**Causa:** el artefacto principal de PrimeFaces 14 es para `javax`; Payara 6 usa Jakarta.

**Solución:** agregar el clasificador `jakarta`:

```xml
<dependency>
    <groupId>org.primefaces</groupId>
    <artifactId>primefaces</artifactId>
    <version>14.0.18</version>
    <classifier>jakarta</classifier>
</dependency>
```

Luego `clean package` y redespliegue.

### 13.2 Paginador mostraba `{first}` y `{last}` literales

**Causa:** PrimeFaces 14 usa los placeholders `{startRecord}` y `{endRecord}`; `{first}` y `{last}` ya no se reemplazan.

**Solución:** cambiar la plantilla a:

```xml
currentPageReportTemplate="Mostrando {startRecord} a {endRecord} de {totalRecords}"
```

Verificado con `Mostrando 1 a 1 de 1` y `Mostrando 1 a 2 de 2`.

### 13.3 El contador no se actualizaba al eliminar

**Causa:** el botón de eliminar está dentro de la tabla (`UIData`), que es un `NamingContainer`; el target relativo `contador` no se resolvía.

**Solución:** usar el client ID absoluto:

```xml
update=":formPrincipal:tablaEstudiantes :formPrincipal:contador :growl"
```

Verificado: el contador pasó de `2 estudiantes registrados` a `1 estudiante registrado` tras eliminar.

### 13.4 Mensajes duplicados en el growl

**Causa:** los mensajes de campos requeridos tienen el mismo texto en resumen y detalle; `p:growl` mostraba ambos.

**Solución:** configurar:

```xml
<p:growl id="growl" showSummary="false" showDetail="true" life="5000"/>
```

Verificado: cada error apareció una sola vez.

### 13.5 Concordancia singular/plural

**Causa:** el contador siempre usaba `encontrados` / `registrados`.

**Solución:** ajustar `getTextoContador()` para usar `encontrado` / `registrado` cuando hay 1 estudiante.

### 13.6 Advertencias `JSF1091` por fuentes PrimeIcons

**Causa:** Payara no tenía mapeados los tipos MIME de `.eot`, `.ttf`, `.woff`, `.woff2` y `.svg`.

**Solución:** agregar `mime-mapping` en `web.xml`. Verificado: no se generaron nuevas advertencias tras el redespliegue.

### 13.7 `asadmin` fallaba con `&` en la URL

**Causa:** `asadmin.bat` de Windows interpreta `&` como separador de comandos.

**Solución:** no incluir parámetros con `&` en la propiedad `url`; usar solo la URL base y configurar parámetros adicionales desde la consola de Payara si fueran necesarios.

### 13.8 Contraseña con caracteres especiales

**Causa:** `asadmin` usa `:` como separador de propiedades.

**Solución:** usar una contraseña alfanumérica para el entorno local o configurar el pool desde la consola de Payara. No documentar ni commitear contraseñas reales.

### 13.9 Redirección `<` en PowerShell

**Causa:** PowerShell no admite `<` para redirección de entrada.

**Solución:** documentar el uso de `cmd /c "mysql ... < database.sql"` o `Get-Content ... | mysql ...`.

### 13.10 Advertencia de JDK no soportado por Payara

**Causa:** Payara 6 puede advertir si detecta un JDK no listado oficialmente, aunque la aplicación funciona con Java 21.

**Solución:** verificar que el dominio use JDK 21 y que la aplicación responda. La verificación se realizó con Temurin 21.0.13+9 y funcionó correctamente.

---

## 14. Seguridad

### 14.1 Medidas implementadas

- Credenciales de base de datos fuera del código y del repositorio.
- Uso de un usuario de aplicación con permisos mínimos (`SELECT`, `INSERT`, `UPDATE`, `DELETE`).
- Uso de `PreparedStatement` para prevenir inyección SQL.
- Validación de datos en servidor con Bean Validation.
- Restricciones `UNIQUE` y `CHECK` en la base de datos.
- Escape de salida de JSF/PrimeFaces para prevenir XSS.
- Cookie de sesión `http-only`.
- Confirmación antes de eliminar.
- Manejo de errores sin exponer credenciales.
- Script de configuración que solicita la contraseña de forma segura y la redacta en mensajes de error.

### 14.2 Recomendaciones para producción

- Cambiar `jakarta.faces.PROJECT_STAGE` a `Production`.
- Usar HTTPS mediante proxy inverso.
- Agregar autenticación y autorización.
- Restringir el acceso a la consola de Payara.
- Cambiar contraseñas por defecto.
- Configurar respaldos de MariaDB.
- Revisar logs y alertas.
- Evaluar protección CSRF adicional si se agregan formularios con acciones sensibles.
- No publicar el puerto 3306 fuera de la red local.

---

## 15. Limitaciones y mejoras futuras

### 15.1 Limitaciones actuales

- No hay autenticación ni roles.
- No hay pruebas automatizadas.
- La paginación es en memoria (lista completa); para grandes volúmenes conviene `LazyDataModel`.
- No hay exportación de datos.
- No hay auditoría de cambios.
- La aplicación está configurada en etapa `Development`.
- El entorno de verificación usa HTTP local, no HTTPS.

### 15.2 Mejoras sugeridas

- Login con usuarios y roles (administrador, docente, consulta).
- Pruebas unitarias de DAO y pruebas de integración con Testcontainers.
- Paginación y filtrado en servidor.
- Exportación a CSV/PDF.
- Auditoría de altas, modificaciones y bajas.
- Validación de DNI con dígito verificador según el país.
- Docker Compose para MariaDB y Payara.
- Internacionalización de mensajes.
- API REST para integraciones.
- Notificaciones por correo.

---

## 16. Conclusiones

El trabajo cumplió los objetivos planteados:

- Se implementó un CRUD completo de estudiantes con Jakarta Faces, PrimeFaces y JDBC.
- Se utilizó MariaDB real mediante XAMPP y phpMyAdmin.
- Se configuró un JDBC Connection Pool y un recurso JNDI en Payara.
- Se mantuvieron las credenciales fuera del código.
- Se aplicaron validaciones de servidor y restricciones de base de datos.
- Se construyó una interfaz profesional, responsive y con mensajes claros.
- Se compiló y desplegó un WAR funcional en Payara.
- Se verificaron las operaciones contra la base de datos real.
- Se documentaron instalación, configuración, uso, pruebas y mantenimiento.

El sistema queda operativo en:

```text
http://localhost:8080/gestion-estudiantes/
```

La base de datos quedó limpia, sin registros de prueba, lista para su uso.

---

## 17. Anexos

### Anexo A. Comandos útiles

```powershell
# Compilar
.\mvnw.cmd clean package

# Iniciar Payara
& "C:\payara6\bin\asadmin.bat" start-domain

# Detener Payara
& "C:\payara6\bin\asadmin.bat" stop-domain

# Reiniciar Payara
& "C:\payara6\bin\asadmin.bat" restart-domain

# Ver aplicaciones desplegadas
& "C:\payara6\bin\asadmin.bat" list-applications

# Ver recursos JNDI
& "C:\payara6\bin\asadmin.bat" list-jdbc-resources

# Probar pool
& "C:\payara6\bin\asadmin.bat" ping-connection-pool GestionEstudiantesPool

# Desplegar
& "C:\payara6\bin\asadmin.bat" deploy --force .\target\gestion-estudiantes.war

# Desinstalar
& "C:\payara6\bin\asadmin.bat" undeploy gestion-estudiantes

# Ver log
Get-Content "C:\payara6\glassfish\domains\domain1\logs\server.log" -Tail 100 -Wait
```

### Anexo B. Inventario de archivos

| Archivo | Descripción |
|---|---|
| `pom.xml` | Configuración Maven, dependencias y plugins. |
| `mvnw`, `mvnw.cmd` | Maven Wrapper. |
| `.mvn/wrapper/maven-wrapper.properties` | Distribución Maven 3.9.9. |
| `database.sql` | Creación de base, tabla, índices y restricciones. |
| `README.md` | Guía rápida de instalación, configuración y uso. |
| `DOCUMENTACION.md` | Documento completo del trabajo. |
| `scripts/configurar-payara.ps1` | Configuración automatizada del pool/JNDI. |
| `src/main/java/.../model/Estudiante.java` | Modelo y validaciones. |
| `src/main/java/.../dao/EstudianteDAO.java` | Persistencia JDBC. |
| `src/main/java/.../config/DatabaseConfig.java` | Acceso JNDI. |
| `src/main/java/.../controller/EstudianteBean.java` | CRUD y búsqueda. |
| `src/main/java/.../controller/DashboardBean.java` | Indicadores del dashboard. |
| `src/main/webapp/WEB-INF/web.xml` | Configuración web, JNDI y MIME. |
| `src/main/webapp/WEB-INF/beans.xml` | Configuración CDI. |
| `src/main/webapp/WEB-INF/faces-config.xml` | Configuración Jakarta Faces. |
| `src/main/webapp/WEB-INF/templates/layout.xhtml` | Plantilla compartida. |
| `src/main/webapp/views/dashboard.xhtml` | Vista del dashboard. |
| `src/main/webapp/views/estudiantes.xhtml` | Vista CRUD. |
| `src/main/webapp/resources/css/styles.css` | Estilos personalizados. |
| `src/main/webapp/resources/js/app.js` | Comportamiento del sidebar. |
| `src/main/webapp/index.xhtml` | Redirección inicial. |
| `target/gestion-estudiantes.war` | WAR generado. |
| `target/payara-lib/mariadb-java-client.jar` | Driver copiado para Payara. |

### Anexo C. Glosario

| Término | Significado |
|---|---|
| JNDI | Java Naming and Directory Interface; nombre lógico del recurso en el servidor. |
| JDBC | Java Database Connectivity; API para acceder a bases de datos. |
| Connection Pool | Conjunto de conexiones reutilizables administradas por el servidor. |
| DataSource | Interfaz JDBC que representa una fuente de conexiones. |
| WAR | Web Application Archive; archivo de despliegue de la aplicación web. |
| CDI | Contexts and Dependency Injection; modelo de inyección de dependencias de Jakarta EE. |
| Bean Validation | Especificación de validación de objetos con anotaciones. |
| PrimeFaces | Biblioteca de componentes JSF. |
| Payara | Servidor de aplicaciones compatible con Jakarta EE. |
| XAMPP | Paquete que incluye Apache, MariaDB y phpMyAdmin. |
| phpMyAdmin | Herramienta web para administrar MySQL/MariaDB. |

### Anexo D. Registro de cambios

| Fecha | Cambio |
|---|---|
| 2026-11-09 | Creación del proyecto, base de datos, modelo, DAO, controladores y vistas. |
| 2026-11-09 | Configuración de Payara, driver, pool y JNDI; primer despliegue. |
| 2026-11-10 | Pruebas CRUD reales, búsqueda, edición, validaciones y duplicados. |
| 2026-11-10 | Corrección de PrimeFaces Jakarta, paginador, contador, growl y MIME. |
| 2026-11-10 | Pruebas de eliminación, dashboard, redirección y script de configuración. |
| 2026-11-10 | Limpieza de datos de prueba y documentación final. |

---

**Fin del documento.**
