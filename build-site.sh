#!/bin/bash

git push origin --delete gh-pages
rm -fr resources/public/.git

lein clean
lein cljsbuild once min

cd resources/public
git init
git add .
git commit -m "Deploy to GitHub Pages"
git push https://github.com/ijmbarr/trumpissimus.git master:gh-pages
