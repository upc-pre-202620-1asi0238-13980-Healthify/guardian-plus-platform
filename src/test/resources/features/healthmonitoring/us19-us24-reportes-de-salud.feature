# language: es
@US19 @US24
Característica: Reportes de salud
  Como cuidador
  Quiero recibir reportes consolidados del estado de salud del Fragile Citizen
  Para evaluar su evolución sin revisar la telemetría segundo a segundo

  Antecedentes:
    Dado una persona bajo cuidado con una pulsera asignada
    Y un umbral de HR entre 60 y 100 con 3 lecturas consecutivas
    Y un umbral de SPO2 entre 95 y 100 con 3 lecturas consecutivas

  Escenario: Solicitud de reporte en un rango vacío
    Cuando el cuidador solicita el reporte del "2026-09-01" al "2026-09-30"
    Entonces el sistema bloquea el reporte indicando que no existen registros en el rango

  Escenario: Compilación automática al cierre del ciclo semanal
    Dado la pulsera transmite las lecturas de HR: "70, 72, 68"
    Y la pulsera transmite las lecturas de SPO2: "97, 98"
    Cuando se compila el resumen semanal
    Entonces el reporte resume 2 parámetros
    Y el reporte es clínicamente estable

  Escenario: Resaltado de incidentes recurrentes
    Dado la pulsera transmite las lecturas de HR: "120, 70, 125, 72, 130, 71, 135"
    Y la pulsera transmite las lecturas de SPO2: "97, 89"
    Cuando se compila el resumen semanal
    Entonces el reporte marca HR como RECURRENT
    Y el reporte marca SPO2 como UNSTABLE
    Y el reporte incluye 1 parámetro recurrente
