## Graylog docker stack 建立

### 參考：
* https://hub.docker.com/r/graylog/graylog/
* https://community.graylog.org/t/graylog-heap-size-on-docker/2545
* https://docs.graylog.org/
* https://marketplace.graylog.org/addons/6b867cbe-8f5b-4fc9-84d2-fc1a75a0830d
* http://docs.graylog.org/en/2.4/pages/queries.html

### 簡介：

* 日誌管理工具：收集，解析，可視化。
* 整理各種不同的log記錄，可以支援來自不同的主機、程式、虛擬機。
* 可自定log的schema，有市集可以下載現成的
* 簡單的搜尋語法，並可匯出elasticsearch語法利於開發
* 利用docker方便部屬

![部屬圖](http://docs.graylog.org/en/2.4/_images/architec_bigger_setup.png)

### 支援輸入：

* Syslog (TCP, UDP, AMQP, Kafka)
* GELF (TCP, UDP, AMQP, Kafka, HTTP)
* AWS (AWS Logs, FlowLogs, CloudTrail)
* Beats/Logstash
* CEF (TCP, UDP, AMQP, Kafka)
* JSON Path from HTTP API
* Netflow (UDP)
* Plain/Raw Text (TCP, UDP, AMQP, Kafka)

### 安裝：

請先安裝好docker ,docker-compose,docker swarm</br>

docker-compose.yml:
``` yaml
version: '3.4'
services:
    mongo:
      image: mongo:3
      volumes:
          - ./mongo/db:/data/db
      networks:
          graylogend:
    elasticsearch:
      image: elasticsearch:5.6.9
      environment:
          - http.host=0.0.0.0
          - xpack.security.enabled=false
          - transport.host=localhost
          - network.host=0.0.0.0
          # Disable X-Pack security: https://www.elastic.co/guide/en/elasticsearch/reference/5.5/security-settings.html#general-security-settings
          - "ES_JAVA_OPTS=-Xms384m -Xmx384m"
      volumes:
          - ./elasticsearch/data:/usr/share/elasticsearch/data
      networks:
          graylogend:
      ulimits:
        memlock:
          soft: -1
          hard: -1

    graylog:
      image: graylog/graylog:2.4
      volumes:
          - /etc/localtime:/etc/localtime:ro
          - ./graylog/data/journal:/usr/share/graylog/data/journal
          - ./graylog/data/config:/usr/share/graylog/data/config
      environment:
          - GRAYLOG_SERVER_JAVA_OPTS=-Xms384m -Xmx384m -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:NewRatio=1 -XX:MaxMetaspaceSize=256m -server -XX:+ResizeTLAB -XX:+UseConcMarkSweepGC -XX:+CMSConcurrentMTEnabled -XX:+CMSClassUnloadingEnabled -XX:+UseParNewGC -XX:-OmitStackTraceInFastThrow
          - GRAYLOG_WEB_ENDPOINT_URI=https://graylog.mycasa.care/api
          - GRAYLOG_PASSWORD_SECRET=somepasswordpepper
          - GRAYLOG_ROOT_PASSWORD_SHA2=yourpassword_SHA2
          # Password: admin
      depends_on:
          - mongo
          - elasticsearch
      networks:
          frontend:
              aliases:
                  - graylog
          graylogend:
      ports:
          - 514:514/udp
          - 514:514
          - 12201:12201
          - 12201:12201/udp
      expose:
          - 9000

    nginx:
      image: nginx/nginx:latest
      depends_on:
          - graylog
      volumes:
          - /etc/localtime:/etc/localtime:ro
          - ./nginx/nginx.conf:/etc/nginx/conf.d/my.conf
      environment:
          - "TZ=Asia/Taipei"
      networks:
          frontend:
      ports:
          - 80:80
          - 443:443
networks:
    frontend:
```

#### mongo db:
負責單純記錄graylog的schema

#### elasticsearch:
負責搜尋log用</br>
參數: </br>
ES_JAVA_OPTS: 調整JAVA參數，範例爲調整JVM記憶體大小

#### graylog:
web界面、連線終端。 </br>
參數: </br>
GRAYLOG_SERVER_JAVA_OPTS: 從dockerfile 摳來的，多增加了JAM大小</br>
GRAYLOG_WEB_ENDPOINT_URI： 終端URI</br>
GRAYLOG_ROOT_PASSWORD_SHA2： admin的sha2密碼</br>
ports的部分可以依照inputs的需求開</br>

#### nginx:
web server，本範例拿來提供log用。</br>
參數：</br>
volumes: ./nginx/nginx.conf -> 設定virturl host以及log 例：
```javascript

  server {
    listen 80;
    server_name $SERVER_NAME;
    root $ROOT_PATH;
    index index.php index.html index.htm;

    allow all;

    location / {
      set_real_ip_from 0.0.0.0/0;
      real_ip_header X-Real-IP;
      real_ip_recursive on;

      proxy_redirect off;

      proxy_set_header Host \$host;

      proxy_set_header X-Real-IP \$remote_addr;

      proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;

      proxy_next_upstream error timeout invalid_header http_500;

      proxy_connect_timeout 10s;

      proxy_read_timeout 10s;
      proxy_pass http://graylog:9000;

      error_page 404 /404.html;


      # WebSocket support
      proxy_http_version 1.1;
      proxy_set_header Upgrade \$http_upgrade;
      proxy_set_header Connection "upgrade";
    }
  }
log_format graylog2_json escape=json '{ "timestamp": "$time_iso8601", '
                  '"remote_addr": "$remote_addr", '
                  '"body_bytes_sent": $body_bytes_sent, '
                  '"request_time": $request_time, '
                  '"response_status": $status, '
                  '"request": "$request", '
                  '"request_method": "$request_method", '
                  '"host": "$host",'
                  '"upstream_cache_status": "$upstream_cache_status",'
                  '"upstream_addr": "$upstream_addr",'
                  '"http_x_forwarded_for": "$http_x_forwarded_for",'
                  '"http_referrer": "$http_referer", '
                  '"http_user_agent": "$http_user_agent" }';

access_log syslog:server=graylog:12301,facility=local0,tag=nginx,severity=info graylog2_json;
error_log syslog:server=graylog:12302,facility=local0,tag=nginx,severity=warn;
```

#### 設定graylog:

1. 下載設定檔到`./graylog/data/config`：
```bash
wget https://raw.githubusercontent.com/Graylog2/graylog-docker/2.4/config/graylog.conf
wget https://raw.githubusercontent.com/Graylog2/graylog-docker/2.4/config/log4j2.xml
```
2. 啟動`docker-compose up -d`，瀏覽`http://$SERVER_NAME` </br>
3. 選擇Content packs: ![](img/gds1.png)
4. 前往 https://marketplace.graylog.org/addons/6b867cbe-8f5b-4fc9-84d2-fc1a75a0830d 下載並解壓縮
5. 回到Content packs點擊`Import content pack` 選擇剛才解壓縮的`contenpack.json`並上傳
6. `Select content packs`會多出`nginx`的選項，選擇`nginx-json`然後點選旁編的`Apply content`。 ![](img/gds2.png)
7. 到此爲止設定完畢，點選toolbar的`Search`即可看到log記錄。

### 常用query：

* 訊息包含abc: `abc`
* 訊息包含abc或def: `abc def`
* 訊息完整包含api cmd: `"api cmd"`
* 查詢某欄位的abc: `field:abc`
* 查詢某欄位的abc或def: `field:(abc def)`
* 訊息有包含header這個欄位: `_exists_:header`
* 用正規表達式: /[0-9]+/
* 支援operater: `AND OR NOT`
* Wildcards: `*`取代零到多個字，`?`取代一個字
* 糢糊搜尋: `~`類似sql的`%`
* 範圍: `{}`爲不包含`[]`爲包含，如:`response_status:{200 TO 400]` 顯示201~400</br>
  也可以單邊界搜尋：`response_status:>=400]`</br>
  也可以混和條件: `response_status:(>=400 AND <500)`
* 下面爲特殊字元，需加反斜線`\`:</br>
  `&& || : \ / + - ! ( ) { } [ ] ^ " ~ * ?`
* 下面爲可用的時間搜尋:
```
`1st of april to 2 days ago`
`4 hours ago`
`last month`
`yesterday midnight +0200 to today midnight +0200`
```

### 操作圖形：
![](img/gds3.png)

* chart:
![](img/gds4.png)
* Quick Values:
![](img/gds5.png)
* Field Statistics:
![](img/gds6.png)

### 装饰器:

裝飾器允許你在搜尋的過程中修改欄位，而不會影響到硬碟上的資料。它可以讓你的資料變得更容易閱讀，也可以結合不同的欄位資料。</br>
裝飾器是根據每一個數據流設定的(包含預設數據流)，你也可以呈現一個訊息在不同的數據流。
</br></br>
裝飾器是不會保存在硬碟裏的，所以你沒辦法直接搜尋裝飾後的數據流，也無法使用分析器等工具。</br>
</br>

#### 如何添加裝飾器：

![](http://docs.graylog.org/en/2.4/_images/create_decorator.png)

1. 點擊`Decorators`在你側邊的搜尋欄
2. 從下拉欄中選擇你想要的裝飾器，然後點擊`apply`

#### Syslog severity mapper:

Syslog severity mapper 裝飾器可以讓系統日誌的安全層級數字轉變成可讀的字串。
例如：應用這個裝飾器在'level'欄位上，將會讓內容由'4'轉變成'Warning (4)'。

你必需要提供這些資訊才能使用Syslog severity mapper裝飾器：

* **Source field:** 包含系統日誌層級的欄位
* **Target field:** 存放可讀的日誌層級的欄位，如果你想取代原有的欄位，也可以使用舊有的欄位名稱

#### Format string:

Format string 裝飾器提供了一個簡單的方式讓你可以組合不同欄位的內容。
他也可以用來修改欄位內容並且呈現，而不影響原來欄位的內容。

你必需要提供這些資訊才能使用format string裝飾器：

* **Format string:**
  一個用來格式化搜尋結果的模式。 你可以利用'\${}'來包住你的搜尋結果的欄位。
  例如： '\${source}' 將會增加'source'這個欄位的內容到你的字串。
* **Target field:**
  用來存放成果字串的欄位名稱
* **Require all fields (optional):**
  當你會用到所有欄位時，勾選這個項目

舉個例子，使用這個 format string：
'Request to \${controller}#\${action} finished in ${took_ms}ms with code ${http_response_code}'，
將會產生'Request to PostsController#show finished in 57ms with code 200'，
並會讓這個字串作爲你的搜尋中的其中一個欄位。

#### Pipeline Decorator:

pipeline 裝飾器可以讓現有的'processing pipeline'來處理訊息。
跟直接使用processing pipeline的差異在於，pipeline裝飾器的結果不會保留下來，它只有在搜尋的時候才會轉換。

你得先有一個processing pipeline才可以使用pipeline裝飾器。

* **Note:**
  當你的processing pipeline被使用爲裝飾器的時後，請將它從stream中移除，避免重複執行。

#### Debugging decorators:

當你的訊習沒有按照預想的呈現時，或著想知道你的訊息原來是長得如何。
可以在訊息的'detail'中按下 “Show changes” ，你可以看到裝飾器進行的所有改變。

![](http://docs.graylog.org/en/2.4/_images/pipeline_decorator_show_changes.png)

在這張表中，刪除掉的訊息是紅色的，新增的是綠色的。

### Pipeline:

### log保留時限：

### 計算log可能的大小：
GB/day x Ret. Days x 1.3 = storage req.

### 透過osgi bundle把mysql log傳送到graylog:

```java

import java.net.InetSocketAddress;
import java.util.Map;

import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.json.JSONArray;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kingnet.osgi.http.mysql.MysqlConnectionPool;

@Component
public class Example {

	private MysqlConnectionPool dbpool;
	private boolean isStop;
	private static final Logger log = LoggerFactory.getLogger(Example.class);

	/**
	 * 新增mysql connection pool依賴
	 * @param dbpool
	 */
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	public void setDbpool(MysqlConnectionPool dbpool) {
		this.dbpool = dbpool;
	}

	public void unsetDbpool(MysqlConnectionPool dbpool) {
		this.dbpool = null;
	}

	@Activate
	public void start(BundleContext context) {
		isStop = false;
		log.info("start");

		//設定graylog連線機制
		final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress("graylog", 514))
				.transport(GelfTransports.UDP)//
				.queueSize(512)//
				.connectTimeout(5000)//
				.reconnectDelay(1000)//
				.tcpNoDelay(true)//
				.sendBufferSize(32768);

		//建立graylog傳輸器
		final GelfTransport transport = GelfTransports.create(config);
		//建立graylog訊息的builder
		final GelfMessageBuilder builder = new GelfMessageBuilder("jdbc", "mysql")//
				.level(GelfMessageLevel.INFO);

		//新增一個thread執行訊息傳輸，避免阻塞osgi main thread
		new Thread(() -> {

			JSONArray res;
			try {
				//j爲頁數40786爲總頁數
				for (int j = 11789; j < 40786; j++) {

					//判斷程式是否停止
					if (isStop) {
						break;
					}
					//取得mysql資料
					res = dbpool.toJSONArray("SELECT * " + "FROM  `Logger` " + "LIMIT " + j * 1000l + ", 1000");

					for (int i = 0; i < res.length(); i++) {
						//將json轉成map
						Map<String, Object> map = res.getJSONObject(i).toMap();

						//建立graylog訊息
						final GelfMessage message = builder.message("%{message}").additionalFields(map).build();

						//傳送訊息
						transport.send(message);

					}
					log.info("j =" + j);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("end");
		}).start();
	}

	@Deactivate
	public void stop(BundleContext context) {
		isStop = true;
	}
}


```
