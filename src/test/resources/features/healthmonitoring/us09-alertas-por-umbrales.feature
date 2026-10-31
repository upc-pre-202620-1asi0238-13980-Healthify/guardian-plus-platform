# language: es
@US09
Característica: Generación de alertas por transgresión de umbrales biomédicos
  Como familiar o cuidador
  Quiero que el sistema notifique cuando los signos vitales excedan los rangos seguros configurados
  Para intervenir preventivamente

  Antecedentes:
    Dado una persona bajo cuidado con una pulsera asignada
    Y el rango normal de HR es de 60 a 100

  Escenario: Superación persistente de umbrales clínicos
    Cuando la pulsera transmite las lecturas de HR: "120, 125, 130"
    Entonces se notifica a Emergency & Alerting una anomalía de HR ABOVE_RANGE

  Escenario: Lecturas fuera de rango no consecutivas no generan alerta
    Cuando la pulsera transmite las lecturas de HR: "120, 125, 80, 130"
    Entonces no se notifica ninguna anomalía

  Escenario: Una misma racha de anomalías genera una sola alerta
    Cuando la pulsera transmite las lecturas de HR: "120, 125, 130, 135, 140"
    Entonces se notifican 1 anomalías a Emergency & Alerting
