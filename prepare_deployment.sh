#!/bin/bash

rm target/universal/*.tgz
rm dist/*.tgz

./activator universal:package-zip-tarball

cp target/universal/*.tgz dist/
git add dist/*.tgz
