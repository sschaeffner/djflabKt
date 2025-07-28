# syntax=docker/dockerfile:1

FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY app/build/libs/app-*-all.jar ./app.jar
COPY frontend ./frontend
CMD ["app.jar"]
