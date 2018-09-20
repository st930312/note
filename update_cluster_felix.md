## 如何更新叢集bundle

### 參考工具：

* kafka

### 思路：

* 建立kafka相關的docker container
* 建一個跨網域的docker volume，docker_felix會mount它
* 一個bundle建立web界面的更新接收api，並向消息系統推送訊息producer，也實現更新邏輯
* 一個bundle建立接收消息系統的consumer及producer，並提供java api給同一個felix內的bundle傳遞/監聽消息

### 訊息傳遞順序
![](img/felix_update.png)

### 跨網域docker volume
