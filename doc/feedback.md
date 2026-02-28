# Feedback Documentation - The Utopian Chronicle



<details>
<summary><strong>Feedback Session 1 – Code Feedback</strong> · Frank Schimmel · 2. Februar 2026</summary>

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

**Fixed:**
- [x] Session Scope Konfiguration für User Sessions (`@SessionScope` auf GameController)
- [x] JAR build testen und Directory-Struktur fixen (Classpath-basierter QuadrantLoader)
- [x] Item aufheben und dann aus der Welt entfernen (InventoryHandler entfernt Item aus Quadrant)
- [x] Item-Interface erweitert um typ-abhängige `use()`-Methode (UseHandler prüft itemType)
- [x] NPCs vereinfachen (Fokus auf essenzielle Interaktionen)

**Not implemented (scope/time):**
- [ ] State variables auf private gesetzt, getter/setter überarbeitet
- [ ] Liste vs MutableList Code-Review durchgeführt
- [ ] Visibility Modifiers überprüft und angepasst

</details>

<br>

<details>
<summary><strong>Feedback Session 2 – Playtesting Feedback</strong> · Ina Emmerich (meine Mutter / keine technischen Kenntnisse) · 22. Februar 2026</summary>

### Feedback received:

**UI / Design:**
- "Begin the Journey" Button sieht anders aus als die Command Line (andere Form/Style)
- Intro-Text zu klein
- Karten-Icon nicht klar als Karte erkennbar
- Inventory-Icons nicht erkennbar weil Pixel-Art zu klein

**UX / Spielbarkeit:**
- Commands-Anleitung am Anfang fehlt (Spieler weiß nicht wie man spielt)
- Descriptions zu lang, kürzen
- Karte nicht hilfreich genug, eigene Position auf der Karte nicht sichtbar
- Aktuelle Mission / Quest-Status nicht sichtbar
- es sollte mehr Wasser unterwegs geben

**Bugs:**
- Fehlermeldung bei `use`,`talk` und `take` ohne Argument zeigt keinen Hinweis auf `use <name>` / `talk <name>`/`take <name>`
- "Stream Water" hat eine komische/unpassende Description
- Verschiedene Wasser-Items können nicht mit dem generischen Begriff "water" aufgenommen werden
- Flags werden beim Tod nicht zurückgesetzt
- Crystal Spring sorgt für Verwirrung, weil man es nicht trinken kann
- Es passiert nichts wenn Crystal Keys gedroppt werden statt used (in der Singing Chamber)

**Feature Requests:**
- Text-to-Speech / vorlesen Funktion wäre toll

### Actions taken:

**Fixed:**
- [x] Crystal Keys drop in Singing Chamber funktioniert jetzt genauso wie use
- [x] Fehlermeldungen für `use`, `take` und `drop` zeigen jetzt `use <name>` / `take <name>` Hinweis
- [x] Stream Water Description überarbeitet (kein falscher Crystal Spring Verweis mehr)
- [x] Wasser-Items mit generischem Begriff "water" aufnehmbar (contains-Fallback in take/drop/use)
- [x] Relevante Flags beim Tod zurückgesetzt (crystal_keys_complete, labyrinth, guardian state)
- [x] Crystal Spring gibt jetzt +5 Hydration wenn interagiert
- [x] Intro-Text Schriftgröße erhöht (13px Text, 14px Quote)
- [x] "Begin the Journey" Button in Command Wrapper integriert (gleicher Clip-Path/Style)
- [x] Karten-Icon ersetzt (neues zentriertes SVG Pin-Icon)
- [x] Eigene Position auf der Karte anzeigen (mathematisch aus Quadrant-ID berechnet)
- [x] Commands-Anleitung: Idle-Hints im Command Input + Hinweis in Forest Entrance Beschreibung
- [x] Quest-Tracker / aktuelle Mission anzeigen

**Not implemented (scope/time):**
- [ ] Text-to-Speech

</details>

<br>

