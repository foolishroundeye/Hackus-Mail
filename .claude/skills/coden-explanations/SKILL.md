---
name: coden-explanations
description: Vysvětluje architekturu, návrh a kód uživateli mobilní aplikace coden. Použij, když máš sestavit mapu vysvětlitelných položek nebo něco vysvětlit — odpověď piš rovnou do konverzace, srozumitelně a bez výpisů kódu.
---
<!-- coden-skill-verze: 7 -->

# Vysvětlování

Čte to **člověk na telefonu**, ne vývojář u editoru. Nemá po ruce soubory,
nevidí kód a nechce ho vidět — ptá se právě proto, aby ho číst nemusel.

## Kořen projektu

Všechny cesty v tomhle skillu (`docs/explanations/…`) jsou od **kořene projektu** — složky,
nad kterou běží Server. Její absolutní cesta je v proměnné prostředí
`CODEN_PROJECT_DIR`; chybí-li, je to pracovní složka, ve které Konverzace
začala.

Nikdy ne od kořene gitu ani od podsložky, kam ses přesunul (třeba kvůli
spuštění testů). Leží-li kód v podsložce, třeba `projekt/`, soubory stejně
patří do `$CODEN_PROJECT_DIR/docs/explanations/…`, ne do `projekt/docs/explanations/…`. Jinde je
aplikace nehledá a zapsané by neviděla.

Zapisuj proto vždy absolutní cestou od `$CODEN_PROJECT_DIR`.

## Pořadí práce

1. **Přečti si, co potřebuješ** — mlčky. Čtení nekomentuj a nevypisuj.
2. **Odpověz v konverzaci.** Celou odpovědí, ne odkazem na soubor.
3. **Teprve pak ulož** — jedním zápisem, na konci. Ne po částech, ne
   průběžně: každý zápis je pro uživatele další blok práce navíc a on chce
   číst výklad, ne sledovat, jak vzniká soubor.

## Tři pravidla, která platí vždycky

1. **Vysvětlení piš do konverzace.** Je to odpověď na otázku, ne dokument
   k pozdějšímu přečtení. Uživatel na ni kouká hned, jak ji píšeš.
2. **Nevypisuj obsah souborů.** Ani celé, ani po kusech, ani „pro ilustraci".
   Co jsi v souboru našel, řekni vlastními slovy. Nanejvýš zmiň **jméno**
   funkce nebo souboru, když se o něm bavíme — bez těla, bez úryvku.
3. **Do vysvětlovaného souboru nikdy nezapisuj.** Vysvětlování je čtení.
   Když si při něm všimneš chyby, popiš ji a nech na uživateli, jestli ji má
   někdo opravit; oprava je jiná práce a patří do jiného zadání.

## Jak psát

- **Nejdřív k čemu to je**, pak jak to funguje. Kdo se ptá „co je přenosový
  protokol", potřebuje vědět, jaký problém řeší, ne jak se jmenuje struktura.
- **Bez žargonu, nebo se žargonem vysvětleným.** Padne-li „rámcování",
  musí být větu předtím řečeno, co to znamená.
- **Krátké odstavce**, tři až pět vět. Dlouhý blok se na telefonu nedá číst.
- **Vysvětluj rozhodnutí, ne jen stav.** Nejcennější je, co bylo alternativou
  a proč se nevybrala — to z kódu nikdo nevyčte.
- **Přirovnání pomáhají**, dokud nelžou. „Je to jako pošta, která zaručí
  pořadí dopisů" je lepší než odstavec o sekvenčních číslech.
- **Bez odrážkových výčtů všeho.** Souvislý text, který se dá přečíst nahlas.

## Diagramy

**Kresli je, kdykoliv to dává aspoň trochu smysl.** Obrázek se pochopí na
jedno podívání, kdežto tentýž popis slovy se musí přečíst dvakrát a stejně
si ho čtenář v hlavě překreslí do obrázku sám. Aplikace vykresluje **mermaid**
přímo v odpovědi, takže diagram v konverzaci není příloha, ale plnohodnotná
část výkladu.

Ke každému tématu, které má nějaký tvar, patří diagram — a tvar má skoro
všechno:

| Co vysvětluješ | Co nakreslit |
|:---|:---|
| Z čeho se to skládá, co s čím mluví | `flowchart LR` s částmi a šipkami |
| Co se stane po sobě, kdo komu co pošle | `sequenceDiagram` |
| V jakých stavech to může být a čím se mění | `stateDiagram-v2` |
| Jak vypadají data a co na co odkazuje | `erDiagram` nebo `classDiagram` |
| Jak dlouho co trvá, co běží souběžně | `gantt` nebo `sequenceDiagram` |

Jak je psát, aby k něčemu byly:

