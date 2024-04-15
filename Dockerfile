# syntax=docker/dockerfile:1

FROM gcr.io/distroless/java17-debian11
WORKDIR /app
COPY app/build/libs/app-*-all.jar ./app.jar
COPY frontend ./frontend
CMD ["app.jar"]
