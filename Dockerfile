FROM eclipse-temurin:17 as build

RUN apt update && apt-get install -y maven
RUN mkdir /app
COPY . /app
WORKDIR /app
RUN mvn initialize && mvn install

FROM eclipse-temurin:17
COPY --from=build /app/target /app/target
COPY --from=build /app/resources /app/target/resources
COPY docker-entrypoint.sh /usr/local/bin
ENTRYPOINT ["docker-entrypoint.sh"]