# Plan refaktoru mixinów

## Uzgodnione ograniczenia

- Zachowujemy jeden semantyczny injector ProcessingWire. Bez ordinali, lokalnych zmiennych bytecode i osobnych injectorów dla wariantów receptur.
- Zachowujemy obecne granice audytu: pomiar obejmuje również rozpoznawanie receptury. Nie przenosimy filtrów przed audyt w ramach tego refaktoru.
- Nie zmieniamy balansu ani przypiętych zależności.
- Testujemy publiczny efekt transformacji przewodów/pierścieni, nie prywatne metody ani treść adnotacji. Zakres zatwierdzony przez użytkownika.

## Kolejność

1. Zachowanie upstream: gettery generatorów i LCR przez ModifyReturnValue, stała konwersji pary przez ModifyConstant; ograniczenie liczby dopasowań obu stałych fill.
2. Delegowanie rejestracji przez WrapOperation, z zachowaniem celowego pomijania usuwanej receptury. Bez dodatkowych punktów zaczepienia dla poszczególnych receptur.
3. Przewody: wspólna operacja wstawienia folii, czytelne rozpoznawanie istniejących wariantów, bez nowej hierarchii strategii.
4. Pierścienie: pełniejsze rozpoznawanie wyniku i symbolu. Zachować granicę surowego Object[] wymaganą przez upstream.
5. Kontrola diffu, check, testy harnessu i pełna paczka serwerowa. Wyniki i ograniczenia dopisać po weryfikacji.

## Świadomie pominięte

Nie zmieniamy zachowania slotu obwodu: standardowe ścieżki GUI już sprawdzają allowSelectCircuit, a zmiana GUI wymaga osobnej weryfikacji klienta. Nie rozszerzamy selekcji circuit assemblera o wymyślone sygnatury: pozostaje nerf nano/quantum zgodny z obecną regułą. Nie dodajemy cache materiałów przed inicjalizacją rejestrów.

Konstruktory ogniw pozostają Redirectami: ich migracja wymagałaby osobnej zmiany mechanizmu dla niewielkiej logiki. Tooltip LCR nadal celowo zastępuje oryginalną operację. Accessory i poprawne, wąskie injectory pozostają bez zmian.

## Wdrożony zakres

- Usunięto wszystkie 6 Overwrite: 5 zastąpiono ModifyReturnValue, 1 ModifyConstant. Łączna liczba punktów integracji pozostaje taka sama; samych deklaracji injectorów jest teraz 22 zamiast 16, ponieważ zastąpiły nadpisania.
- Cztery Redirecty rejestracji/konfiguracji zastąpiono WrapOperation: SmallOres, CircuitAssemblerRecipes, CraftingMaterialLoader i MetaGeneratedItem01.
- ProcessingWire ma nadal jeden ModifyReceiver, bez zmian selektora. Helper ma wspólną operację wstawienia PPS i wspólne naliczenie modyfikacji, bez nowej hierarchii klas.
- Rozpoznawanie pierścieni sprawdza również prefiks wyniku i symbol X. Niewłaściwe dopasowanie nie zmienia tablicy składników.
- Zachowano granice audytów i balans. Doprecyzowano kontrakt obu stałych pary oraz dokumentację.

## Weryfikacja

- `-PmixinIntegrationTests spotlessApply check`: sukces; dodatkowy fixture Forge kompiluje się wyłącznie w buildzie testowym.
- 3 testy Python harnessu: sukces.
- Pełny serwer z GT5 5.09.54.51 i NHCore 2.9.21: świat załadowany, poprawne zatrzymanie, exit 0.
- Audyt tego serwera: 3006 modyfikacji, 36 dodań, 120 usunięć, 0 błędnych/pominiętych/niewykonanych zakresów — zgodnie z bazą.
- Testy integracyjne receptur w tym serwerze: PASSED. Obejmują rzeczywiste rejestry, trzy gumy, dwa rodzaje dielektryków, oba rozmiary receptury dielektrycznej, idempotencję PPS, ochronę obcych receptur i zachowanie danych receptury.
- Dowody lokalne: `build/mixin-integration-build.log`, `build/mixin-pinned-smoke.log`, `build/recipe-refactor/server-console.log`.
- Nie testowano interaktywnego GUI klienta ani wydajności. Z logów nie wynika obietnica przyspieszenia.
- Normalny `spotlessApply check build`: sukces, 42 testy JUnit bez błędów. Sprawdzono archiwum produkcyjne: nie zawiera klas fixture integracyjnego.
- Dodatkowo standardowy `runFullPackServer` pobrał nightly 2.9.0 z GT5 5.09.54.149 i NHCore 2.9.62. Ten zestaw również przeszedł audyt 3006/36/120, testy integracyjne, załadowanie świata i zatrzymanie z exit 0, bez crash reportu wykrytego przez harness. To dodatkowa obserwacja kompatybilności, nie zmiana przypiętych zależności.
- Pierwszą próbę standardowego harnessu przerwano po błędzie kodowania stdout CP1250. Powtórzenie z `PYTHONIOENCODING=utf-8` zakończyło się sukcesem. Dowody: `build/mixin-fullpack-utf8.log` i `build/fullpack/server-evidence/`.
