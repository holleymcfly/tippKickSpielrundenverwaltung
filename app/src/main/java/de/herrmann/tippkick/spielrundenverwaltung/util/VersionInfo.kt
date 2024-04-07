package de.herrmann.tippkick.spielrundenverwaltung.util

class VersionInfo {

    companion object {

        fun getCurrentVersionInfo(): String {
            return getVersion1_1_Info()
        }

        private fun getVersion1_1_Info(): String {

            return """
                <h1>Version 2.0:</h1>
                <br>
                <h3>Verbesserungen:</h3>
                <ul>
                    <li>Verlängerung und Elfmeterschießen können nicht mehr ohne gültiges Ergebnis gesetzt werden.</li>
                </ul>
                <br>
                <h3>Neue Funktionen:</h3>
                <ul>
                    <li>&nbsp;Gruppenwettbewerb</li>
                </ul>
                <br>
                <br>
                <h1>Version 1.1:</h1>
                <br>
                <h3>Fehlerbehebungen:</h3>
                <ul>
                   <li>&nbsp;Teamliste ist nicht mehr abgeschnitten.</li>
                   <li>&nbsp;Kein Absturz der App mehr, wenn ein Ergebnis ohne Zahl in beiden Feldern gespeichert wird.</li>
                   <li>&nbsp;Noch nicht gestartete Wettbewerbe konnten im Bereich "Spielen" ausgewählt werden, was zum einem Absturz der App führte.</li>
                </ul>
                <br>
                <h3>Verbesserungen:</h3>
                <ul>
                   <li>&nbsp;Vereins- und Wettbewerbseingabe kann nur noch einzeilig erfolgen.</li>
                   <li>&nbsp;Alle Dialoge sind "modal" und können nur noch über den Button geschlossen werden.</li>
                   <li>&nbsp;Beim Speichern eines Ergebnisses wird das aktuelle Datum mit gespeichert.</li>
                   <li>&nbsp;Ein einmal erfasstes Final-Ergebnis kann nicht mehr geändert werden.</li>
                </ul>
                <br>
                <h3>Neue Funktionen:</h3>
                <ul>
                   <li>&nbsp;Wettbewerbe können jetzt (mit allen zugehörigen Spielen) gelöscht werden.</li>
                   <li>&nbsp;Anzeige der Versionsinformation.</li>
                   <li>&nbsp;PDF-Erstellung von Wettbewerben ("Drucken").</li>
                </ul>
            """.trimIndent()
        }
    }
}