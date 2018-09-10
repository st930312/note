## 如何更新叢集bundle

### 參考工具：

* splunk
* syslog-ng


### 思路：

建立一個消息系統的docker container，</br>
一個bundle建立web界面的更新接收api，並向消息系統推送訊息producer，</br>
一個bundle建立接收消息系統的consumer，並提供java api給同一個felix內的bundle監聽消息</br>
一個bundle實現更新邏輯
