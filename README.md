# Sistema de Gestión de Estudiantes

Aplicación web para la gestión académica de estudiantes, desarrollada con **Java 21**, **Jakarta EE 10 Web Profile**, **Jakarta Faces**, **PrimeFaces**, **JDBC puro** y **MariaDB**. La aplicación se despliega como WAR en **Payara Server 6** y obtiene las conexiones a la base de datos mediante un **JDBC Connection Pool** administrado por el servidor y expuesto por **JNDI** como `jdbc/GestionEstudiantes`.

> **Datos reales, no simulados.** La aplicación fue compilada, desplegada y probada contra una instancia real de MariaDB (XAMPP) y Payara Server. La sección [Verificación realizada](#6-verificación-realizada) resume las pruebas ejecutadas.

> **Documentación completa:** el informe técnico con objetivos, arquitectura, desarrollo, pruebas, evidencias, problemas y soluciones está en **[DOCUMENTACION.md](DOCUMENTACION.md)**. Este README es la guía rápida de instalación y uso.

> **Evidencias de Payara:** capturas de la consola, aplicación desplegada, pool JNDI y salidas de comandos en **[docs/evidencias-payara/EVIDENCIAS-PAYARA.md](docs/evidencias-payara/EVIDENCIAS-PAYARA.md)**.

---

## Características

- CRUD completo de estudiantes: registrar, listar, buscar, editar y eliminar.
- Búsqueda por nombres, apellidos o DNI.
- Validación de datos en servidor con Jakarta Bean Validation:
  - Nombres y apellidos obligatorios, máximo 100 caracteres.
  - DNI obligatorio, exactamente 8 dígitos numéricos y único.
  - Correo obligatorio, formato válido, máximo 120 caracteres y único.
  - Teléfono obligatorio, entre 6 y 15 caracteres válidos.
- Mensajes de éxito y error con `p:growl` y mensajes junto a cada campo.
- Confirmación antes de eliminar un estudiante.
- Dashboard con indicadores reales: total de estudiantes, altas del mes, último registro y estado de la conexión a MariaDB.
- Interfaz responsive con menú lateral, tarjetas, tabla PrimeFaces, diálogos modales y estilos personalizados.
- Persistencia con JDBC `PreparedStatement`; conexiones, sentencias y `ResultSet` cerrados con `try-with-resources`.
- Credenciales de base de datos fuera del código, administradas por Payara.
- Script de PowerShell para configurar el pool y el recurso JNDI de forma repetible.

---

## Tecnologías y versiones

| Componente | Versión usada / requerida |
|---|---|
| Java | JDK 21 (probado con Temurin 21.0.13+9 LTS) |
| Jakarta EE | 10.0 Web Profile |
| Jakarta Faces | 4.0 (incluido en Payara) |
| PrimeFaces | 14.0.18, clasificador `jakarta` |
| MariaDB | 10.4.32 (XAMPP); compatible con 10.4+ |
| MariaDB Connector/J | 3.5.11 |
| Payara Server Community | 6.2025.11 |
| Maven | 3.9.9 (Maven Wrapper incluido) |
| Servidor de base de datos | XAMPP / MariaDB en `localhost:3306` |
| IDE recomendado | Visual Studio Code + Extension Pack for Java + XML |

**Restricciones respetadas:** no se usa Spring Boot, Hibernate/JPA, React, Angular ni Vue. La persistencia es JDBC directo con `PreparedStatement`.

---

## Requisitos previos

1. **JDK 21** instalado y disponible.
   ```powershell
   java -version
   ```
   Debe mostrar una versión 21.x.

2. **XAMPP** con MariaDB/MySQL funcionando.
   - Abra el Panel de Control de XAMPP e inicie **MySQL**.
   - Verifique que el puerto sea `3306` (o ajuste la URL del pool si usa otro).

3. **Payara Server Community 6** descargado y descomprimido, por ejemplo en `C:\payara6`.
   - Descarga: <https://www.payara.fish/downloads/payara-platform-community-edition/>
   - Verifique:
     ```powershell
     & "C:\payara6\bin\asadmin.bat" version
     ```

4. **Maven** no es obligatorio instalarlo por separado: el proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`), que descarga Maven 3.9.9 automáticamente.

5. **Visual Studio Code** (opcional pero recomendado):
   - Extensiones: `vscjava.vscode-java-pack`, `redhat.vscode-xml`, `redhat.vscode-community-server-connector`.
   - Abra la carpeta del proyecto y espere a que Maven importe las dependencias.

---

## Estructura del proyecto

```text
sistema-gestion-estudiantes/
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── .vscode/
│   ├── extensions.json
│   └── settings.json
├── scripts/
│   └── configurar-payara.ps1
├── src/
│   └── main/
│       ├── java/
│       │   └── com/gestionestudiantes/
│       │       ├── config/
│       │       │   └── DatabaseConfig.java
│       │       ├── controller/
│       │       │   ├── DashboardBean.java
│       │       │   └── EstudianteBean.java
│       │       ├── dao/
│       │       │   └── EstudianteDAO.java
│       │       └── model/
│       │           └── Estudiante.java
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── beans.xml
│           │   ├── faces-config.xml
│           │   ├── templates/
│           │   │   └── layout.xhtml
│           │   └── web.xml
│           ├── resources/
│           │   ├── css/
│           │   │   └── styles.css
│           │   └── js/
│           │       └── app.js
│           ├── views/
│           │   ├── dashboard.xhtml
│           │   └── estudiantes.xhtml
│           └── index.xhtml
├── database.sql
├── DOCUMENTACION.md
├── docs/
│   └── evidencias-payara/
│       └── EVIDENCIAS-PAYARA.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Responsabilidad de cada capa

| Paquete / archivo | Responsabilidad |
|---|---|
| `model.Estudiante` | Modelo de datos y anotaciones de Bean Validation. |
| `dao.EstudianteDAO` | Operaciones JDBC: listar, buscar, contar, insertar, actualizar, eliminar y verificar duplicados. |
| `config.DatabaseConfig` | Inyección del `DataSource` JNDI y obtención de conexiones del pool. |
| `controller.EstudianteBean` | Bean CDI `@Named` / `@ViewScoped` para el CRUD y la búsqueda. |
| `controller.DashboardBean` | Bean CDI con los indicadores reales del dashboard. |
| `web.xml` | Configuración de Faces, tema, referencia JNDI y tipos MIME. |
| `layout.xhtml` | Plantilla compartida: menú lateral, barra superior, growl y pie. |
| `dashboard.xhtml` | Vista del panel de control. |
| `estudiantes.xhtml` | Vista de listado, formulario modal y confirmación de eliminación. |

---

## 1. Base de datos

### 1.1 Crear la base y la tabla

El archivo `database.sql` crea la base `gestion_estudiantes`, la tabla `estudiantes`, sus índices y restricciones.

**Opción A: phpMyAdmin (recomendada para XAMPP)**

1. Inicie **Apache** y **MySQL** en XAMPP.
2. Abra <http://localhost/phpmyadmin>.
3. Vaya a la pestaña **SQL**.
4. Copie y pegue todo el contenido de `database.sql`.
5. Ejecute.

**Opción B: línea de comandos (PowerShell)**

PowerShell no admite el operador `<` para redirección de entrada; use `cmd /c`:

```powershell
cmd /c "C:\xampp\mysql\bin\mysql.exe -u root -p < database.sql"
```

Si el usuario `root` de XAMPP no tiene contraseña, omita `-p`:

```powershell
cmd /c "C:\xampp\mysql\bin\mysql.exe -u root < database.sql"
```

> Ajuste la ruta de `mysql.exe` si XAMPP está instalado en otra ubicación.

**Opción C: desde VS Code**

Abra una terminal integrada en la raíz del proyecto y ejecute una de las opciones anteriores.

### 1.2 Crear el usuario de aplicación

No use `root` para la aplicación. Cree un usuario con permisos mínimos. Ejecute en phpMyAdmin o en la consola de MySQL:

```sql
CREATE USER IF NOT EXISTS 'gestion_user'@'localhost' IDENTIFIED BY '<TU_CLAVE_SEGURA>';
CREATE USER IF NOT EXISTS 'gestion_user'@'127.0.0.1' IDENTIFIED BY '<TU_CLAVE_SEGURA>';

GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_estudiantes.* TO 'gestion_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_estudiantes.* TO 'gestion_user'@'127.0.0.1';

FLUSH PRIVILEGES;
```

> **Importante:** el pool se conecta por TCP a `127.0.0.1`, por eso se crean ambos hosts. Si su MariaDB solo permite `localhost`, use `localhost` en la URL del pool; si usa otro host, ajuste los `GRANT` correspondientes.
>
> Cambie `<TU_CLAVE_SEGURA>` por una contraseña real y **no la guarde en el repositorio**. En el entorno de verificación se usó una contraseña local almacenada fuera del proyecto.

### 1.3 Verificar la base

```powershell
C:\xampp\mysql\bin\mysql.exe -u gestion_user -p -h 127.0.0.1 gestion_estudiantes -e "SHOW TABLES; DESCRIBE estudiantes;"
```

También puede comprobar las restricciones:

```sql
SHOW CREATE TABLE estudiantes;
```

Debe ver:

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_estudiantes_dni (dni)`
- `UNIQUE KEY uk_estudiantes_correo (correo)`
- `CHECK (dni REGEXP '^[0-9]{8}$')`
- Índices por apellidos/nombres y por fecha de registro.

---

## 2. Configurar Payara Server

### 2.1 Iniciar Payara

```powershell
& "C:\payara6\bin\asadmin.bat" start-domain
```

- Aplicación: <http://localhost:8080/gestion-estudiantes/>
- Consola de administración: <http://localhost:4848>

Si el dominio ya está iniciado, `start-domain` lo indicará. Para detenerlo:

```powershell
& "C:\payara6\bin\asadmin.bat" stop-domain
```

### 2.2 Instalar el driver de MariaDB en Payara

El proyecto declara el driver como `provided` para no incluirlo dentro del WAR; Payara debe tenerlo en el classpath del dominio.

1. Compile el proyecto (esto copia el driver a `target/payara-lib`):
   ```powershell
   .\mvnw.cmd clean package
   ```

2. Copie el driver al directorio `lib` del dominio:
   ```powershell
   Copy-Item .\target\payara-lib\mariadb-java-client.jar `
     C:\payara6\glassfish\domains\domain1\lib\ -Force
   ```

3. **Reinicie Payara** para que cargue el driver:
   ```powershell
   & "C:\payara6\bin\asadmin.bat" restart-domain
   ```

> Si el driver no está en `domain1/lib`, el pool fallará con `ClassNotFoundException: org.mariadb.jdbc.MariaDbDataSource` o `No suitable driver`.

### 2.3 Crear el JDBC Connection Pool

**Opción A: asadmin (PowerShell)**

```powershell
& "C:\payara6\bin\asadmin.bat" create-jdbc-connection-pool `
  --datasourceclassname org.mariadb.jdbc.MariaDbDataSource `
  --restype javax.sql.DataSource `
  --property "url=jdbc\:mariadb\://127.0.0.1\:3306/gestion_estudiantes:user=gestion_user:password=<TU_CLAVE_SEGURA>" `
  GestionEstudiantesPool
```

Notas importantes:

- Los dos puntos de la URL se escapan con `\:` porque `asadmin` usa `:` como separador de propiedades.
- No agregue parámetros con `&` (por ejemplo `?useUnicode=true&characterEncoding=UTF-8`) en el comando de Windows: `asadmin.bat` puede interpretar `&` como separador de comandos. MariaDB Connector/J 3.x usa UTF-8/utf8mb4 por defecto.
- Si la contraseña contiene `:`, `&`, `=` o espacios, es más seguro configurar el pool desde la consola de Payara o usar una contraseña alfanumérica para este entorno local.
- El nombre del pool debe ser exactamente `GestionEstudiantesPool`.

**Opción B: consola de administración**

1. Abra <http://localhost:4848>.
2. Vaya a **Resources → JDBC → JDBC Connection Pools → New...**.
3. Complete:
   - **Pool Name:** `GestionEstudiantesPool`
   - **Resource Type:** `javax.sql.DataSource`
   - **Database Driver Vendor:** `MariaDB` (si aparece) o `Custom`
4. En **Additional Properties** configure:
   - `url` = `jdbc:mariadb://127.0.0.1:3306/gestion_estudiantes`
   - `user` = `gestion_user`
   - `password` = su contraseña
   - `datasourceclassname` = `org.mariadb.jdbc.MariaDbDataSource` (si la consola lo solicita)
5. Guarde y use **Ping** para probar la conexión.

### 2.4 Crear el recurso JNDI

**Opción A: asadmin**

```powershell
& "C:\payara6\bin\asadmin.bat" create-jdbc-resource `
  --connectionpoolid GestionEstudiantesPool `
  jdbc/GestionEstudiantes
```

**Opción B: consola**

1. Vaya a **Resources → JDBC → JDBC Resources → New...**.
2. **JNDI Name:** `jdbc/GestionEstudiantes`
3. **Pool Name:** `GestionEstudiantesPool`
4. Guarde.

El nombre debe coincidir con `DatabaseConfig.JNDI_NAME` y con el `<lookup-name>` de `web.xml`.

### 2.5 Verificar el pool y el recurso

```powershell
& "C:\payara6\bin\asadmin.bat" ping-connection-pool GestionEstudiantesPool
& "C:\payara6\bin\asadmin.bat" list-jdbc-connection-pools
& "C:\payara6\bin\asadmin.bat" list-jdbc-resources
```

Resultado esperado: `Command ping-connection-pool executed successfully.` y ambos nombres listados.

### 2.6 Script automatizado (opcional)

El repositorio incluye `scripts/configurar-payara.ps1`, que:

- Verifica que Payara esté en ejecución.
- Copia el driver desde `target/payara-lib` si aún no está en `domain1/lib`.
- Elimina y recrea el pool y el recurso JNDI (configuración idempotente).
- Ejecuta `ping-connection-pool`.
- Opcionalmente despliega el WAR con `-Deploy`.

Uso:

```powershell
.\scripts\configurar-payara.ps1 -PayaraHome "C:\payara6" -Deploy
```

El script solicita la contraseña de forma segura si no se pasa `-DbPassword`. No guarda credenciales en disco.

> Si el script copia el driver por primera vez, reinicie Payara antes de usar la aplicación:
> ```powershell
> & "C:\payara6\bin\asadmin.bat" restart-domain
> ```
>
> Si la aplicación ya estaba desplegada, vuelva a desplegarla después de recrear el pool/recurso.

---

## 3. Compilar y desplegar

### 3.1 Configurar JDK 21

Verifique `JAVA_HOME`:

```powershell
$env:JAVA_HOME
java -version
```

Si es necesario, ajústelo temporalmente en la sesión de PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

La ruta exacta depende de su instalación. El proyecto exige Java 21 o superior mediante `maven-enforcer-plugin`.

### 3.2 Compilar

```powershell
.\mvnw.cmd clean package
```

En Linux/macOS:

```bash
./mvnw clean package
```

Resultado esperado:

- `BUILD SUCCESS`
- WAR: `target\gestion-estudiantes.war`
- Driver copiado: `target\payara-lib\mariadb-java-client.jar`

### 3.3 Desplegar en Payara

Con Payara en ejecución:

```powershell
& "C:\payara6\bin\asadmin.bat" deploy --force .\target\gestion-estudiantes.war
```

También puede copiar el WAR a:

```text
C:\payara6\glassfish\domains\domain1\autodeploy\
```

Payara lo desplegará automáticamente (el nombre del contexto será el nombre del archivo, `gestion-estudiantes`).

### 3.4 Abrir la aplicación

- Inicio / redirección: <http://localhost:8080/gestion-estudiantes/>
- Dashboard: <http://localhost:8080/gestion-estudiantes/views/dashboard.xhtml>
- Estudiantes: <http://localhost:8080/gestion-estudiantes/views/estudiantes.xhtml>

### 3.5 Recompilar y volver a desplegar

Después de cambiar código o vistas:

```powershell
.\mvnw.cmd clean package
& "C:\payara6\bin\asadmin.bat" deploy --force .\target\gestion-estudiantes.war
```

### 3.6 Desinstalar

```powershell
& "C:\payara6\bin\asadmin.bat" undeploy gestion-estudiantes
```

---

## 4. Uso de la aplicación

### Dashboard

Muestra:

- Total de estudiantes registrados.
- Altas durante el mes actual.
- Último estudiante registrado y su fecha.
- Estado real de la conexión al pool JNDI.
- Tabla con los últimos 5 registros.

### Gestión de estudiantes

1. **Registrar:** pulse **Nuevo estudiante**, complete los campos y pulse **Registrar estudiante**. El diálogo se cierra y aparece un mensaje de éxito.
2. **Buscar:** escriba en el campo de búsqueda y pulse **Buscar** o Enter. Se busca por nombres, apellidos o DNI. **Limpiar** restablece el listado.
3. **Editar:** pulse el icono del lápiz en la fila. El formulario se carga con los datos actuales; modifique y pulse **Actualizar datos**.
4. **Eliminar:** pulse el icono de la papelera. Se muestra un diálogo de confirmación con el nombre del estudiante. **Sí, eliminar** borra el registro; **Cancelar** lo conserva.
5. **Validaciones:** los campos obligatorios, el DNI de 8 dígitos, el correo válido y la unicidad de DNI/correo se validan en servidor. Los errores aparecen junto al campo y en el growl.

---

## 5. Configuración técnica

### 5.1 JNDI y pool

| Elemento | Valor |
|---|---|
| Nombre JNDI | `jdbc/GestionEstudiantes` |
| Pool | `GestionEstudiantesPool` |
| DataSource class | `org.mariadb.jdbc.MariaDbDataSource` |
| Resource type | `javax.sql.DataSource` |
| URL JDBC | `jdbc:mariadb://127.0.0.1:3306/gestion_estudiantes` |
| Driver | MariaDB Connector/J 3.5.11 en `domain1/lib` |

`DatabaseConfig` inyecta el recurso:

```java
@Resource(lookup = "jdbc/GestionEstudiantes")
private DataSource dataSource;
```

`web.xml` declara la referencia:

```xml
<resource-ref>
    <res-ref-name>jdbc/GestionEstudiantes</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
    <lookup-name>jdbc/GestionEstudiantes</lookup-name>
</resource-ref>
```

### 5.2 Persistencia JDBC

`EstudianteDAO` usa `PreparedStatement` para todas las operaciones y cierra los recursos con `try-with-resources`:

```java
try (Connection conexion = databaseConfig.getConnection();
     PreparedStatement sentencia = conexion.prepareStatement(sql);
     ResultSet resultado = sentencia.executeQuery()) {
    // ...
}
```

No se usan ORM ni generación automática de SQL. Las consultas de búsqueda usan parámetros `LIKE ?` y las inserciones/actualizaciones usan marcadores `?`.

### 5.3 Credenciales

- No hay contraseñas en el código Java, en `pom.xml`, en `web.xml` ni en `database.sql`.
- Las credenciales viven únicamente en la configuración del pool de Payara.
- El archivo `database.sql` incluye el bloque de creación de usuario **comentado** y con un marcador `<TU_CLAVE_SEGURA>`.
- No agregue archivos `.env`, contraseñas ni capturas con credenciales al repositorio.

### 5.4 PrimeFaces Jakarta

El `pom.xml` usa el clasificador `jakarta`:

```xml
<dependency>
    <groupId>org.primefaces</groupId>
    <artifactId>primefaces</artifactId>
    <version>14.0.18</version>
    <classifier>jakarta</classifier>
</dependency>
```

Sin este clasificador, Payara puede fallar al desplegar con `NoClassDefFoundError: javax/servlet/ServletRequestListener`.

---

## 6. Verificación realizada

Las siguientes pruebas se ejecutaron realmente el **10 de noviembre de 2026** sobre:

- Windows + XAMPP MariaDB 10.4.32 en `127.0.0.1:3306`.
- Payara Server Community 6.2025.11 en `localhost:8080`.
- JDK 21.0.13+9 (Temurin).
- Maven 3.9.9 mediante Maven Wrapper.
- Base `gestion_estudiantes` con la tabla `estudiantes` creada desde `database.sql`.

| Prueba | Resultado obtenido |
|---|---|
| `mvnw.cmd clean package` | `BUILD SUCCESS`; 5 clases compiladas; WAR generado en `target/gestion-estudiantes.war`. |
| Copia del driver a `domain1/lib` | Driver `mariadb-java-client.jar` presente y cargado por Payara. |
| `create-jdbc-connection-pool` | Pool `GestionEstudiantesPool` creado. |
| `create-jdbc-resource` | Recurso `jdbc/GestionEstudiantes` creado. |
| `ping-connection-pool` | `Command ping-connection-pool executed successfully.` |
| `deploy --force` | `Application deployed with name gestion-estudiantes.` |
| Dashboard | Estado **Operativa**, total real de estudiantes, altas del mes y último registro consultados desde MariaDB. |
| Registrar estudiante | Se creó **María Fernanda García López**, DNI `12345678`, correo `maria.garcia@example.com`; la fila apareció en la tabla y el `SELECT` en MariaDB confirmó el registro con `fecha_registro` real. |
| Buscar por apellido | Búsqueda `García` devolvió 1 resultado y el contador mostró `1 estudiante encontrado`. |
| Editar | Se actualizaron correo y teléfono; la tabla y el `SELECT` en MariaDB reflejaron los nuevos valores. |
| Validación de campos vacíos | El formulario permaneció abierto y mostró los 5 mensajes de obligatoriedad, uno por campo. |
| DNI inválido | DNI `1234` rechazado con `El DNI debe contener exactamente 8 dígitos.` |
| Correo inválido | `correo-invalido` rechazado con `Ingrese un correo electrónico válido.` |
| DNI duplicado | Rechazado con `Ya existe un estudiante registrado con este DNI.`; no se insertó otra fila. |
| Correo duplicado | Rechazado con `Ya existe un estudiante registrado con este correo electrónico.`; no se insertó otra fila. |
| Cancelar eliminación | El diálogo se cerró y el estudiante permaneció en la tabla y en MariaDB. |
| Confirmar eliminación | El registro se eliminó, apareció el mensaje de éxito y el contador/paginador se actualizaron. |
| Paginación | Plantilla corregida a `Mostrando {startRecord} a {endRecord} de {totalRecords}`; se verificó `Mostrando 1 a 1 de 1` y `Mostrando 1 a 2 de 2`. |
| Redirección de inicio | `http://localhost:8080/gestion-estudiantes/` redirigió a `views/dashboard.xhtml`. |
| Script de configuración | `scripts/configurar-payara.ps1` recreó pool y recurso, el `ping` fue exitoso y la aplicación volvió a desplegarse y consultar MariaDB correctamente. |
| Advertencias JSF1091 | Se agregaron tipos MIME para `eot`, `ttf`, `woff`, `woff2` y `svg` en `web.xml`; no se generaron nuevas advertencias tras el redespliegue. |

> Los registros creados exclusivamente para las pruebas se eliminaron al finalizar. La tabla queda lista para que usted registre sus propios estudiantes.

### Correcciones aplicadas durante la verificación

- **PrimeFaces Jakarta:** se agregó el clasificador `jakarta` para evitar `NoClassDefFoundError` por clases `javax`.
- **Plantilla del paginador:** PrimeFaces 14 usa `{startRecord}` y `{endRecord}`; se reemplazaron los antiguos `{first}` y `{last}`.
- **Contador tras eliminar:** el botón de eliminar está dentro de la tabla (`UIData`), por lo que el target `contador` no se resolvía; se cambió a `:formPrincipal:contador`.
- **Mensajes duplicados:** se configuró `p:growl` con `showSummary="false"` para no repetir el mismo texto en resumen y detalle.
- **Gramática del contador:** se agregaron las formas singulares `1 estudiante registrado` / `1 estudiante encontrado`.
- **Tipos MIME:** se agregaron los tipos de las fuentes de PrimeIcons en `web.xml`.

---

## 7. Solución de problemas

### La página muestra 502 Bad Gateway o no conecta

**Causa:** Payara no está iniciado o el puerto 8080 está ocupado.

```powershell
& "C:\payara6\bin\asadmin.bat" list-domains
& "C:\payara6\bin\asadmin.bat" start-domain
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

Si otro proceso usa el 8080, deténgalo o cambie el puerto HTTP del listener en la consola de Payara.

### `NoClassDefFoundError: javax/servlet/ServletRequestListener`

**Causa:** se usó el artefacto de PrimeFaces para `javax` en lugar de Jakarta.

**Solución:** verifique que `pom.xml` tenga `<classifier>jakarta</classifier>`, ejecute `.\mvnw.cmd clean package` y vuelva a desplegar.

### `NameNotFoundException: jdbc/GestionEstudiantes`

**Causa:** el recurso JNDI no existe, tiene otro nombre o la aplicación se desplegó antes de crearlo.

**Solución:**

```powershell
& "C:\payara6\bin\asadmin.bat" list-jdbc-resources
& "C:\payara6\bin\asadmin.bat" create-jdbc-resource --connectionpoolid GestionEstudiantesPool jdbc/GestionEstudiantes
```

Verifique también el `<lookup-name>` de `web.xml` y redespliegue.

### `ClassNotFoundException: org.mariadb.jdbc.MariaDbDataSource` o `No suitable driver`

**Causa:** el driver no está en el classpath de Payara.

**Solución:**

```powershell
Copy-Item .\target\payara-lib\mariadb-java-client.jar `
  C:\payara6\glassfish\domains\domain1\lib\ -Force
& "C:\payara6\bin\asadmin.bat" restart-domain
```

### `Access denied for user 'gestion_user'@'localhost'`

**Causa:** contraseña incorrecta, usuario inexistente o el host no coincide.

**Solución:** cree el usuario para `localhost` y `127.0.0.1` con la misma contraseña y otorgue los permisos indicados en [1.2](#12-crear-el-usuario-de-aplicación). Verifique:

```sql
SELECT User, Host FROM mysql.user WHERE User = 'gestion_user';
SHOW GRANTS FOR 'gestion_user'@'localhost';
SHOW GRANTS FOR 'gestion_user'@'127.0.0.1';
```

### `Unknown database 'gestion_estudiantes'` o `Table ... doesn't exist`

**Causa:** no se ejecutó `database.sql` o se ejecutó en otra instancia de MariaDB.

**Solución:** ejecute `database.sql` en la misma instancia y puerto configurados en el pool (`127.0.0.1:3306`).

### `Duplicate entry ... for key 'uk_estudiantes_dni'`

**Causa:** intento de insertar un DNI o correo ya registrado.

La aplicación captura esta situación y muestra un mensaje amigable. Si ocurre al ejecutar SQL manual, es el comportamiento esperado de las restricciones `UNIQUE`.

### Acentos o caracteres especiales incorrectos

**Causa:** conexión o base con un charset distinto de UTF-8.

**Solución:**

- La base y la tabla se crean con `utf8mb4` / `utf8mb4_unicode_ci`.
- MariaDB Connector/J 3.x usa UTF-8 por defecto.
- Evite agregar parámetros con `&` al comando `asadmin` de Windows; si necesita parámetros de conexión, configúrelos desde la consola de Payara.
- Verifique el charset:
  ```sql
  SHOW VARIABLES LIKE 'character_set%';
  ```

### `asadmin` falla con `&` en la propiedad del pool

**Causa:** `asadmin.bat` de Windows interpreta `&` como separador de comandos.

**Solución:** no incluya `&` en la propiedad `url`. Use solo `jdbc:mariadb://host:puerto/base` y configure parámetros adicionales desde la consola de Payara si fueran necesarios.

### Contraseña con `:`, `&`, `=` o espacios

**Causa:** `asadmin` usa `:` como separador de propiedades y `&` como separador de comandos.

**Solución:** use la consola de Payara para configurar el pool, o utilice una contraseña alfanumérica para el entorno local. No publique contraseñas reales.

### Advertencias `JSF1091` sobre `primeicons.ttf`, `.woff`, etc.

**Causa:** Payara no tiene mapeados los tipos MIME de las fuentes.

**Solución:** el proyecto ya incluye los `mime-mapping` en `web.xml`. Si aparecen advertencias antiguas en el log, corresponden a despliegues anteriores; limpie/regenere el WAR y redespliegue.

### Error de versión de Java al compilar

**Causa:** Maven está usando un JDK distinto de 21.

**Solución:**

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
.\mvnw.cmd clean package
```

### Payara advierte “unsupported JDK version” al iniciar

Verifique que el dominio esté usando JDK 21:

```powershell
& "C:\payara6\bin\asadmin.bat" version
```

La aplicación fue verificada con Temurin 21.0.13+9. Si su Payara usa otro JDK, ajuste `JAVA_HOME` o la configuración de `asenv.conf` del dominio.

---

## 8. Seguridad y buenas prácticas

- Cambie la contraseña de `gestion_user` y no use la misma en otros sistemas.
- Restrinja el usuario de base de datos a `localhost` / `127.0.0.1` y solo a las operaciones necesarias (`SELECT`, `INSERT`, `UPDATE`, `DELETE`).
- No use el usuario `root` de MariaDB para la aplicación.
- No publique la consola de administración de Payara (`4848`) fuera de su equipo.
- En producción, use HTTPS mediante un proxy inverso y habilite autenticación/autorización de usuarios.
- Cambie `jakarta.faces.PROJECT_STAGE` de `Development` a `Production` en `web.xml` antes de publicar.
- Mantenga respaldos de la base de datos.
- No agregue contraseñas, archivos `.env` ni certificados privados al repositorio.
- Revise los logs de Payara (`glassfish/domains/domain1/logs/server.log`) ante errores.

---

## 9. Posibles mejoras

- Autenticación y roles (administrador, docente, consulta).
- Pruebas automatizadas de DAO y de vistas (JUnit, Testcontainers o Arquillian).
- Paginación y filtrado en servidor con `LazyDataModel` para grandes volúmenes.
- Exportación de listados a CSV/PDF.
- Auditoría de cambios (quién creó/modificó/eliminó).
- Validación de DNI con dígito verificador si el país lo requiere.
- Docker Compose para MariaDB y Payara.
- Internacionalización de mensajes y formatos de fecha.

---

## Comandos rápidos

```powershell
# 1. Compilar
.\mvnw.cmd clean package

# 2. Iniciar Payara
& "C:\payara6\bin\asadmin.bat" start-domain

# 3. Configurar pool, JNDI y desplegar
.\scripts\configurar-payara.ps1 -PayaraHome "C:\payara6" -Deploy

# 4. Abrir la aplicación
Start-Process "http://localhost:8080/gestion-estudiantes/"

# 5. Ver logs
Get-Content "C:\payara6\glassfish\domains\domain1\logs\server.log" -Tail 100 -Wait
```

---

**Proyecto académico:** Sistema de Gestión de Estudiantes · Jakarta Faces + PrimeFaces + JDBC + MariaDB + Payara Server.