<details>
<summary><strong>Feedback Session 3 – Playtesting Feedback</strong> · Anna Michnia (meine Freundin / keine technischen Kenntnisse) · 23. Februar 2026</summary>

### Feedback received:

**Bugs:**
- Es gab mal zwei verschiedene Beerentypen (vom Icon her) im Spiel, jetzt gibt es nur noch einen
- Father's Campsite (E4) hat Wüstenmusik und Wüstenhintergrund, obwohl es sich in den Höhlen befindet
- Der Mining Lift (B6) hat Himmels-Musik und Himmels-Hintergrund, obwohl er noch in den Höhlen ist, sollte erst ab dem Sky Docks-Bereich erscheinen
- Die Lautstärke der Musik ist in jeder Zone unterschiedlich laut, sollte einheitlich sein
- Die Wasser-Trinknachrichten sind ortsgebunden, wenn man Höhlenwasser aufnimmt und es in Nephelia trinkt, ergibt die Nachricht keinen Sinn mehr
- Die Himmelskarte erscheint zu spät, sollte bereits beim Betreten der Sky Docks erscheinen, nicht erst nach dem Gespräch mit Zara
- Nachdem man von Zaras Schiff ins Meer geworfen wird, kann man sich frei bewegen ohne zu tauchen, man sollte stattdessen eine Nachricht bekommen, die erklärt, dass man tauchen muss
- Der `dive`-Befehl fehlt in der Hilfe-Funktion
- `use Diving Platform` zeigt eine Tauchnachricht, aber nur `dive` taucht wirklich, die Interaktion ist verwirrend
- Beim Tod in der Unterwasserwelt wird man in der Sky Platia (Nephelia) respawnt statt am Dive Point (E8)
- Nach dem Respawn in Nephelia waren alle Nephelia-Flags gelöscht, was den Fortschritt blockiert
- Zu wenig Wasser auf dem Weg vom Ozean in die Wüste (H8 -> F5), der Spieler verliert 10 Hydration auf 5 Zügen ohne Wasserquelle

### Actions taken:

**Fixed:**
- [x] Beeren in Forest Entrance umbenannt zu "Forest Berries" für klare Unterscheidung
- [x] E4 Father's Campsite korrekt als Höhlenzone klassifiziert (Musik/Hintergrund)
- [x] B6 Mining Lift korrekt als Höhlenzone klassifiziert (Musik/Hintergrund)
- [x] Himmelskarte erscheint jetzt beim ersten Betreten der Sky Docks (A6)
- [x] Wasser-Trinknachrichten für Höhlenwasser-Items generisch umgeschrieben
- [x] Bewegungssperre bei E8: Spieler kann sich auf der Ozeanoberfläche nicht bewegen ohne zu tauchen
- [x] `dive`-Befehl zur Hilfe-Funktion hinzugefügt
- [x] Diving Platform Interaktion auf `dive`-Befehl hingewiesen statt falscher Tauchmeldung
- [x] Checkpoint beim Benutzen des Storm Chasers auf E8 gesetzt (korrekter Respawn-Punkt)
- [x] Nephelia-Flags bleiben nach dem Tod in der Unterwasserwelt erhalten (durch korrekten Checkpoint)
- [x] Wasser und Nahrung auf dem Weg vom Ozean in die Wüste ergänzt (Spring Water in F7, Fresh Water in G7, Cactus Fruit in F5)

**Not implemented (scope/time):**
- [ ] Lautstärke der Musikdateien angleichen (erfordert Normalisierung der Audiodateien selbst)

</details>

<br>

<details>
<summary><strong>Feedback Session 4 – Design Feedback</strong> · Kjell Wistoff · 24. Februar 2026</summary>

### Feedback received:

**Positiv (beibehalten):**
- Kartenaufbau und -layout passt gut so, nichts ändern
- Hintergrundbild ist gut gewählt, die fehlenden Bereichsillustrationen sollen im gleichen Stil dazugemalt werden
- Serifen-Schriftart ist die richtige Wahl für das Buchdesign

