#!/usr/bin/env bash
set -e
jar="$1"
filename="${jar##*/}"
arch="${filename%.*}"
echo "Packaging $arch"
mkdir tmppkg
cp "$jar" tmppkg/ -v
cp -r models/ tmppkg/
cp -r views/ tmppkg/
cd tmppkg
rm -f "../${arch}.zip"
zip -r "../${arch}.zip" *
cd ..
echo "Cleaning up..."
rm -r tmppkg
echo "Done"