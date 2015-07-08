#!/bin/sh
keytool -genkey -keystore comicscanner.keys -alias me
keytool -selfcert -keystore comicscanner.keys -alias me
jarsigner -keystore comicscanner.keys $1 me