- **Jeden diagram, jedna myšlenka.** Radši dva menší než jeden, ve kterém se
  uživatel ztratí. Nad osm uzlů to na telefonu přestává být čitelné.
- **Popisky česky a lidsky** — „telefon", „Server", „soubor na disku", ne
  názvy tříd.
- **Bez HTML v popiscích.** Žádné `<br/>` ani jiné značky: aplikace kreslí
  diagramy v přísném režimu, kde se popisky berou jako text, a na ostré
  závorce parser spadne. Potřebuješ-li kratší popisek, napiš kratší popisek.
- **Diagram doprovoď větou**, co si z něj má čtenář odnést. Obrázek ukáže
  tvar, věta řekne pointu.
- Diagram **nenahrazuje** vysvětlení proč; nahrazuje popis toho, co kam vede.

## Mapa vysvětlitelných položek

```
docs/explanations/map.md
```

Mapa je **nabídka otázek**, ne obsah projektu. Uživatel z ní vybírá, aniž by
projekt znal, takže popis u každé položky musí říct, **co ta věc je** — ne
jen kde leží.

Dělí se na dvě části a na pořadí záleží: **nejdřív obecné, pak konkrétní.**

````markdown
# Co lze vysvětlit

Aktualizováno: 2026-09-06

## Celkový pohled

- **Architektura** — z jakých částí se aplikace skládá, kde běží a jak spolu mluví.
- **Návrh a rozhodnutí** — proč je to postavené takhle a co bylo alternativou.
- **Datový tok** — co se stane od chvíle, kdy uživatel klepne, po zápis dat.
- **Bezpečnost** — kdo se k čemu dostane a čím je to hlídané.
- **Jak se to spouští** — co je potřeba, aby projekt běžel u někoho jiného.

## Konkrétní části

- **Přenosový protokol** — vlastní protokol nad UDP, který zaručí pořadí a doručení. `jadro/src/ramec.rs`
- **Dokumentové zrcadlo** — jak se soubory z počítače dostanou do telefonu. `jadro/src/zrcadlo.rs`
````

Pravidla, na kterých závisí čtení v aplikaci:

- Nadpisy `## …` dělí mapu na skupiny; aplikace je ukazuje v tomhle pořadí.
  **První skupina je vždycky ta obecná.**
- Položka je řádek `- **název** — popis` a volitelně cesta v `` ` `` na konci.
- **Popis je celá věta o tom, co ta věc je**, ne opis názvu. „Zrcadlo —
  zrcadlení souborů" uživateli neřekne nic; „jak se soubory z počítače
  dostanou do telefonu" ano.
- Obecných položek dej pět až sedm, konkrétních nanejvýš deset. Mapa je
  rozcestník, ne rejstřík.
- Konkrétní položky vybírej podle toho, **co je na projektu nezvyklé** —
  vlastní protokol, vlastní formát, nečekané rozhodnutí. Běžné vrstvy, které
  vypadají v každém projektu stejně, do mapy nepatří.

## Uložení vysvětlení

Po odpovědi ji ulož i do souboru, aby projektu rostla dokumentace:

```
docs/explanations/<téma>.md
```

`<téma>` je název položky v kebab-case. **Jedno téma, jeden soubor**;
navazující dovysvětlení se doplňují do téhož souboru, nezakládá se nový.

### Několik položek najednou

Uživatel si v aplikaci může zaškrtnout víc položek a přijde ti pak jeden
požadavek se seznamem. **Vysvětli každou z nich** — každou pod vlastním
nadpisem, jednu po druhé. Žádná se nevynechává a neslučuje se do jednoho
povídání o tom, co mají společného; uživatel si je vybral proto, že chce
rozumět každé zvlášť.

Téma je v tom případě celý ten výběr, takže **soubor je pořád jeden**: jméno
má podle zadání a uvnitř má oddíl za každou položku. Sekce
„Lze dovysvětlit“ je na konci jedna za všechny.

````markdown
# Přenosový protokol

Aktualizováno: 2026-09-06
Týká se: jadro/src/ramec.rs, jadro/src/proud.rs

<vlastní text vysvětlení, týž jako v konverzaci>

## Lze dovysvětlit

- **Rámcování** — jak se zpráva rozdělí na kusy a proč zrovna takhle.
- **Řízení toku** — co se stane, když druhá strana nestíhá.
````

Sekce `## Lze dovysvětlit` je povinná a aplikace z ní staví tlačítka pro
další otázky. Piš do ní **věci, které jsi v odpovědi jen naznačil** — ne
obecná témata. Nemáš-li co nabídnout, dej tam jednu odrážku s nejbližší
nedořečenou věcí; prázdná sekce je slepá ulička.

Soubor je **kopie odpovědi, ne náhrada za ni**. Odpověď musí být v konverzaci
celá — uživatel do souborů chodit nemusí.
