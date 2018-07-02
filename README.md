Scalambda
=========
The goals of this project are two-fold:

- Create a sensible pure functional API on top of AWS Lambda
- Generate API-stubbs from Swagger 2.0

Swagger Generation
------------------
This is currently WIP, but you can already try out generating an API via the
`petstore.json` file included in the repo.

To do this:

```bash
$ sbt
> lambda-swaggy/run ./modules/swaggy/test/resources/petstore.json ./modules/generated/src petstore
> lambda-generated/compile
```

Status
======
- [x] Pure functional API for AWS Lambda
- [x] Generate API-stubbs from Swagger
- [ ] Integrate with SBT by way of plugin
- [ ] Generate comments
- [ ] Preserve existing code on regen
- [ ] Preserve existing comments on regen
- [ ] Support AWS extensions and generate deployment infra
