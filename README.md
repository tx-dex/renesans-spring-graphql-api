# Renesans Spring GraphQL Backend

Spring implementation of the weCan GraphQL Backend

## Requirements

* java
* docker

```bash
brew install java
brew cask install docker
```

## Development setup

First, set up development environment based on 
[development](https://thp.sangre.fi/renesans/development).

Then, clone this repository and run...

```bash
./gradlew -p core bootRun
```
...or run it in IDEA.

The database will be automatically populated with minimal test data on the first run.

The GraphQL server will be accessible at [localhost:8080/survey/graphql](http://localhost:8080/survey/graphql)

## Production build

Production and staging builds are generated automatically by GitLab CI based on 
GIT tags.
