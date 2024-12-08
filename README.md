# Quina Musical

Aquest programa permet crear la teva pròpia quina musical amb les cançons de la teva elecció.

## Instal·lació
**Requisits**

L'aplicació necessita <a href="https://www.oracle.com/es/java/technologies/downloads/">java</a> versió +17 per funcionar.

**Instal·lar**

Baixa't l'instal·lador de la pàgina de <a href="#">Releases</a>.<br>
Executa'l i segueix els passos d'instal·lació.

## Utilització
**Preparació d'una quina musical**

Mou-te a la carpeta arrel del programa (per defecte és `C:\ProgramFiles (x86)\Quina Musical`).

Dins hi ha una carpeta `music`. Dins aquesta carpeta s'han de posar totes les pistes d'àudio de la quina.

Un cop fet s'ha d'editar el fitxer `music/info.txt`. Aquest fitxer guarda totes les cançons de la quina i a quin temps ha de començar la cançó.

El format del fitxer és el següent:
```
nomFitxer;00:00
```
Per exemple:
```
Michael Jackson - Billie Jean.mp3;1:26
```
> [!Note]
> El programa avisa quan el format del fitxer no és vàlid o no s'han trobat les cançons.
