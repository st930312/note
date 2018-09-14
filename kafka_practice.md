## 利用apache kafka 建立分散式log信息推送

### Kafka架構組件

話題（Topic）：是特定類型的消息流。消息是位元組的有效負載（Payload），話題是消息的分類名或種子（Feed）名。

生產者（Producer）：是能夠發布消息到話題的任何對象。

服務代理（Broker）：已發布的消息保存在一組伺服器中，它們被稱為代理（Broker）或Kafka集群。

消費者（Consumer）：可以訂閱一個或多個話題，並從Broker拉數據，從而消費這些已發布的消息。

### Kafka broker

與其它消息系統不同，Kafka broker是無狀態的。這意味著消費者必須維護已消費的狀態信息。這些信息由消費者自己維護，broker完全不管（有offset managerbroker管理）。

從代理刪除消息變得很棘手，因為代理並不知道消費者是否已經使用了該消息。Kafka創新性地解決了這個問題，它將一個簡單的基於時間的SLA應用於保留策略。當消息在代理中超過一定時間后，將會被自動刪除。這種創新設計有很大的好處，消費者可以故意倒回到老的偏移量再次消費數據。這違反了隊列的常見約定，但被證明是許多消費者的基本特徵。

例： 消費者當機、重啓後仍然可消費之前的訊息、或著延遲一併消費。


### Push vs Pull

1. producer push data to broker，consumer pull data from broker

2. consumer pull的優點：consumer自己控制消息的讀取速度和數量。

3. consumer pull的缺點：如果broker沒有數據，則可能要pull多次忙等待，Kafka可以配置consumer long pull一直等到有數據。

### kafka服务器消息存储策略

kafka以topic來進行消息管理，每個topic包含多個partition，每個partition對應一個邏輯log，有多個segment組成。
每個part在內存中對應一個index，記錄每個segment中的第一條消息偏移。

發布者發到某個topic的消息會被均勻的分佈到多個partition上（或根據用戶指定的路由規則進行分佈），broker收到發布消息往對應partition的最後一個segment上添加該消息，當某個segment上的消息條數達到配置值或消息發布時間超過閾值時，segment上的消息會被flush到磁碟，只有flush到磁碟上的消息訂閱者才能訂閱到，segment達到一定的大小后將不會再往該segment寫數據，broker會創建新的segment。

谈到kafka的存储，就不得不提到分区，即partitions，创建一个topic时，同时可以指定分区数目，分区数越多，其吞吐量也越大，但是需要的资源也越多，同时也会导致更高的不可用性，kafka在接收到生产者发送的消息之后，会根据均衡策略将消息存储到不同的分区中。

### kafka docker swarm mode compose example:

```yaml
version: '3.4'
services:
  kafka1:
    image: wurstmeister/kafka
    depends_on:
        - zoo1
        - zoo2
    deploy:
        mode: global
    ports:
        - target: 9094
          published: 9094
          protocol: tcp
          mode: host
    environment:
        #- KAFKA_LOG_DIRS=/kafka
        - KAFKA_BROKER_ID=1
        - KAFKA_CREATE_TOPICS=test:2:1
        #- HOSTNAME_COMMAND=getent hosts tasks.kafka1|awk '{print $1}'
        - KAFKA_ZOOKEEPER_CONNECT=zoo1:2181,zoo2:2181
        - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
        - KAFKA_ADVERTISED_LISTENERS=INSIDE://:9092,OUTSIDE://192.168.2.108:9094
        - KAFKA_LISTENERS=INSIDE://:9092,OUTSIDE://:9094
        - KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE
    networks:
        graylogend:
    volumes:
        - /var/run/docker.sock:/var/run/docker.sock

  kafka2:
    image: wurstmeister/kafka
    depends_on:
        - zoo1
        - zoo2
    deploy:
        mode: global
    ports:
        - target: 9093
          published: 9093
          protocol: tcp
          mode: host
    environment:
        #- KAFKA_LOG_DIRS=/kafka
        - KAFKA_BROKER_ID=2
        #- HOSTNAME_COMMAND=getent hosts tasks.kafka1|awk '{print $1}'
        - KAFKA_ZOOKEEPER_CONNECT=zoo1:2181,zoo2:2181
        - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
        - KAFKA_ADVERTISED_LISTENERS=INSIDE://:9092,OUTSIDE://192.168.2.108:9093
        - KAFKA_LISTENERS=INSIDE://:9092,OUTSIDE://:9093
        - KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE
    networks:
        graylogend:
    volumes:
        - /var/run/docker.sock:/var/run/docker.sock
  zoo1:
    image: zookeeper:latest
    deploy:
        mode: global
    volumes:
        - /etc/localtime:/etc/localtime:ro
    environment:
        ZOO_MY_ID: 1
        SERVERS: zoo1,zoo2
    networks:
        graylogend:
  zoo2:
    image: zookeeper:latest
    deploy:
        mode: global
    volumes:
        - /etc/localtime:/etc/localtime:ro
    environment:
        ZOO_MY_ID: 2
        SERVERS: zoo1,zoo2
    networks:
        graylogend:
networks:
    graylogend:
```

#### 簡單說明：

* 把內部的port `9092` 映射到外部的port `9094`
* deploy: mode: global ，不能用replica，因爲他會記錄其他cluster的位置。


### java code:

#### consumer:

```JAVA

	public void consume(String brokers, String groupId) throws Exception {
		// Create a consumer
		KafkaConsumer<String, String> consumer;
		// Configure the consumer
		Properties properties = new Properties();
		// Point it to the brokers
		properties.setProperty("bootstrap.servers", brokers);
		// Set the consumer group (all consumers must belong to a group).
		properties.setProperty("group.id", groupId);
		// Set how to serialize key/value pairs
		properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		// When a group is first created, it has no offset stored to start reading from.
		// This tells it to start
		// with the earliest record in the stream.
		properties.setProperty("auto.offset.reset", "earliest");
		consumer = new KafkaConsumer<>(properties);

		// Subscribe to the 'test' topic
		consumer.subscribe(Arrays.asList("test"));

		// Loop until ctrl + c
		int count = 0;
		log.info("start consume");
		while (isStart) {
			// Poll for records
			ConsumerRecords<String, String> records = consumer.poll(200);
			// Did we get any?
			if (records.count() == 0) {
				// timeout/nothing to read
			} else {
				// Yes, loop over records
				for (ConsumerRecord<String, String> record : records) {
					// Display record and count
					count += 1;
					log.info(count + ": " + record.value());
				}
			}
		}
		consumer.close();
		log.info("stop consume");
	}
```

#### producer:

```JAVA

	public void produce(String brokers) throws Exception {

		// Set properties used to configure the producer
		Properties properties = new Properties();
		// Set the brokers (bootstrap servers)
		properties.setProperty("bootstrap.servers", brokers);
		// Set how to serialize key/value pairs
		properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		// So we can generate random sentences
		Random random = new Random();
		String[] sentences = new String[] { "the cow jumped over the moon", "an apple a day keeps the doctor away",
				"four score and seven years ago", "snow white and the seven dwarfs", "i am at two with nature" };

		String progressAnimation = "|/-\\";
		// Produce a bunch of records
		for (int i = 0; i < 5; i++) {
			if (!isStart) {
				break;
			}
			// Pick a sentence at random
			String sentence = sentences[random.nextInt(sentences.length)];
			// Send the sentence to the test topic
			producer.send(new ProducerRecord<String, String>("test", sentence));
			String progressBar = "\r" + progressAnimation.charAt(i % progressAnimation.length()) + " " + i;
			System.out.write(progressBar.getBytes());
		}
		producer.close();
	}
```
