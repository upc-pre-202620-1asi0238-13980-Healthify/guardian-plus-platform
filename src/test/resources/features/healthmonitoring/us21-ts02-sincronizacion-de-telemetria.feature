# language: es
@US21 @TS02
Característica: Sincronización y persistencia resiliente de telemetría
  Como cuidador
  Quiero que las lecturas guardadas por la pulsera durante una pérdida de red se sincronicen al reconectarse
  Para garantizar la integridad histórica

  Antecedentes:
    Dado una persona bajo cuidado con una pulsera asignada

  Escenario: Vaciado del buffer tras la reconexión eliminando duplicados
    Cuando la pulsera sincroniza un lote de lecturas de HR:
      | valor | hace_segundos |
      | 70    | 300           |
      | 72    | 240           |
      | 70    | 300           |
    Entonces el sistema almacena 2 lecturas y descarta 1 duplicadas

  Escenario: Reenvío de un lote ya sincronizado
    Dado la pulsera sincroniza un lote de lecturas de HR:
      | valor | hace_segundos |
      | 70    | 300           |
      | 72    | 240           |
    Cuando la pulsera sincroniza un lote de lecturas de HR:
      | valor | hace_segundos |
      | 70    | 300           |
      | 72    | 240           |
    Entonces el sistema almacena 0 lecturas y descarta 2 duplicadas

  Escenario: Ingesta rechazada por una lectura inválida
    Cuando la pulsera sincroniza un lote con una lectura de otra persona bajo cuidado
    Entonces el lote es rechazado como no procesable
