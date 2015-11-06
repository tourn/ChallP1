---
layout:     post
title:      (WIP) Testfahrt auf der echten Bahn
date:       2015-11-06
---

Nachdem wir Vormittags noch kurz einen Logger zusammengebaut haben, der unsere Fahrtergebnisse als JSON abspeichern kann, konnten wir heute unsere ersten Testfahrten auf der echten Carrerabahn durchführen. Dabei haben wir einige Dinge gelernt.

### Allgemein
* Runden Start/Ende-Event abfangen und Zustand entsprechend anpassen (z.B beim Rundenanfang loeschen)

### Trackanalyzer
* Funktionioniert im Moment mit RoundEndMessage - Stattdessen sollte er mit Cycle Detection arbeiten
* Autos haben verschiedene Physikalische eigenschafften; Motorstaerke und Reibung unterscheiden sich. Physische eigenschafften sollen nach der Lernphase erraten werden.

### SpeedOptimizer
* Auf Penalties muss reagiert werden
* History von vergangenen Runden soll auf aktuelle Fahrstrategie mit einwirken
  * Ein Historyelement pro TrackSection sollte beinhalten
    * Eingangsgeschwindigkeit
    * Verwendete Power
    * Punkt (Prozentual), ab dem gebremst wird
    * Durchquerungszeit
    * Wurde ein Penalty kassiert?
