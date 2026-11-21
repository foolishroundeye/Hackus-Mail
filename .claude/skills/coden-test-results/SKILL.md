---
name: coden-test-results
description: Zapisuje výsledky běhu testů jako JUnit XML do tests/results/<úroveň>/ v tvaru, který umí přečíst mobilní aplikace coden. Použij vždy, když spouštíš testy nebo zaznamenáváš jejich výsledek.
---
<!-- coden-skill-verze: 4 -->

# Výsledky běhů testů

Aplikace coden zobrazuje na telefonu historii běhů, porovnává je mezi sebou
a páruje je s commity. Čte k tomu soubory, které zapíšeš ty — ne výstup na
konzoli. Co tady není zapsané, uživatel na telefonu neuvidí.

## Kořen projektu

Všechny cesty v tomhle skillu (`tests/results/…`) jsou od **kořene projektu** — složky,
nad kterou běží Server. Její absolutní cesta je v proměnné prostředí
`CODEN_PROJECT_DIR`; chybí-li, je to pracovní složka, ve které Konverzace
začala.

Nikdy ne od kořene gitu ani od podsložky, kam ses přesunul (třeba kvůli
spuštění testů). Leží-li kód v podsložce, třeba `projekt/`, soubory stejně
patří do `$CODEN_PROJECT_DIR/tests/results/…`, ne do `projekt/tests/results/…`. Jinde je
aplikace nehledá a zapsané by neviděla.

Zapisuj proto vždy absolutní cestou od `$CODEN_PROJECT_DIR`.

## Kam se zapisuje

```
tests/results/<úroveň>/<ISO čas>-<krátký commit>.xml
```

Například `tests/results/unit/2026-09-06T14-31-02-a1b2c3d.xml`.
Čas v názvu je v UTC, dvojtečky nahrazené pomlčkami, aby šel název seřadit
a fungoval na všech systémech. `<úroveň>` je jedna z: `unit`, `integration`,
`system`, `e2e`, `performance-ui`, `performance-backend`.

**Nikdy nepřepisuj existující soubor běhu.** Historie stojí na tom, že každý
běh je vlastní soubor.

## Tvar souboru

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuites>
  <testsuite name="prihlaseni" tests="2" failures="1" errors="0" skipped="0" time="1.284">
    <properties>
      <property name="commit" value="a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0"/>
      <property name="uroven" value="unit"/>
    </properties>
    <testcase classname="UNIT-014.auth.prihlaseni" name="odmítne prázdné heslo" time="0.412"/>
    <testcase classname="UNIT-015.auth.prihlaseni" name="přijme správné heslo" time="0.872">
      <failure message="čekalo se 200, přišlo 401">
        stack trace nebo výpis nástroje
      </failure>
    </testcase>
  </testsuite>
</testsuites>
```

Závazná pravidla:

1. **Identifikátor případu je první segment atributu `classname`**, oddělený
   tečkou: `UNIT-014.cokoliv.dalsiho`. Podle něj aplikace najde popis případu
   v `tests/docs/`. Zbytek `classname` může být cokoliv, co dává smysl
   v použitém nástroji.
2. **Atribut `time` je skutečná doba případu z výstupu testovacího nástroje**,
   ne odhad. Neposkytuje-li ji nástroj, atribut vůbec nezapisuj — nula by
   lhala, že test trval nulový čas.
3. **Commit se zapisuje jako `<property name="commit" …>`** v části
   `<properties>` u sady. Hodnota je plný otisk commitu, na kterém běh
   proběhl (`git rev-parse HEAD`). Bez něj aplikace u commitu neukáže testy;
   nikdy se nedovozuje z času souboru.
4. **Žádné `<!DOCTYPE>` ani entity.** Soubor se čte na telefonu a externí
   entity se odmítají — soubor s nimi se zobrazí jako nečitelný.
5. Znaky `&`, `<` a `>` v textu chyby řádně escapuj.

## Výkonnostní ukazatele

U úrovní `performance-ui` a `performance-backend` zapiš naměřené hodnoty jako
vlastnosti **u konkrétního případu**:

```xml
<testcase classname="PERFUI-003.seznam" name="vykreslení seznamu 500 položek" time="2.140">
  <properties>
    <property name="metric.render-time-ms" value="184"/>
    <property name="metric.memory-mb" value="62.5"/>
  </properties>
