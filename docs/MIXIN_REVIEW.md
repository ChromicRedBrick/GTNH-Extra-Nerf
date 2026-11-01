# Przegląd mixinów — 2026-09-12

Stan: `master`, commit `854a655`. Przegląd obejmuje wszystkie 22 mixiny, ich rejestrację i trzy klasy pomocnicze. Nie zmienia zachowania moda.

## Wniosek

Najwięcej wartości da precyzyjniejsze rozpoznawanie receptur, ograniczenie pracy audytu dla wywołań niedotyczących nerfa i ograniczenie zastępowania całych operacji upstream. Nie ma uzasadnienia dla przebudowy wszystkich mixinów ani wspólnego frameworka do kilkunastu prostych podmian.

Najbardziej złożony jest `ProcessingWireRecipeTransformer`, nie sam `MixinProcessingWire`. Najbardziej kruche mechanizmy niekoniecznie mają dużo warunków: `MixinMetaTileEntity` nie ma żadnego, ale wybiera stałe wyłącznie po wartości.

## Jak liczyłem

- 6 mixinów early i 16 late.
- **16 deklaracji injectorów**: 7 Redirect, 2 Inject, 2 ModifyArg, 2 ModifyReceiver, 1 ModifyConstant, 1 ModifyReturnValue, 1 WrapOperation.
- Osobno: **6 Overwrite** oraz **4 mostki dostępu** (2 Accessor, 2 Invoker). Overwrite zastępuje metodę; accessor/invoker udostępnia pole/metodę. Nie są tym samym co punkt wstrzyknięcia instrukcji.
- Kolumna „miejsca” oznacza statyczne miejsca pasujące do celu w oryginalnym bytecode przypiętych JAR-ów GT5 `5.09.54.51` i NHCore `2.9.21`, odczytanym przez `javap -c -p`. Nie jest liczbą wykonań w grze ani dowodem zastosowania mixina po wszystkich transformacjach innych modów. `—` oznacza brak takiego pomiaru lub nieadekwatność do mostka/Overwrite.
- `L/D`: niepuste linie po usunięciu komentarzy i literałów tekstowych (włącznie z importami i adnotacjami) / liczba leksykalnych decyzji: `if`, `for`, `while`, `case`, `catch`, `&&`, `||` i operator warunkowy. To pomocniczy wskaźnik dla całego pliku, **nie cyclomatic complexity pojedynczej metody ani wynik Sonara**. Proste usuwanie literałów może usuwać również linie zawierające sam tekst; L nie służy porównywaniu produktywności.
- Ocena złożoności uwzględnia semantykę i helpery, nie tylko D.
- `defaultRequire: 1` w obu konfiguracjach obejmuje injectory bez jawnego `require`. `require` określa minimum. `require = 2` nie odrzuca trzech dopasowań.

## Wszystkie mixiny

Ścieżki odnoszą się do `src/main/java/pl/kuba6000/gtnhextranerf/mixins/`.

