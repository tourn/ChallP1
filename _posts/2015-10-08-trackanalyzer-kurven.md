---
layout:     post
title:      TrackAnalyzer und Kurvenerkennung
date:       2015-10-08
---

Eine einfache Idee zur Streckenerkennung hatten wir relativ schnell: Sobald der Z-Gyro-Sensor einen gewissen Wert positiv/negativ überschreitet, befinden wir uns in einer Links- bzw. Rechtskurve. Es war schon recht befriedigend, dem Piloten bei der Kurvenerkennung zuzuschauen, aber noch weit weg vom eigentlichen Erlernen der Strecke.

Die Strecke wird folgendermassen gespeichert: Jede Kurve und jedes Geradensegment dazwischen wird als einzelnes Objekt, eine `TrackSection`, gespeichert. Dieses enthält die Durchfahrungszeit, Richtung (Links/Rechts/Gerade), eventuell eine Bewertung der Kurvenschärfe und einen Zeiger auf die nächste `TrackSection`. Die geschwindigkeitsmessenden Checkpoints sowie die Position des Autos enthalten einen Zeiger auf das `TrackSection`, in dem sie sich befinden, sowie einen zeitlichen Offset vom Start des Abschnitts.

![tracksections](/ChallP1/images/trackdata.png)

Gelernt wird die Strecke in einer sogenannten Kennenlernphase, wo das Auto mit einer konstanten Geschwindigkeit um die Bahn kurvt, um all diese Abschnitte zu analysieren. Aus den mehreren Runden wird für eine höhere Verlässlichkeit dann ein Durchschnitt berechnet. Dies bringt mehrere Problemstellungen mit sich:

1. Der Beginn der ersten Runde ist nicht wirklich repräsentativ, da das Auto zuerst beschleunigen muss. Hier bieten sich verschiedene Lösungen an, wovon keine so wirklich toll klingt:
    - Ignorieren des Problems
    - Ignorieren der ersten Runde
    - Versuchen, die Beschleunigungsphase irgendwie herauszurechnen
2. Wie oft soll die Strecke umfahren werden? Ist die Geschwindigkeit zu hoch, fliegt das Auto aus der Bahn (bzw. erhält es Penalties), was die Daten der Umfahrung wesentlich weniger nützlich macht.
  - Diese Frage steht noch offen, es soll ja möglichst viel Zeit für die eigentliche Fahrphase zur Verfügung stehen. Momentan experimentieren Wir mit 1-3 Runden.
3. Wie werden die Daten von mehreren Runden zusammengeführt? Theoretisch sollten mit derselben konstanten Geschwindigkeit in jeder Runde gleich viele `TrackSections` vorkommen. Die Praxis zeigt aber, dass durch das Rauschen der Sensoren insbesondere in schnellen Abfolgen von kurzen Kurven gerne einmal ein Zwischenstück einfach als Gerade erkannt wird.
  - Unsere neue Idee plant die Vereinfachung von mehreren, kurz aufeinanderfolgenden Kurvensegmenten in ein langes. Dies führt auch dazu, dass die Kurvenrichtung verlorengeht, was aber eigentlich nicht so wichtig ist. Das Ziel ist ja einfach, in Kurven vorsichtiger zu fahren. Stimmen die Daten von mehreren Runden nicht überein, so wird einfach die Lösung mit weniger Abschnitten genommen.
