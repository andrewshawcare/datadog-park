# datadog-park

A wide-open grassy field where Datadog can run around.

This was an initial attempt to see how Datadog would interact with Kafka, Python, and Java. There's a lot to do, but it's a good start.

## Getting started

### .env file

You need an `.env` file with your Datadog API key populated. There's an `.env.template` file you can copy like so:

```shell
cp .env.template .env
```

Then just add your Datadog API key in the `DD_API_KEY` environment variable.

Once that's complete, run the applications with:

```shell
docker-compose up
```