| Mixin | Deklaracje | Miejsca | L/D | Złożoność i zalecenie |
|---|---|---:|---:|---|
| early/MixinCraftingManagerAccessor | 1 Accessor | — | 10/0 | Minimalna. Zostawić. `List<Object>` to granica rejestru Minecrafta/RFB, nie model naszych reguł. |
| early/MixinRecipeAccessor | 2 Invoker | — | 14/0 | Minimalna. Zostawić. Stringowy target ma znaczenie dla ładowania interfejsu Minecrafta; nie zamieniać mechanicznie na class literal. |
| early/MixinShapedOreRecipeAccessor | 1 Accessor | — | 9/0 | Minimalna. Wąski dostęp do `mirrored`; zostawić. |
| early/MixinFMLClientHandler | 1 Inject | — | 17/0 | Minimalny kod, wrażliwe położenie. Wywołanie po `Loader.initializeMods()` musi pozostać wewnątrz obsługi wyjątków FML. Przeniesienie na RETURN może zepsuć ekran błędu. |
| early/MixinLoaderGTBlockFluid | 2 Redirect NEW | 2 + 31 | 58/5 | Niska logika, duża zależność od sygnatur konstruktorów. Rozważyć przechwycenie argumentu lub WrapOperation zgodny z używaną wersją MixinExtras; zachować selekcję po nazwie przedmiotu. |
| early/MixinSmallOres | 1 Redirect | 55 | 25/1 | Niska. Jeden nerf obsługiwany w wielu miejscach. Filtr nazwy przed otwarciem audytu; rozważyć WrapOperation. Nie wybierać arbitralnego ordinalu rudy. |
| late/MixinMTESteamTurbine | 1 ModifyReturnValue | 1 | 14/0 | Niska. Zostawić. `getEfficiency()` upstream zwraca zużycie pary, więc wzór `600 / efficiency` wymaga zachowania jednostek. |
| late/MixinMTEDieselGenerator | 1 Overwrite | — | 13/0 | Niska. Rozważyć ModifyReturnValue dla spójności i mniejszej ingerencji; poprawić nieaktualny komentarz o limicie 25%. |
| late/MixinMTEGasTurbine | 1 Overwrite | — | 13/0 | Niska. Jak diesel; komentarz o limicie 25% również nie opisuje obecnego wzoru. |
| late/MixinMTEPlasmaGenerator | 1 Overwrite | — | 13/0 | Niska. Ten sam kierunek co pozostałe generatory; zachować przesunięcie progresji od EV. |
| late/MixinMTESemiFluidGenerator | 1 Overwrite | — | 13/0 | Niska. Ten sam kierunek; brak potrzeby nowej abstrakcji ponad `GeneratorEfficiency`. |
| late/MixinSteamOverclockDescriber | 1 Overwrite | — | 11/0 | Minimalna. Upstream robi `2 * eu`, mod `10 * eu`. Wąskie ModifyConstant dla stałej long jest kandydatem; nie mnożyć wyniku upstream bez analizy przepełnienia. |
| late/MixinMTELargeChemicalReactor | 1 Overwrite + 1 Redirect | — + 1 | 24/0 | Niska logika, średnia kruchość. Modyfikować zwrócony ProcessingLogic przez `setOverclock(3,4)` zamiast konstruować własny od zera. Tooltip jest celowym zastąpieniem informacji. |
| late/MixinMetaTileEntity | 1 ModifyConstant | 2 | 12/0 | Minimalna logika, istotny kontrakt selektora. Doprecyzować deskryptor `fill` i maksymalną liczbę dopasowań. Obecne dwie stałe to dzielenie i mnożenie zużycia pary. |
| late/MixinConfigurationCircuitSupport | 1 Inject HEAD cancellable | — | 48/7 | Średnia wraz z handlerem. Przywrócić lokalny warunek `allowSelectCircuit()`, uprościć powtórzone odczyty inventory, nazwać mixin zgodnie z targetem CommonWidgets. |
| late/MixinProcessingWire | 1 ModifyReceiver | 40 | 28/0 | Wysoka wraz z helperem 169/49. Najważniejszy obszar do uporządkowania rozpoznawania wariantów; sam typ injectora jest odpowiedni. |
| late/MixinCircuitAssemblerRecipes | 1 Redirect | 42 | 53/10 | Średnia. Jasno określić kontrakt selekcji nano/quantum, sprawdzać wejścia przed mutacją, uprościć ścieżkę delegowania. Kandydat do WrapOperation. |
| late/MixinMetaGeneratedItem01 | 1 Redirect | 54 | 72/20 | Średnia. Dokładna sygnatura receptury silnika uzasadnia liczne warunki. Zachować rozpoznawanie; helpery oznaczyć Unique i nadać jednoznaczne nazwy. WrapOperation może zachować łańcuch przy delegowaniu. |
| late/MixinMaterialsInit | 2 ModifyArg | 1 + 1 | 27/0 | Niska. Dobry wąski punkt ingerencji. Dwa krótkie podobne callbacki nie uzasadniają frameworka. |
| late/MixinProcessingShaping | 1 WrapOperation | 1 | 38/5 | Niska/średnia. Już używa `original.call`; nie wymaga migracji z Redirect. Można spłaszczyć zagnieżdżenia, zachowując dokładnie jedną podmianę stick. |
| late/MixinCraftingMaterialLoader | 1 Redirect | 5 | 48/10 | Średnia. Doprecyzować wynik jako ring i symbol jako X. Obecnie rozpoznaje głównie kształt i stick. Uprościć delegowanie, rozważyć WrapOperation. |
| late/MixinAssemblyLineRecipes | 1 ModifyReceiver | 45 | 64/16 | Średnia. Selekcja konkretnego wyniku i pojedynczego wejścia jest sensowna; liczba warunków nie jest sama w sobie wadą. Filtr mapy/wyniku przed audytem, z zachowaniem wykrywania brakującej zmiany. |

## Ustalenia i priorytety

### 1. Precyzja selektorów i kontraktów — pierwsza kolejność

