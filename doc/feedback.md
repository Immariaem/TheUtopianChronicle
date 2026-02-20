# Feedback Documentation - The Utopian Chronicle

## Feedback Session 1 - Code Feedback
**Date:** 2. Februar 2026  
**Feedback from:** Frank Schimmel

### Feedback received:

**Technical Issues:**
- Directory structure funktioniert nicht im JAR build
- State variables und Setter sollten nicht public sein, bessere Kapselung nötig

**Architecture & Design:**
- Session Scope für User-Daten implementieren
- Items: `use()`-Funktionalität sollte abhängig vom Item-Typ sein
- NPCs: Einfach halten aufgrund der verbleibenden Zeit
- Listen-Typen beachten: `List` vs `MutableList` bewusst wählen
- Generell auf Sichtbarkeit von Klassen/Methoden achten 

**Game Mechanics:**
- Ein Item aufheben und es dann aus der Welt entfernen
- Items sollten verwendbar sein (use-Funktionalität implementieren)

### Actions taken:

**Planned:**
- [ ] State variables auf private gesetzt, getter/setter überarbeitet
- [ ] Session Scope Konfiguration für User Sessions
- [ ] Item-Interface erweitert um typ-abhängige `use()`-Methode
- [ ] Liste vs MutableList Code-Review durchgeführt
- [ ] Visibility Modifiers überprüft und angepasst
- [ ] JAR build testen und Directory-Struktur fixen
- [ ] NPCs vereinfachen (Fokus auf essenzielle Interaktionen)
- [ ] Item aufheben und dann aus der Welt entfernen

**Not implemented:**
- tbd

## Feedback Session 2 - Design Feedback
**Date:** 24. Februar 2026  
**Feedback from:** Kjell & Conrad

### Feedback received:

### Actions taken: