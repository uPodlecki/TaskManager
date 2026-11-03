#  TaskManager

Nowoczesna aplikacja na system Android służąca do zarządzania zadaniami, organizowania codziennych obowiązków oraz monitorowania lokalizacji i załączników powiązanych z zadaniami. 

Aplikacja została zbudowana w języku **Kotlin** z wykorzystaniem **Jetpack Compose** oraz lokalnej bazy danych **Room**.

---

##  Funkcjonalności

- **Zarządzanie zadaniami:** Tworzenie, edycja, usuwanie oraz oznaczanie zadań jako ukończone.
- **Szczegóły i terminy:** Wybór daty wykonania zadania (`DueDatePicker`) oraz określanie priorytetów.
- **Filtrowanie i sortowanie:** Dynamiczne sortowanie listy zadań według kryteriów (`SortDropdown`).
- **Załączniki audio i pliki:** Możliwość nagrywania notatek głosowych (`AudioRecorder`) oraz dołączania plików do zadań (`TaskAttachment`).
- **Lokalizacja i Geofencing:** Przypisywanie współrzędnych do zadań oraz powiadomienia oparte na strefach geograficznych (`GeofenceHelper`, `LocationPickerScreen`).
- **Powiadomienia i harmonogram:** Lokalne powiadomienia o nadchodzących zadaniach (`NotificationScheduler`).
- **Widget na ekran główny:** Szybki podgląd zadań bezpośrednio z pulpitu telefonu (`TaskWidgetProvider`).
- **Motyw UI:** Nowoczesny interfejs w Jetpack Compose z obsługą motywu ciemnego i jasnego (Material 3).

---

##  Stos technologiczny

- **Język:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architektura:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Baza danych:** [Room Database](https://developer.android.com/training/data-storage/room) z wykorzystaniem `TypeConverters`
- **Nawigacja:** Jetpack Navigation Compose
- **Usługi lokalizacyjne:** Google Play Services Location (Geofencing)
- **Asynchroniczność:** Kotlin Coroutines & Flow
- **Narzędzie budowania:** Gradle (Kotlin DSL - `.gradle.kts`)

---

##  Struktura projektu

```text
app/src/main/java/com/example/taskmanagers/
├── data/
│   ├── local/          # Baza danych Room: Dao, Encje (Task, TaskAttachment, TaskLocation)
│   └── repository/     # Repozytoria pośredniczące w wymianie danych
├── navigation/         # Konfiguracja grafu nawigacji (NavGraph)
├── ui/                 # Ekrany Compose (TaskList, AddTask, Settings, Details itp.)
│   └── theme/          # Konfiguracja motywu, kolorów i typografii
├── utils/              # Narzędzia pomocnicze (Geofence, Audio, Powiadomienia, Widget)
├── viewmodel/          # Klasy ViewModel i fabryki ViewModelFactory
└── MainActivity.kt     # Główny punkt wejściowy aplikacji
```

---

##  Uruchomienie projektu lokalnie

### Wymagania wstępne
- Zainstalowane środowisko [Android Studio](https://developer.android.com/studio).
- Android SDK z min. API 26.
- Emulator lub fizyczne urządzenie z systemem Android.

### Klonowanie i instalacja

1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/uPodlecki/TaskManager.git
   cd TaskManager
   ```

2. Otwórz projekt w Android Studio (`File -> Open...` i wskaż folder z projektem).
3. Poczekaj na zsynchronizowanie projektu z plikami Gradle (`Sync Project with Gradle Files`).
4. Uruchom aplikację na emulatorze lub podłączonym urządzeniu:
   - Kliknij zielony przycisk **Run** (`Shift + F10`) w Android Studio.
   - Lub z poziomu terminala:
     ```bash
     ./gradlew assembleDebug
     ```

---

##  Wymagane uprawnienia

Aplikacja wykorzystuje następujące uprawnienia systemowe:
- `POST_NOTIFICATIONS` – powiadomienia o przypomnieniach.
- `RECORD_AUDIO` – nagrywanie notatek głosowych.
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` – geolokalizacja i obsługa stref Geofence.
- `ACCESS_BACKGROUND_LOCATION` – wyzwalanie stref lokalizacyjnych w tle.

---

##  Licencja

Projekt udostępniany na licencji MIT. Szczegóły w pliku `LICENSE`.