`MixinMetaTileEntity`, linia 12: `@ModifyConstant(method = "fill", intValue = 2, require = 2)` trafia obecnie w obie właściwe stałe. Nie stwierdzam obecnego błędu przeliczania. Przy zmianie upstream trzecia, niezwiązana stała 2 również mogłaby zostać podmieniona. Pełny deskryptor i `allow = 2` obok `require = 2` dają prostą kontrolę tego przypadku. Same liczniki nie dowodzą jednak semantyki: zamiana jednej stałej na inną nadal wymaga przeglądu upstream.

`MixinCraftingMaterialLoader`, linie 29–45: warunek akceptuje dowolny Character w definicji symbolu i nie potwierdza prefiksu ring wyniku. W odczytanym upstream obecny wzorzec `h /fX` rzeczywiście rejestruje ring, więc to luka odporności na kolejne receptury, nie dowód aktualnego nerfa złego przedmiotu. Dodać warunki semantyczne, zachować obsługę materiałów Werkstoff.

`MixinConfigurationCircuitSupport`, linie 27–35: pomija `allowSelectCircuit()`, które sprawdza CommonWidgets. Standardowe ścieżki `MTETieredMachineBlockBaseGui.doesAddCircuitSlot` i `GTBaseGuiBuilder.createGhostCircuitSlot` sprawdzają ten warunek wcześniej. Nie ma zatem podstaw, by na tej podstawie ogłaszać obecny błąd GUI. Mimo to helper powinien zachować kontrakt przy bezpośrednim wywołaniu. Czyszczenie zapisanych phantom stacków musi pozostać po stronie serwera.

`MixinCircuitAssemblerRecipes`, linie 35–58: selekcja sprawdza pierwszy output, dopuszcza kolejne outputy, a następnie mnoży wszystkie niepuste wejścia. To jest poprawne tylko przy intencji „wszystkie takie receptury”. Jeśli nerf ma dotyczyć konkretnych wariantów, należy opisać ich sygnatury i audytować oczekiwaną liczbę zmian. Brak null-check dla całej tablicy wejść to zależność od kontraktu buildera, nie potwierdzony crash. Nie zamieniać nieprawidłowego celu w ciche pominięcie.

### 2. Przewody i praca podczas startu — największy potencjał porządkowania

`ProcessingWireRecipeTransformer`: 169 niepustych linii, 49 decyzji leksykalnych. Obejmuje odrębne warianty: circuit 24, izolację płynami oraz kombinacje pyłu dielektryka i gumy. Rozdzielić rozpoznanie wariantu od jednej operacji wstawienia folii PPS. Najpierw tanie warunki mapy/prefixu/materiału, następnie struktura wejść, na końcu rozwiązywanie materiałów i tworzenie stacków. Małe nazwane metody dla wariantów wystarczą; rejestr interfejsów nie jest automatycznie potrzebny dla trzech zamkniętych przypadków.

Mixin przewodów otwiera audyt przed filtrami helpera. Podobnie AssemblyLine, SmallOres i kilka Redirectów. Audyt to więcej niż lambda: otwarcie/zamknięcie uchwytu, aktualizacja statystyk, pomiar czasu. Można ominąć tę pracę dla oczywiście obcych wywołań. Trzeba zachować końcowe wykrywanie braku oczekiwanej zmiany oraz świadomie ustalić znaczenie liczników sukcesów; zmiana granicy audytu zmieni ich wartości.

40 miejsc bytecode przewodów nie oznacza 40 wykonań przy starcie: `registerOre` jest wywoływane dla wielu materiałów i prefixów, a gałęzie wybierają różne miejsca. Analogicznie 45 miejsc Assembly Line nie jest liczbą zmienianych receptur. Pomiar czasu i alokacji podczas uruchamiania paczki powinien poprzedzić twierdzenie o odczuwalnym przyspieszeniu.

Nie priorytetyzować zamiany małych list 20 materiałów i 6 prefixów na hashe. `Materials` nie jest zwykłym enumem do EnumSet. Ewentualny cache opisów płynów musi powstać po inicjalizacji rejestrów; statyczne `getMolten` może uruchomić się za wcześnie.

### 3. Mniejsza ingerencja w upstream — druga kolejność

Cztery generatory nadpisują krótkie gettery. Odczytany upstream zwraca pole lub proste wyrażenie, bez efektów ubocznych. ModifyReturnValue pozwala zachować metodę i jest spójny z turbiną parową. Nie jest to obietnica przyspieszenia: oryginalne wyrażenie nadal się wykona, a konflikt wartości z innym modem nadal może zależeć od kolejności transformacji.

