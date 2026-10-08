# language: es
@US01 @US02 @US03 @US04 @US05
Característica: Visualización de signos vitales en tiempo real
  Como cuidador
  Quiero consultar la lectura actual de los signos vitales del Fragile Citizen
  Para identificar irregularidades de manera oportuna

  Antecedentes:
    Dado una persona bajo cuidado con una pulsera asignada

  Esquema del escenario: Clasificación de la lectura frente al rango normal de su tipo
    Dado el rango normal de <tipo> es de <minimo> a <maximo>
    Cuando la pulsera transmite una lectura de <tipo> de <valor>
    Y el cuidador consulta los signos vitales en vivo
    Entonces el sistema muestra <tipo> con valor <valor> clasificado como <clasificacion>

    Ejemplos:
      | tipo      | minimo | maximo | valor | clasificacion |
      | HR        | 60     | 100    | 72    | WITHIN_RANGE  |
      | HR        | 60     | 100    | 112   | ABOVE_RANGE   |
      | HR        | 60     | 100    | 45    | BELOW_RANGE   |
      | BP_SYS    | 90     | 140    | 150   | ABOVE_RANGE   |
      | BP_DIA    | 60     | 90     | 55    | BELOW_RANGE   |
      | SPO2      | 92     | 100    | 97    | WITHIN_RANGE  |
      | SPO2      | 92     | 100    | 88    | BELOW_RANGE   |
      | TEMP      | 36.0   | 37.5   | 38.4  | ABOVE_RANGE   |
      | TEMP      | 36.0   | 37.5   | 34.5  | BELOW_RANGE   |
      | RESP_RATE | 12     | 20     | 16    | WITHIN_RANGE  |
      | RESP_RATE | 12     | 20     | 28    | ABOVE_RANGE   |

  Escenario: Interrupción en la transmisión de telemetría
    Dado la última lectura de HR de 75 fue transmitida hace 10 minutos
    Cuando el cuidador consulta los signos vitales en vivo
    Entonces el sistema muestra el último valor de HR sin señal en vivo
