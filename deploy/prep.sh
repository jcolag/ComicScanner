#!/bin/sh
infile=template.html
outdir=stage
outfile=ComicScanner.html
applet=ComicScanner.jar
jarlist=$applet
mkdir -p $outdir
rm -rf $outdir/*
sh ./sign.sh $applet
mv *.jar $outdir
for jar in $(grep "<classpathentry kind=\"lib\" path=\".*\.jar\"/>" ../.classpath | cut -f4 -d'"')
do
  cp -u $jar $outdir
  base=$(basename $jar)
  jarlist=$jarlist,$base
done
sed "s/===/$jarlist/g" $infile > $outdir/$outfile
