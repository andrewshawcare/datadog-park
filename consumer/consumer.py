import time
import uuid

import ddtrace
from ddtrace import tracer
from ddtrace.context import Context
from kafka import KafkaConsumer
from kafka.consumer.fetcher import ConsumerRecord
from kafka.errors import NoBrokersAvailable
import structlog


def tracer_injection(logger, log_method, event_dict):
    current_span = tracer.current_span()
    trace_id, span_id = (current_span.trace_id, current_span.span_id) if current_span else (None, None)

    event_dict['dd.trace_id'] = str(trace_id or 0)
    event_dict['dd.span_id'] = str(span_id or 0)
    event_dict['dd.env'] = ddtrace.config.env or ""
    event_dict['dd.service'] = ddtrace.config.service or ""
    event_dict['dd.version'] = ddtrace.config.version or ""

    return event_dict


structlog.configure(
    processors=[
        tracer_injection,
        structlog.processors.JSONRenderer()
    ]
)
logger = structlog.get_logger()


def extract_context_from_kafka_consumer_record(consumer_record: ConsumerRecord):
    headers = dict(consumer_record.headers)
    return Context(
        trace_id=int(headers['x-datadog-trace-id']),
        span_id=int(headers['x-datadog-parent-id']),
        sampling_priority=float(headers['x-datadog-sampling-priority'])
    )


@tracer.wrap()
def process_consumer_record(consumer_record: ConsumerRecord):
    logger.info({
        'id': uuid.uuid4(),
        'type': 'DocumentProcessed',
        'meta': {
            'team': {
                'id': 'com.andrewshawcare.datadog-park.consumer',
            },
            'system': {
                'id': 'com.andrewshawcare.datadog-park.consumer.consumer'
            },
            'classification': {
                'name': 'HIGHLY SENSITIVE'
            },
            'document': {
                'format': '',
                'standard': '',
                'release': '',
                'type': ''
            }
        }
    })


sleep_duration = 1
while True:
    try:
        kafkaConsumer = KafkaConsumer('Event', bootstrap_servers=['kafka:9092'])
        while True:
            for consumer_record in kafkaConsumer:
                context = extract_context_from_kafka_consumer_record(consumer_record)
                tracer.context_provider.activate(context)
                process_consumer_record(consumer_record)
    except NoBrokersAvailable as noBrokersAvailable:
        logger.info(f'No brokers available, sleeping for {sleep_duration}s')
        time.sleep(sleep_duration)
