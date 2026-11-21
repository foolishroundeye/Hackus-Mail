---
name: coden-tasks
description: Úkoly projektu vedené mobilní aplikací coden — kde je jejich výpis, co v něm je a proč se do něj nezapisuje. Použij, když se ptají, co zbývá udělat, co bylo zadáno, nebo když máš pokračovat v rozdělané práci.
---
<!-- coden-skill-verze: 6 -->

# Úkoly projektu

Uživatel si v aplikaci coden vede **seznam úkolů**: co zadal, co se má udělat
a co už odkýval jako hotové. Vzniká z jeho promptů a žije mimo tvůj dosah.

## Kořen projektu

Všechny cesty v tomhle skillu (`.coden/…`) jsou od **kořene projektu** — složky,
nad kterou běží Server. Její absolutní cesta je v proměnné prostředí
`CODEN_PROJECT_DIR`; chybí-li, je to pracovní složka, ve které Konverzace
začala.

Nikdy ne od kořene gitu ani od podsložky, kam ses přesunul (třeba kvůli
spuštění testů). Leží-li kód v podsložce, třeba `projekt/`, soubory stejně
patří do `$CODEN_PROJECT_DIR/.coden/TASKS.md`, ne do `projekt/.coden/TASKS.md`. Jinde je
aplikace nehledá a zapsané by neviděla.

Zapisuj proto vždy absolutní cestou od `$CODEN_PROJECT_DIR`.

## Kde to je

```
.coden/TASKS.md
```

Čitelný výpis všech úkolů, seskupený po stavech. **Čti ho**, když potřebuješ
vědět, co je rozdělané — je to levnější a přesnější než se ptát.

U každého úkolu jsou ve výpisu i **obrázky** a **komentáře** i s tím, kdo je
napsal. Komentář od uživatele je to, co ti o úkolu dodatečně řekl — typicky co je
na hotové práci špatně.

**Obrázek je obyčejný soubor v projektu** (`.coden/images/…`). Když ho úkol nebo
komentář zmiňuje, **otevři si ho** — bývá to snímek obrazovky s tím, co je špatně,
takže je v něm polovina zadání. Pracovat podle textu a obrázek minout znamená
udělat práci podle poloviny.

Dostaneš-li zadání s úkoly, je v něm u každého **číslo v hranatých závorkách**
(`- [12] oprav zarovnání`). Podle něj si dohledáš zbytek ve výpisu — stav,
historii, celé vlákno komentářů — a podle něj taky odpovídáš.

Vedle něj leží `.coden/tasks.db`. To je databáze, ze které se výpis pořizuje.
Nemusíš ji otevírat; všechno, co je v ní zajímavého, je ve výpisu.

## Do čeho nesahat

**Ani jeden z těch dvou souborů neupravuj.** Není to jen etiketa:

- `TASKS.md` se při každé změně úkolů **přepisuje celý**. Co do něj napíšeš,
  zmizí při nejbližším přesunu úkolu a nikdo si toho nevšimne.
- `tasks.db` zapisuje Server. Souběžný zápis odjinud je nejkratší cesta
  k poškozené databázi. Na komentáře máš příkaz níž — ten ví, jak na to.

## Jak odpovědět na komentář

Komentáře jsou **jediné místo, kam u úkolu smíš psát** — a je to k tomu určený
příkaz, ne zápis do souboru:

```
coden-server comment <číslo úkolu> "text odpovědi"
```

Číslo úkolu je to v hranatých závorkách ve výpisu. Příkaz se spouští
**v adresáři projektu** (tam, kde pracuješ) a zapíše komentář tak, aby se
uživateli ukázal v aplikaci u toho úkolu.

Používej to, když se uživatel na něco v komentáři ptá, nebo když jeho výtku
vyřešíš: „opraveno, bylo to v gridu" u úkolu řekne víc než tatáž věta
v konverzaci, protože zůstane u věci, které se týká.

**Komentář nemění stav úkolu** a nemá se ho snažit měnit. Je to řeč o práci,
ne rozhodnutí o ní.

## Stavy a kdo je posouvá

| Stav | Co znamená |
|:---|:---|
| **Backlog** | někdy se to bude řešit |
| **TODO** | k řešení, klidně hned |
| **K ověření** | někdo na tom byl, uživatel to ještě neprošel |
| **Vráceno** | uživatel to odmítl — co bylo špatně, stojí v komentářích |
| **Hotovo** | uživatel to odkýval |

**Stavy posouvá uživatel, ne ty.** Je to záměr, ne opomenutí: seznam má být
tvrzením člověka o tom, jak práce stojí, ne dohadem nástroje. Když dokončíš
práci, napiš to do konverzace jako vždycky — uživatel si úkol přesune sám.

Jediná výjimka patří aplikaci, ne tobě: úkoly, které ti uživatel poslal
tlačítkem „pusť se do nich", si aplikace po skončení tahu sama označí jako
**K ověření**.

## Štítek

U úkolu může stát štítek v hranatých závorkách hned za číslem:

```
- [32] [bug] seznam se při rolování zasekává
```

Šest hodnot a **nejvýš jedna**: `bug`, `feature`, `task`, `test`, `performance`,
`analysis`. Dva štítky na jednom úkolu by znamenaly, že se měl rozdělit na dva.

Štítek ti **říká, jakou práci čekat** — u chyby se hledá příčina, u funkce se
staví, u rozboru se nemá sahat na kód, dokud není jasno.

Přiděluje ho uživatel v aplikaci, **ty do něj nesahej**. Je to totéž pravidlo
jako u stavů: seznam má být jeho tvrzením o práci, ne dohadem nástroje.

## K čemu je to dobré tobě

Když se tě uživatel zeptá „co zbývá?" nebo ti řekne „pokračuj", máš v tom
souboru odpověď, aniž bys musel listovat konverzací. A když zjistíš, že
rozdělaná práce odpovídá některému úkolu, **pojmenuj ho ve své odpovědi** —
uživatel pak ví, který si má přesunout.
