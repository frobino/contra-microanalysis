# You can use a specific version too, like python:3.6.5-alpine3.7
# FROM python:3-alpine
# apt install postgresql-libs && \
# apt install gcc musl-dev postgresql-dev && \
FROM python:3.10.4-slim-bullseye

WORKDIR /usr/src/app
COPY . .

RUN \
 apt-get update && \
 apt-get -y install gcc default-libmysqlclient-dev pkg-config libpq-dev && \
 apt-get clean && \
 python3 -m pip install -r requirements.txt --no-cache-dir

COPY docker-entrypoint.sh /usr/local/bin
ENTRYPOINT ["docker-entrypoint.sh"]