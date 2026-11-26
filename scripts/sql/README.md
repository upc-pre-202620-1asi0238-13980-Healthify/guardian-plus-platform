# Catálogo de planes de suscripción

## Descripción

El script `seed_subscription_catalog.sql` permite
inicializar el catálogo de planes de suscripción
de Guardian+ y sus beneficios asociados en PostgreSQL.

## Planes de suscripción

| Plan | Precio mensual | Moneda | Beneficios |
|---|---:|---|---:|
| Guardian Basic | 0.00 | PEN | 2 |
| Guardian Premium | 29.90 | PEN | 6 |
| Cuidado Pro | 59.90 | PEN | 10 |

Estos valores corresponden al catálogo inicial.
Los precios e identificadores de los planes existentes
se conservan al ejecutar el script.

El precio de Cuidado Pro está sujeto a validación
comercial por parte del equipo.

## Requisitos previos

- Tener PostgreSQL en ejecución.
- Tener inicializado el esquema de la base de datos
  de Guardian+.
- Contar con acceso a la base de datos correspondiente.

## Ejecución local con Docker

Ejecutar el siguiente comando desde la raíz del
repositorio del backend utilizando PowerShell:

```powershell
Get-Content -Raw -Encoding UTF8 .\scripts\sql\seed_subscription_catalog.sql | docker exec -i guardian-plus-postgres psql -U postgres -d guardian_plus -v ON_ERROR_STOP=1
```

## Ejecución mediante el cliente de PostgreSQL

```bash
psql -h localhost -U postgres -d guardian_plus \
  -v ON_ERROR_STOP=1 \
  -f scripts/sql/seed_subscription_catalog.sql
```

## Endpoint del catálogo

```http
GET /api/v1/subscriptions/plans
```

Este endpoint permite consultar los planes de
suscripción activos, ordenados de menor a mayor precio.

La respuesta incluye:

- Identificador del plan.
- Nombre del plan.
- Precio.
- Moneda.
- Ciclo de facturación.
- Estado del plan.
- Indicador de requerimiento de pago.
- Códigos de los beneficios asociados.

## Integración con aplicaciones móviles

Las aplicaciones móviles desarrolladas en Flutter
y Kotlin deben consumir este endpoint para obtener
dinámicamente los planes disponibles.

El backend es la fuente única de verdad para los
nombres, precios, monedas, identificadores y beneficios
de los planes de suscripción.

No se deben establecer precios ni identificadores
de planes directamente en el código de las
aplicaciones móviles.

## Consideraciones importantes

- El script es idempotente en ejecuciones secuenciales.
- Se conservan los planes y beneficios existentes.
- Se agregan las asociaciones de beneficios faltantes.
- Los beneficios de las suscripciones existentes no
  se sincronizan automáticamente.
- Registrar un beneficio en el catálogo no garantiza
  que su control de acceso esté implementado.
- El script no se ejecuta automáticamente al integrar
  los cambios en la rama develop.
- El script debe ejecutarse manualmente en cada entorno.
- Su ejecución en producción requiere la aprobación
  del equipo.

## Validaciones realizadas

- Compilación exitosa del backend.
- Tres pruebas unitarias del controlador de planes.
- Cuatro pruebas unitarias del servicio de consultas.
- Ejecución exitosa de la suite completa de pruebas.
- Verificación de la idempotencia del script SQL.
- Validación del endpoint mediante una petición HTTP 200.
- Verificación de los tres planes y sus beneficios
  directamente desde PostgreSQL.