</testcase>
```

Povolená jsou právě tato jména, jiná se v aplikaci nezobrazí:

| Jméno | Co znamená |
|:---|:---|
| `metric.response-time-ms` | doba odpovědi v milisekundách |
| `metric.render-time-ms` | doba vykreslení v milisekundách |
| `metric.memory-mb` | špičková spotřeba paměti v MB |
| `metric.throughput-ops` | operací za sekundu |
| `metric.cpu-percent` | špičkové vytížení procesoru v procentech |
| `metric.transferred-kb` | objem přenesených dat v KB |

Jednotka je součástí jména, takže hodnota je vždy **holé číslo** bez jednotky
a bez oddělovače tisíců. Desetinný oddělovač je tečka.

**Hodnota nikdy nerozhoduje o výsledku případu.** Případ označ za neúspěšný
jen tehdy, když test skutečně selhal, ne když je číslo vysoké.

### Průběh v jednom běhu

Zátěžový, stresový ani test stability nemá jednu hodnotu — jeho obsahem je
**křivka**: jak se systém chová, když parametr roste. Jedna hodnota za běh
z něj dělá baseline, který to není, a okamžik, kdy se to zlomí, se z průměru
přečíst nedá.

Naměřené hodnoty proto zapiš **po jednotlivých nastaveních** a za jméno dej
`@` a hodnotu parametru:

```xml
<testcase classname="PERFBE-021.zatez.objednavky" name="Držená zátěž na objednávkách" time="42.3">
  <properties>
    <property name="metric.parameter" value="souběžných uživatelů"/>
    <property name="metric.response-time-ms@10" value="11.4"/>
    <property name="metric.response-time-ms@50" value="24.8"/>
    <property name="metric.response-time-ms@100" value="98.2"/>
    <property name="metric.response-time-ms@200" value="512.7"/>
    <property name="metric.throughput-ops@10" value="880"/>
    <property name="metric.throughput-ops@50" value="2010"/>
    <property name="metric.throughput-ops@100" value="2080"/>
    <property name="metric.throughput-ops@200" value="1140"/>
    <property name="metric.response-time-ms" value="98.2"/>
  </properties>
</testcase>
```

Pravidla:

- `metric.parameter` je **jedna věta o tom, co se měnilo** — „souběžných
  uživatelů", „sekund běhu", „velikost dávky". Píše se jednou za případ a
  aplikace ji dá pod vodorovnou osu.
- Za `@` je **číslo**, ne popisek: podle něj se body rozmístí, takže skok
  z 50 na 100 vypadá jinak než z 10 na 50.
- Jméno před `@` je pořád z povolené sady výše.
- **Hodnota bez `@` zůstává souhrnem** za celý běh. Zapiš ji taky: v seznamu
  případů se ukazuje jako číslo a srovnává se napříč běhy. Dej tam tu, která
  případ nejlíp vystihuje — u zátěže hodnotu na nejvyšším stupni, ne průměr
  přes všechny.
- Jeden bod křivku nedělá; buď měř aspoň na dvou nastaveních, nebo zapiš jen
  souhrn.

U `baseline-test` křivka nedává smysl — měří se jedno nastavení a srovnává se
s dřívějšími běhy. Tam zapiš jen souhrnnou hodnotu.

## Artefakty u neúspěšných případů

Snímky obrazovky, záznamy a protokoly ukládej sem:

```
tests/results/<úroveň>/artifacts/<IDENTIFIKÁTOR>/<název souboru>
```

a odkaž se na ně z případu:

```xml
<property name="artifact" value="artifacts/E2E-007/selhani.png"/>
```

Hodnota je cesta relativní ke složce úrovně. Na telefonu se zobrazují
obrázky (`png`, `jpg`, `webp`); ostatní typy se jen vypíšou jménem.

Složka `artifacts` drží vždy jen **poslední** běh dané úrovně — před zápisem
nových artefaktů tu starou složku smaž. Kdyby se hromadily, telefon by je
stahoval všechny.

## Postup při spuštění testů

1. Zjisti commit: `git rev-parse HEAD`.
2. Spusť testy tak, aby ti nástroj dal časy jednotlivých případů (většina
   nástrojů to umí sama do JUnit XML — pak stačí výstup přesunout a doplnit
   identifikátory a vlastnost `commit`).
3. Zapiš soubor podle pravidel výše.
4. V odpovědi napiš jednou větou, co prošlo a co spadlo. Podrobnosti už
   uživatel uvidí v aplikaci.

### Běh není oprava

Když tě někdo požádá o **spuštění** testů, spustíš je a zapíšeš výsledek.
Nic víc:

- **Neopravuj kód ani testy.** Ani drobnost, ani „zjevnou" chybu.
- **Nedoplňuj chybějící případy** a žádný nepřeskakuj, aby běh vyšel líp.
- **Nezkoumej příčiny selhání.** Spadlý test je výsledek, ne úkol.
- Nepodaří-li se testy spustit vůbec, napiš jednou větou proč a dál nic
  nedělej — instalace závislostí ani úprava konfigurace do běhu nepatří.

Uživatel se dívá na historii běhů; ta má ukazovat, jak projekt dopadl, ne
jak dopadl poté, co jsi ho cestou spravil. O opravu si řekne zvlášť.

Související skilly: `coden-test-docs` (popisy případů).
