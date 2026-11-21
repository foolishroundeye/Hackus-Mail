---
name: coden-test-docs
description: Zapisuje dokumentaci testů do tests/docs/<úroveň>/<kritérium>/ v tvaru, který umí přečíst mobilní aplikace coden. Použij vždy, když vytváříš nebo měníš testy, testovací případy nebo jejich popis.
---
<!-- coden-skill-verze: 3 -->

# Dokumentace testů

Aplikace coden čte dokumentaci testů z telefonu a prokliká z výsledku běhu
přímo na řádek, kde je testovaný případ popsán. Aby to fungovalo, musí být
soubory přesně tam, kde je čeká, a případy označené identifikátorem.

## Kořen projektu

Všechny cesty v tomhle skillu (`tests/docs/…`) jsou od **kořene projektu** — složky,
nad kterou běží Server. Její absolutní cesta je v proměnné prostředí
`CODEN_PROJECT_DIR`; chybí-li, je to pracovní složka, ve které Konverzace
začala.

Nikdy ne od kořene gitu ani od podsložky, kam ses přesunul (třeba kvůli
spuštění testů). Leží-li kód v podsložce, třeba `projekt/`, soubory stejně
patří do `$CODEN_PROJECT_DIR/tests/docs/…`, ne do `projekt/tests/docs/…`. Jinde je
aplikace nehledá a zapsané by neviděla.

Zapisuj proto vždy absolutní cestou od `$CODEN_PROJECT_DIR`.

## Kam se zapisuje

```
tests/docs/<úroveň>/<kritérium>/<oblast>.md
```

`<úroveň>` je právě jedna z: `unit`, `integration`, `system`, `e2e`,
`performance-ui`, `performance-backend`. Jiná hodnota nesmí vzniknout —
aplikace jinou složku nenajde.

`<kritérium>` je **klíč kritéria pokrytí** z katalogu níže. Zadání ti složku
vždy jmenuje; drž se jí, ať dokumentace k jednomu kritériu zůstane
pohromadě. Nepatří-li to, co píšeš, pod žádné kritérium z katalogu, použij
`jine`.

`<oblast>` je krátký název části projektu v kebab-case, například
`prihlaseni-uzivatele.md`.

**Jedno kritérium klidně rozděl do víc souborů.** Edge coverage nad celým
projektem v jednom souboru se nedá číst; rozdělit ho po částech projektu je
správně a aplikace soubory jedné složky ukáže pohromadě pod jménem toho
kritéria.

### Klíče kritérií

| Kategorie | Klíče |
|:---|:---|
| Tok programu | `node-coverage`, `edge-coverage`, `prime-path-coverage` |
| Výkon | `baseline-test`, `load-test`, `stress-test`, `test-stability` |
| Logické podmínky | `predicate-coverage`, `clause-coverage`, `combinatorial-coverage`, `mcdc` |
| Požadavky | `cause-effect` |
| Proměnné | `all-defs`, `all-uses`, `all-du-paths` |
| Rozklad domény | `each-choice`, `pair-wise`, `t-wise`, `base-choice`, `all-combinations` |
| Jiné | `jine` |

### Dokumentace, která tu byla dřív

Soubory ležící přímo v `tests/docs/<úroveň>/` **nech být**. Aplikace je čte
dál a zaznamenané běhy odkazují na identifikátory v nich; přesouvat je není
tvoje práce. Nové případy piš do složek podle kritérií.

## Identifikátor případu

Každý testovací případ má identifikátor `<PREFIX>-<číslo>`:

| Úroveň | Prefix |
|:---|:---|
| `unit` | `UNIT` |
| `integration` | `INT` |
| `system` | `SYS` |
| `e2e` | `E2E` |
| `performance-ui` | `PERFUI` |
| `performance-backend` | `PERFBE` |

Číslo je trojmístné, doplněné nulami (`UNIT-014`), a v rámci úrovně
jedinečné.

**Identifikátory se nikdy nepřečíslovávají a nikdy nepoužívají podruhé.**
Odkazují na ně už zaznamenané běhy testů; přečíslováním by začaly ukazovat
na cizí případ. Nový případ dostane číslo o jedna vyšší, než je nejvyšší
dosud použité v té úrovni — i když mezitím nějaká čísla vypadla. Zrušený
případ se v dokumentaci označí jako zrušený, nesmaže se beze stopy.

## Tvar souboru

Soubor `tests/docs/unit/edge-coverage/prihlaseni-uzivatele.md`:

````markdown
# Přihlášení uživatele

Úroveň: unit

Kritérium: Edge coverage

### UNIT-014 — Odmítne prázdné heslo

**Ověřuje:** že přihlášení s prázdným heslem skončí chybou a nevytvoří relaci.

**Předpoklady:** existující uživatel `test@example.com`.

**Očekávaný výsledek:** návratový kód `INVALID_CREDENTIALS`, žádná relace
v úložišti.

### UNIT-015 — Přijme správné heslo

...
````

Pravidla, na kterých závisí čtení v aplikaci:

- Nadpis případu je vždy `### <IDENTIFIKÁTOR> — <název>` na jednom řádku.
- Identifikátor se v souboru vyskytuje **právě jednou** jako nadpis. Chceš-li
  na případ odkázat jinde, napiš odkaz, ne další nadpis — aplikace jinak
  proklik nenabídne, protože neví, který řádek je ten pravý.
- Sekce `**Ověřuje:**`, `**Předpoklady:**` a `**Očekávaný výsledek:**` jsou
  povinné. Popisují záměr, ne implementaci: uživatel je čte na telefonu ve
  chvíli, kdy mu test spadl a on nemá u sebe kód.

## U výkonnostních úrovní

U `performance-ui` a `performance-backend` **nepiš prahy ani limity**.
Výkon se měří, nehodnotí — případ selže jen tehdy, když selže samotný test,
nikoliv když je naměřená hodnota vysoká. Do očekávaného výsledku napiš, co
se měří a za jakých podmínek, ne jaká hodnota je ještě dobrá.

## Když měníš existující testy

1. Uprav dokumentaci i testovací kód, aby si odpovídaly.
2. Zachovej identifikátory těch případů, které zůstávají.
3. Nové případy přidej na konec s dalšími čísly.

Související skilly: `coden-test-results` (zápis běhů), `coden-criteria`
(nabídka kritérií a jejich náročnost).
