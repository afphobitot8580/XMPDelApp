# Jak postavit APK bez počítače - jen z telefonu

Potřebuješ jen telefon a prohlížeč. Žádný počítač, žádný Android Studio.

## 1. Jdi na github.com
- Přihlas se / udělej si účet zdarma
- Vpravo nahoře + > New repository
- Název např. `XMPDelApp`
- Zaškrtni Public, dej Create repository

## 2. Nahraj soubory z tohoto ZIPu
- V repozitáři klikni na `Add file > Upload files`
- Na telefonu vyber: rozbalený ZIP - nahraj VŠECHNY soubory a složky tak, jak jsou
- Důležité: musí tam být i skrytá složka `.github/workflows/build.yml`
- Dej `Commit changes`

## 3. Spusť build
- Jdi na záložku `Actions`
- Automaticky se spustí workflow `Build APK`
- Počkej 2-4 minuty, než bude zelená fajfka

## 4. Stáhni APK
- Klikni na ten běh workflow
- Dole v sekci `Artifacts` klikni na `XMPDelApp-debug-apk`
- Stáhne se ZIP s APK uvnitř - rozbal ho
- Nainstaluj `app-debug.apk` na telefon (povol Instalovat z neznámých zdrojů)

## 5. Použití aplikace
- Otevři XMPDelApp
- Vybrat fotku > aplikace smaže XMP segment (APP1 s http://ns.adobe.com/xap/1.0/)
- Uloží vyčištěnou kopii do Stažené / XMPDelApp /

## Poznámka k stahování v Meta AI
- NEKLIKEJ na náhled s modrým tlačítkem Download - ten na Vivo uloží jen `.zip.html`
- Klikej PŘÍMO na textový odkaz na soubor - ten stáhne pravý .zip
- Stejný trik jako u 4K fotek - přímý odkaz, ne tlačítko v náhledu
