#!/bin/sh
base=ComicScanner
infile=template.html
outdir=stage
outfile=$base.html
applet=$base.jar
#keys=comicscanner.keys
jarlist=$applet
mkdir -p $outdir
rm -rf $outdir/*
#keytool -genkey -keystore $keys -alias me -storepass $1
#keytool -selfcert -keystore $keys -alias me -storepass $1
#jarsigner -keystore $keys $applet me -storepass $1
mv ./*.jar $outdir
for jar in $(grep "<classpathentry kind=\"lib\" path=\".*\.jar\"/>" ../.classpath | cut -f4 -d'"')
do
  cp -u "$jar" $outdir
  base=$(basename "$jar")
  jarlist=$jarlist,$base
done
sed "s/===/$jarlist/g" $infile > $outdir/$outfile
