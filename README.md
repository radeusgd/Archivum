# Archivum

Baza danych do zbierania i analizy danych demograficznych skupiona na 
galicyjskich księgach metrykalnych.

Schemat bazy zapisany jest w formacie JSON, zaś formularze
wpisywania/przeglądania danych oraz przeszukiwania można konfigurować za pomocą
plików XML.

Silnik bazy danych używa systemu [H2](http://h2database.com/), więcej informacji
o tej i pozostałych zależnościach programu jest dostępne w języku angielskim
w katalogu THIRD-PARTY.

## Uruchamianie

W związku ze sposobem w jaki baza danych jest dystrybuowana, użytkownik musi
mieć zainstalowany na systemie program Java Runtime Environment w wersji
przynajmniej 11, która obsługuje JavaFX. Istnieje wiele dystrybucji JRE
spełniających podane kryteria, jedną z dostępnych opcji jest
[Zulu OpenJDK](https://www.azul.com/downloads/zulu-community/?version=java-11-lts&package=jre-fx),
która zawiera wszystkie potrzebne komponenty i jest dostępna za darmo.

Po zainstalowaniu JRE, należy uruchomić odpowiedni skrypt.
Dla systemu Windows jest to `archivum_windows.bat`, zaś dla macOS oraz Linux
`archivum_macos_linux.sh`.
