---
layout:     post
title:      Daten Visualisierung
date:       2015-10-15
---

Bis anhin haben wir alle möglichen Daten, welche wir irgendwie bekommen konnten, in unsere erstellten Datenstrukturen gespeichert. Damit man bei dieser Unmenge an Daten davon verschohnt werden den Wald voller Bäume nicht mehr zusehen, stellen wir die wichtigsten Informationen visuell dar.

Nach einem kurzen Proof-of-Concept viel die Technologiewahl auf ein Java Swing Framework([JFreeChart](http://www.jfree.org/jsfreechart/)). Somit müssen wir keine weitere Sprache einführen und halten unseren Technologie-Zoo möglichst kompakt.

Zu Begin versuchten wir das `DataChart`-Objekt im `JavaPilotActor` zu instanzieren, da wir dort sehr nah bei den darzustellenden Daten waren. Anfangs liefen wir in eine HeadlessException, welche grundsätzlich bedeutet, dass man im momentanen Kontext kein Java Swing Frame erstellen kann. Als Workaround setzten wir das System-Property `headless` auf false, anschliessend konnte ein erstes DataChart(Gyro-Z) erstellt werden.

![Visualizer](/ChallP1/images/visualizer.png)

Das Swing Fenster dem `JavaPilotActor` anzuhängen, war längerfristig gesehen ein schlechter Entscheid! Es könnte ein Situation enstehen in welcher der Pilot auf die Swing Applikation warten müsste, was fatal wäre in einer Applikation, welche zu Runtime schnelle Berechnungen und Entscheidungen erledigen muss! Mit dieser Erkenntnis starteten wir ein Refactoring um eine möglichst asynchrone Verbindung zu initialisieren. Da wir nur in eine Richtung Daten senden müssen (`JavaPilotActor` -> `DataChart`), können wir auf einen Callback Mechanismus verzichten.

Aufbau der Schnittstelle *Pilot / Visualizer*:

1. `DataVisService`: -Wird vom Spring Framework Injected bei der Erstellung des Pilots
                     -instanziert das DataChart Objekt
                     -Hat die Permission Swing Fenster zu zeichnen
2. `PilotToVisualConnector`: -Implementiert das Interface `PilotToVisualConnection`
                             -Gibt dem Pilot die Möglichkeit dem `DataChart` Updates zu senden
3. `DataChart`: -Darstellung der Gyro-Z Werte(XY-Chart)
                -Darstellung der Velocity Werte(XY-Chart)
                -Tabellendarstellung der erkannten TrackSections
                -Positiosanzeige des Pilots auf der Strecke(Streckentabelle)
Mit diesem Setup kann der Pilot seine Berechnungen dem `DataVisService` mitteilen, welcher diese dann dem `DataChart` zur Darstellung weitergibt. Somit erreichen wir eine saubere Trennung von Datenverarbeitung und Darstellung und verhindern eine Blockade des Pilots!


![Gyro-Z](/ChallP1/images/visualize-gyro.gif)

![Carposition](/ChallP1/images/visualize-sections.gif)

