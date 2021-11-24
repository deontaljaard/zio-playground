# zio-playground
Just a playground to try out some frameworks that I find cool.

## Setup
### Database setup
#### Get PostgreSQL Docker image
[PostgreSQL Docker](https://hub.docker.com/_/postgres/)
```bash
docker run --name pg \
  -e POSTGRES_PASSWORD=4us2know \
  -e POSTGRES_DB=playground \
  -p 5432:5432 \
  -d postgres:13
```

## Run
### SBT
```bash
sbt r
```

### Docker
```bash
sbt docker:publishLocal && \
  docker run --name zio-playground \
  -p 8080:8080 \
  -d \
  --net="host" \
  hub.docker.com/deontaljaard/zio-playground:0.1.0-SNAPSHOT
```
