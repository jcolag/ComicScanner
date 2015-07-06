#!/bin/sh
infile=template.html
outfile=ComicScanner.html
jarlist=
for jar in $(grep "<classpathentry kind=\"lib\" path=\".*\.jar\"/>" ../.classpath | cut -f4 -d'"')
do
  cp -u $jar .
  base=$(basename $jar)
  jarlist=$jarlist,$base
done
jarlist=$(echo $jarlist | cut -c2-)
sed "s/===/$jarlist/g" $infile > $outfile
