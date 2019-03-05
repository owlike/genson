#!/usr/bin/env bash
docker pull pwbgl/docker-jekyll-pygments
docker run --rm --label=jekyll --volume=$(pwd):/srv/jekyll -it -p 127.0.0.1:4000:4000 pwbgl/docker-jekyll-pygments
