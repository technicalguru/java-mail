# Java Mail Queue

This project provides a mail queue that is agnostic to the actual mail sending framework. The mail
queue provides these features:

- Queue capacity is limited (by default it is 1000)
- Mails are processed in order of queuing
- Mail sending can be throttled by a token bucket algorith (see Throttling)
- Mails can be sent with priority (preceding mails in queue with normal priority)
- Thread-safe implementation for mail queueing and sending.

# Installation

Maven Coordinates:

```
	<dependency>
		<groupId>eu.ralph-schuster</groupId>
		<artifactId>mail-queue</artifactId>
		<version>1.0.0</version>
	</dependency>
```

# Documentation

Javadoc is available from: [javadoc.io](https://www.javadoc.io/doc/eu.ralph-schuster/mail-queue)

# Getting Started

The projects provides two sending implementations: 

- traditional [JavaMail](https://javaee.github.io/javamail/) framework (`javax.mail.Mail`)
- flexible [Simple Java Mail](https://www.simplejavamail.org/) framework

You can setup your mail queue quite easily:

```
// The traditional JavaMail way
MailQueue<Mail> queue1 = new MailQueue<>(new MessageMailSender());

// The SimpleJavaMail way
MailQueue<Email> queue2 = new MailQueue<>(new SimpleJavaMailSender());

```

Now you can start to queue mail objects. You pass the message and the reference ID
which will being used to notify you about status changes of this message.

```
boolean success = queue.queue(message, referenceId);
```

The method returns whether the message was queued successfully.

## Queue Capacity and Size

As the queue has a certain capacity, the `queue()` method will block when the
capacity has been reached and return only when another message was sent meanwhile.

You can avoid the blocking by checking the remaining capacity of the queue in advance:

```
int remainingCapacity = remainingCapacity(false);
```

or even simpler:

```
if (queue.hasCapacity(false)) {
	boolean success = queue.queue(message, referenceId);
}
```

The boolean parameter defines whether you want to check the priority queue (`true`) or not.

The queue can give you a status of its current size (not capacity!):

```
int allMessages      = queue.size();
int normalMessages   = queue.size(false);
int priorityMessages = queue.size(true);
```

Please notice that you can control the capacity only when creating the queue. Once set
it cannot be modified anymore.

## Throttling

The sending process can be throttled by using a token bucket. The `MailQueue` can be
configured with an implementation of `Bucket` from the [Token Bucket](https://github.com/cowwoc/token-bucket)
project:

```
Bucket bucket = ...
queue.setTokenBucket(bucket);
```

Refer to the [Token Bucket](https://github.com/cowwoc/token-bucket) documentation for details about
how to create such a bucket.

## Sending the Messages

The `MailQueue` implementation makes no assumption about how you want to organize the
sending process. The `run()` method will process the queue until it is empty or
no token is available from the token bucket and return. It is up to the caller to re-call
the method again to start processing the queue again.

That said it is made clear that `run()` will never block when there is nothing to do at
the given moment. It will simply return.

You can start a separate thread in your application and run the method periodically. Here is
an example of how to do it in Spring Boot:

```
	@Scheduled(fixedDelay = 10000)
	protected void processQueue() {
		MailQueue<Email> queue = getMailQueue();
		if (queue != null) {
			if (log.isDebugEnabled()) log.debug("Queue processing started");
			try {
				queue.run();
			} catch (Throwable t) {
				log.error("Queue processing failed.", t);
			}
			// Queue more emails from persistent store
			checkForNonQueuedMessages();
			if (log.isDebugEnabled()) log.debug("Queue processing finished");
		}
	}
```


## Failed Message Sending

Messages that cannot be sent due to some failure will be queued for a limited number of retries
before giving up. You can fine-tune this process:

```
queue.setMaxRetries(5);
queue.setRetryPeriod(3600000);
```

Default behaviour is to retry every 60 seconds and 3 times.

## Listening to the MailQueue

You can listen to any status change of messages by registering a `MailQueueListener`:

```
queue.addListener(new MailQueueAdapter() {
	public void onSent(String referenceId) {
		// Do something
	}
);
```

The reference ID is the ID of the message that was affected.

`MailQueue` knows 4 states of a message:

- QUEUED - the message was accepted to the queue
- SENDING - the message was picked for sending and is currently being sent
- SENT - the message has been sent successfully
- FAILED - The message could not be sent.

## Using your own Mailing Framework

If you want to use your own mailing framework, you can write an implementation of the `MailSender`
interface and pass an instance of it to the `MailQueue` constructor.
