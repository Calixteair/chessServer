# Serveur d'échecs : TCP 8090 (client JavaFX) + WebSocket 8091 (client web).
# Contexte de build : dossier parent contenant chessGame/ (moteur) et chessServer/.
#   docker build -f chessServer/Dockerfile -t chess-server .

FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /src
# Moteur de règles (tests compris : le build échoue si une règle casse)
COPY chessGame/pom.xml chessGame/pom.xml
COPY chessGame/src chessGame/src
RUN mvn -q -B -f chessGame/pom.xml install
COPY chessServer/pom.xml chessServer/pom.xml
COPY chessServer/src chessServer/src
RUN mvn -q -B -f chessServer/pom.xml package

FROM eclipse-temurin:17.0.20_8-jre-alpine
RUN adduser -D -H -u 10001 chess
COPY --from=build /src/chessServer/target/chess-server-1.0-SNAPSHOT.jar /app/chess-server.jar
USER 10001
WORKDIR /app
ENV CHESS_LOG_FILE=- \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8090 8091
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
  CMD nc -z 127.0.0.1 8090 && nc -z 127.0.0.1 8091 || exit 1
ENTRYPOINT ["java", "-jar", "/app/chess-server.jar"]