**Layout / Typografie:**
- Der Einführungstext sollte in zwei Spalten aufgeteilt werden, entsprechend der vertikalen Linie in der Buchmitte (Buchspiegel/Falz)
- Überschriften wirken zu flach, ein sehr leichter Text-Shadow mit Blur würde sie hervorheben und besser lesbar machen
- All-Caps-Schriftart überdenken: eventuell nicht durchgehend Großbuchstaben, Lesbarkeit prüfen

**UI / Design:**
- Box-Shadow der Inventory-Slots aus dem Figma-Design noch nicht übertragen, implementieren
- Die Inventory-Slot-Boxen sind in HTML manuell wiederholt, mit JavaScript dynamisch generieren, um Redundanz im Code zu vermeiden
- Aktueller Positionsmarker auf der Karte (roter Punkt) durch ein mittelalterliches Kartennadel-SVG ersetzen (historischer Map Pin im Stil einer Pergamentkarte) vielleicht

**Karte:**
- Die Kanten der Kartenausschnitte sollen leicht weichgezeichnet/ausgeblendet werden, damit die Übergänge nicht so hart aussehen
- Die äußeren Ränder der Karte müssen noch "ausgemalt" / gestaltet werden, sie sollen nicht so abgeschnitten wirken
- Alternative ausprobieren: Karte in CSS als Hintergrund mit Masking einbinden statt aktuellem Ansatz

**Browser-Kompatibilität:**
- Layout sieht in Opera anders aus als erwartet, Cross-Browser-Kompatibilität prüfen

### Actions taken:

**Fixed:**
- [x] Box-Shadow der Inventory-Slots implementiert (inset shadow aus Figma-Design übertragen)
- [x] Positionsmarker auf der Karte durch Icon-Bild ersetzt (locationOnMapIcon.png)
- [x] Kartenränder weichgezeichnet (CSS mask-image mit radial-gradient)
- [x] All-Caps-Schriftart evaluiert, eine weitere Schriftart hinzugefügt
- [x] Text-Shadow mit Blur auf Überschriften hinzufügen

**Not implemented (scope/time):**
- [ ] Einführungstext in zwei Spalten aufteilen (links/rechts der Buchmitte)
- [ ] Inventory-Slot-Boxen per JavaScript-Loop generieren statt hartkodierter HTML-Wiederholung
- [ ] Opera-Rendering-Unterschied untersuchen und beheben (Ich glaube es liegt an meinem Opera Theme)

</details>

<br>

<details>
<summary><strong>Feedback Session 5 – Code Review</strong> · Marcelo Emmerich (mein Vater / Software Engineer) · 27. Februar 2026</summary>

### Feedback received:

**Positiv (beibehalten):**
- Projektstruktur für das erste Semester sehr durchdacht
- Saubere Trennung zwischen Controller, Spiellogik und den einzelnen Handlern
- Spielwelt in JSON-Dateien ausgelagert statt hartcodiert, zeigt Verständnis für gutes Software-Design
- Frontend mit Pixel-Art Icons, Audio-Effekten, Command-History und Credits-Sequenz geht weit über eine normale Studienarbeit hinaus

**Bugs:**
- Fehlendes `return` im UseHandler beim Storm Chaser: nach der Teleportation läuft der Code weiter
- `examine` sucht nach technischer Item-ID statt nach dem Namen, den der Spieler sieht
- Code-Duplikation an einigen Stellen (z.B. Kristallschlüssel-Logik)

**Sonstiges:**
- Dev-Tools wie `warp` und `flags` sollten vor der Abgabe entfernt werden

### Actions taken:

**Fixed:**
- [x] Fehlendes `return` im UseHandler nach Storm Chaser Teleportation ergänzt
- [x] `examine` Item-Suche verifiziert - sucht bereits korrekt nach `itemName`, kein Bug in aktueller Version
- [x] Dev-Tools (`warp`, `flags` Befehle) vor der Abgabe aus GameEngine entfernen

**Not implemented (scope/time):**
- [ ] Code-Duplikation in Kristallschlüssel-Logik reduzieren (UseHandler / InventoryHandler)

</details>
