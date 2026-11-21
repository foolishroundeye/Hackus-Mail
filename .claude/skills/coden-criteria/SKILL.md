---
name: coden-criteria
description: Posuzuje kritéria pokrytí v tomhle projektu a zapisuje výsledek do tests/docs/criteria.md pro mobilní aplikaci coden. Použij, když máš sestavit nebo obnovit přehled kritérií, nebo když se ptají, jak náročné je něco otestovat.
---
<!-- coden-skill-verze: 4 -->

# Kritéria pokrytí v projektu

Aplikace nabízí **pevný katalog obecných kritérií pokrytí** — Edge coverage,
MC/DC, pair-wise a další. Ta se nevymýšlejí a nepřepisují: „Edge coverage"
znamená totéž ve všech projektech.

Tvoje práce je to, co obecné být nemůže: **kde v tomhle projektu jednotlivá
kritéria dávají smysl a jak jsou tu náročná.** Uživatel si podle toho vybírá,
co si nechá pokrýt.

## Kořen projektu

Všechny cesty v tomhle skillu (`tests/docs/criteria.md`) jsou od **kořene projektu** — složky,
nad kterou běží Server. Její absolutní cesta je v proměnné prostředí
`CODEN_PROJECT_DIR`; chybí-li, je to pracovní složka, ve které Konverzace
začala.

Nikdy ne od kořene gitu ani od podsložky, kam ses přesunul (třeba kvůli
spuštění testů). Leží-li kód v podsložce, třeba `projekt/`, soubory stejně
patří do `$CODEN_PROJECT_DIR/tests/docs/criteria.md`, ne do `projekt/tests/docs/criteria.md`. Jinde je
aplikace nehledá a zapsané by neviděla.

Zapisuj proto vždy absolutní cestou od `$CODEN_PROJECT_DIR`.

## Kam se zapisuje

```
tests/docs/criteria.md
```

Jeden soubor na projekt. Přepisuje se celý, ne po částech.

## Tvar souboru

````markdown
# Kritéria pokrytí v tomhle projektu

Aktualizováno: 2026-09-06

| Kritérium | Oblast | Náročnost | Proč |
|:---|:---|:---|:---|
| Node coverage | celý projekt | Malé | Tři moduly bez větvení, zbytek pokryjí testy tak jako tak. |
| Edge coverage | Přihlášení, Platby | Střední | Pět větví u ověření hesla, dvě jen při vypršené relaci. |
| Prime path coverage | Router | Vysoké | Cykly v obsluze přesměrování dávají devět nezkratitelných cest. |
| Modified condition/decision coverage | Slevy | Extrémní | Čtyři klauzule, MC/DC dá osm případů a dva z nich jdou jen přes API. |
| Pair-wise coverage | Katalog | Střední | Tři charakteristiky po třech blocích; všechny kombinace by daly 27 testů, dvojice devět. |
| Zátěžový test | Seznam objednávek | Vysoké | Bez testovacích dat v řádu desítek tisíc řádků se úzké místo neprojeví. |
````

Pravidla, na kterých závisí čtení v aplikaci:

- **Sloupec `Kritérium` musí obsahovat název z katalogu**, doslova. Cokoliv
  jiného aplikace nespáruje a poznámka se u kritéria neukáže.
- `Oblast` je krátce, kde to v projektu uplatnit. Piš jména, která uživatel
  v projektu vidí — obrazovky, moduly, koncové body.
- `Náročnost` je právě jedna z hodnot **Malé / Střední / Vysoké / Extrémní**.
- `Proč` je povinné a je to ta vlastní informace. **Piš čísla, která jsi
  napočítal** — kolik větví, klauzulí, bloků, du-párů. Obecná věta typu
  „hodně stavů" uživateli nepomůže rozhodnout.

## Katalog kritérií

| Kategorie | Kritéria |
|:---|:---|
| Tok programu | Node coverage, Edge coverage, Prime path coverage |
| Výkon | Baseline test, Zátěžový test, Stress test, Test stability |
| Logické podmínky | Predicate coverage, Clause coverage, Combinatorial coverage, Modified condition/decision coverage |
| Požadavky | Cause-effect graphing |
| Proměnné | All-defs coverage, All-uses coverage, All du-paths coverage |
| Rozklad domény | Each choice coverage, Pair-wise coverage, T-wise coverage, Base choice coverage, All combinations coverage |

**Nepiš do přehledu kritéria, která v projektu nedávají smysl** — u čistě
výpočetní knihovny nemá cause-effect graphing co pokrývat a jen by
zaplevelilo nabídku. Naopak nevynechávej celou kategorii jen proto, že se
v ní hůř přemýšlí: každá najde jiné chyby.

## Kdy přehled obnovit

- Když o to uživatel požádá.
- Když jsi v projektu dokončil větší kus práce, který přidal nebo odebral
  celou oblast (nový modul, nová obrazovka, odstraněná služba).

Nepřepisuj ho po každé drobné změně — datum v hlavičce má uživateli říct,
jak čerstvý podklad je, a časté přepisy zbytečně plní historii repozitáře.
