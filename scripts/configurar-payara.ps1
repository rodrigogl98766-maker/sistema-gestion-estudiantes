<#
.SYNOPSIS
    Configura el JDBC Connection Pool y el recurso JNDI del Sistema de Gestión
    de Estudiantes en Payara Server 6.

.DESCRIPTION
    Crea (o recrea) el pool "GestionEstudiantesPool" y el recurso JNDI
    "jdbc/GestionEstudiantes" usando asadmin. También puede desplegar el WAR.

    El script no guarda la contraseña en disco. Si no se pasa -DbPassword,
    se solicita de forma segura por consola.

    Antes de ejecutarlo:
      1. MariaDB debe estar encendido y la base gestion_estudiantes creada.
      2. Payara debe estar en ejecución (start-domain).
      3. El driver mariadb-java-client.jar debe estar en
         <PAYARA_HOME>\glassfish\domains\domain1\lib
         (el script lo copia desde target\payara-lib si no existe).
      4. Si el driver se copia por primera vez, reinicie Payara después.

.PARAMETER PayaraHome
    Ruta de instalación de Payara, por ejemplo C:\payara6.

.PARAMETER DbHost
    Host de MariaDB. Por defecto 127.0.0.1.

.PARAMETER DbPort
    Puerto de MariaDB. Por defecto 3306.

.PARAMETER DbName
    Nombre de la base de datos. Por defecto gestion_estudiantes.

.PARAMETER DbUser
    Usuario de aplicación. Por defecto gestion_user.

.PARAMETER DbPassword
    Contraseña del usuario. Si se omite, se solicita por consola.
    Evite caracteres especiales como : & = en la contraseña si usa asadmin;
    para contraseñas complejas configure el pool desde la consola de Payara.

.PARAMETER Deploy
    Si se indica, despliega target\gestion-estudiantes.war al finalizar.

.EXAMPLE
    .\scripts\configurar-payara.ps1 -PayaraHome "C:\payara6" -Deploy

.EXAMPLE
    .\scripts\configurar-payara.ps1 -PayaraHome "C:\payara6" -DbUser gestion_user
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$PayaraHome,

    [string]$DbHost = "127.0.0.1",
    [int]$DbPort = 3306,
    [string]$DbName = "gestion_estudiantes",
    [string]$DbUser = "gestion_user",
    [string]$DbPassword,
    [switch]$Deploy
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$asadmin = Join-Path $PayaraHome "bin\asadmin.bat"
$domainLib = Join-Path $PayaraHome "glassfish\domains\domain1\lib"
$driverDestination = Join-Path $domainLib "mariadb-java-client.jar"
$driverSource = Join-Path $projectRoot "target\payara-lib\mariadb-java-client.jar"
$warPath = Join-Path $projectRoot "target\gestion-estudiantes.war"

$poolName = "GestionEstudiantesPool"
$jndiName = "jdbc/GestionEstudiantes"

function Invoke-Asadmin {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,
        [switch]$AllowFailure
    )

    $output = & $asadmin @Arguments 2>&1
    $exitCode = $LASTEXITCODE

    if ($null -ne $output) {
        # Evita mostrar la contraseña si asadmin la incluye en un mensaje de error.
        $output | ForEach-Object { Write-Host ($_.ToString().Replace($DbPassword, "***")) }
    }

    if ($exitCode -ne 0 -and -not $AllowFailure) {
        throw "asadmin falló (código $exitCode) al ejecutar '$($Arguments[0])'."
    }

    return $output
}

if (-not (Test-Path $asadmin)) {
    throw "No se encontró asadmin en '$asadmin'. Verifique -PayaraHome."
}

if (-not $DbPassword) {
    $securePassword = Read-Host "Contraseña de '$DbUser'" -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    throw "La contraseña de la base de datos no puede estar vacía."
}

# Driver JDBC: Payara lo necesita en domain1/lib porque el WAR lo declara como provided.
if (-not (Test-Path $driverDestination)) {
    if (-not (Test-Path $driverSource)) {
        throw "No se encontró el driver en '$driverDestination' ni en '$driverSource'. Ejecute primero: .\mvnw.cmd clean package"
    }

    New-Item -ItemType Directory -Force -Path $domainLib | Out-Null
    Copy-Item -Path $driverSource -Destination $driverDestination -Force
    Write-Host "Driver copiado a: $driverDestination"
    Write-Host "IMPORTANTE: reinicie Payara para que cargue el driver antes de usar el pool."
}
else {
    Write-Host "Driver encontrado en: $driverDestination"
}

Write-Host "Verificando que Payara esté en ejecución..."
Invoke-Asadmin -Arguments @("list-domains") | Out-Null

# Se recrean el recurso y el pool para que la configuración sea idempotente.
# Si la aplicación está desplegada, vuelva a desplegarla o reinicie Payara después.
Write-Host "Eliminando configuración previa (si existe)..."
Invoke-Asadmin -Arguments @("delete-jdbc-resource", $jndiName) -AllowFailure | Out-Null
Invoke-Asadmin -Arguments @("delete-jdbc-connection-pool", $poolName) -AllowFailure | Out-Null

$property = "url=jdbc\:mariadb\://${DbHost}\:${DbPort}/${DbName}:user=${DbUser}:password=${DbPassword}"

Write-Host "Creando JDBC Connection Pool '$poolName'..."
Invoke-Asadmin -Arguments @(
    "create-jdbc-connection-pool",
    "--datasourceclassname", "org.mariadb.jdbc.MariaDbDataSource",
    "--restype", "javax.sql.DataSource",
    "--property", $property,
    $poolName
) | Out-Null

Write-Host "Creando recurso JNDI '$jndiName'..."
Invoke-Asadmin -Arguments @(
    "create-jdbc-resource",
    "--connectionpoolid", $poolName,
    $jndiName
) | Out-Null

Write-Host "Probando el pool..."
Invoke-Asadmin -Arguments @("ping-connection-pool", $poolName) | Out-Null
Write-Host "Pool '$poolName' operativo."

if ($Deploy) {
    if (-not (Test-Path $warPath)) {
        throw "No se encontró el WAR en '$warPath'. Ejecute primero: .\mvnw.cmd clean package"
    }

    Write-Host "Desplegando '$warPath'..."
    Invoke-Asadmin -Arguments @("deploy", "--force", $warPath) | Out-Null
    Write-Host "Aplicación desplegada."
}

Write-Host ""
Write-Host "Configuración completada."
Write-Host "  Pool : $poolName"
Write-Host "  JNDI : $jndiName"
Write-Host "  URL  : jdbc:mariadb://${DbHost}:${DbPort}/${DbName}"
Write-Host ""
Write-Host "Si Payara ya estaba en ejecución y copió el driver ahora, reinícielo:"
Write-Host "  & '$asadmin' restart-domain"