LCR upstream tworzy dokładnie `new ProcessingLogic().enablePerfectOverclock()`. Obecny Overwrite nie gubi dziś dodatkowej walidacji. Modyfikacja zwróconego obiektu przez `setOverclock(3,4)` ograniczy jednak ryzyko utraty nowych zachowań dodanych upstream. `enablePerfectOverclock()` samo używa `setOverclock(4,4)`, więc kierunek ma oparcie w aktualnej implementacji.

Redirect przejmuje operację na wyłączność. WrapOperation ma sens tam, gdzie kod chce zmienić argumenty lub wykonać pracę wokół oryginału, a potem delegować przez `original.call`. Dotyczy zwłaszcza rejestracji craftingu i circuit assemblera. Samo przepisanie adnotacji bez użycia przekazanej operacji niczego nie poprawi. Celowo usuwana receptura nadal wymaga niedelegowania. Podmiana tooltipa też celowo zastępuje operację — WrapOperation nie zagwarantuje współpracy, jeśli oryginał jest pomijany.

### 4. Małe poprawki utrzymania

- Usunąć fałszywy opis „cap at 25%” w diesel/gas. `GeneratorEfficiency` ma progresję rosnącą powyżej 25%; kod i dokumentacja muszą opisywać tę samą regułę.
- Oznaczyć prywatne pomocnicze metody `MixinMetaGeneratedItem01` jako Unique. To zabezpieczenie nazw w klasie docelowej, nie wykryta kolizja.
- Dopasować nazwę `MixinConfigurationCircuitSupport` do targetu CommonWidgets.
- W rejestracji mixinów sprawdzić spójność zależności NHCore: late wymaga GT5 i wyklucza GT6, a targety NHCore nie używają istniejącego DREAMCRAFT. Główny mod wymaga dreamcraft, więc nie jest to obecnie dowód błędu ładowania. Warto wyrazić zależność w jednym spójnym modelu.

## Performance i granice optymalizacji

Większość omawianych callbacków działa podczas rejestracji, nie co tick. Ich uproszczenie może pomóc czasowi startu i ilości alokacji, ale nie daje podstaw do obietnicy większego FPS/TPS.

Generatory i przeliczanie pary są ścieżkami runtime. Obecne wzory są O(1), bez pętli i tworzenia obiektów. `GeneratorEfficiency` (13/0) już skupia wspólną regułę; nie potrzebuje cache ani strategii obiektowej dla każdego wywołania.

`PhysicalCircuitItemStackHandler` (79/13) ma uzasadnione rozgałęzienia insert/extract/simulate i istniejące testy publicznego zachowania. Nie usuwać ich dla mniejszego wyniku complexity. Kopie ItemStack są częścią bezpieczeństwa własności danych. GUI tworzy wrapper przy budowie slotu, nie w generatorowym przeliczeniu energii.

Przechwytujące lambdy mogą wymagać alokacji, choć JIT może część usunąć. Ich mechaniczne zastąpienie klasami anonimowymi nie rozwiązuje problemu. Najpierw zmniejszyć liczbę zbędnych wejść do audytu; dopiero pomiary uzasadnią dalsze zmiany mechanizmu.

## Proponowana realizacja i weryfikacja

1. Mały pakiet precyzji: selektor pary, semantyka ring, lokalny kontrakt slotu, aktualne komentarze. Bez zmiany balansu.
2. Przewody: nazwać przypadki, oddzielić dopasowanie od mutacji, ograniczyć audyty obcych wywołań. Zachować wynik i wykrywanie braków.
3. Osobno migracja uzasadnionych Overwrite/Redirect. Zmienia mechanizm integracji, więc wymaga sprawdzenia na rzeczywistym zestawie modów.
4. Drobna kosmetyka tylko przy dotykanych plikach.

Weryfikacja implementacji powinna obejmować publiczne efekty transformacji (w tym brak zmiany obcych i niejednoznacznych celów), zachowanie slotu oraz porównanie audytu i końcowych receptur z bazą. Do liczby miejsc wstrzyknięcia potrzebne jest uruchomienie z faktycznymi transformerami; unit test helpera tego nie dowodzi. Do zmian GUI potrzebny klient, a do rejestracji pełna wspierana paczka. Wydajność porównywać na tym samym środowisku, z kilkoma uruchomieniami.

Ten przegląd obejmuje statyczne źródła i kontrolę bytecode wybranych celów o szerokich selektorach. Nie uruchamiałem w jego ramach gry, benchmarku ani nowej serii testów. Nie jest deklaracją przetestowania wszystkich zachowań runtime.
