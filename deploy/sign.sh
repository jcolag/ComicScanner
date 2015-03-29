#!/bin/sh
keytool -genkey -keystore comicscanner.keys -alias me
keytool -selfcert -keystore comicscanner.keys -alias me
jarsigner -keystore comicscanner.keys ComicScanner.jar me
