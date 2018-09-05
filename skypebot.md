## skypebot 從零開始

### 準備工具：
* 作業系統: ubuntu 16.0.1
* nodejs: v10.9.0
* npm: 6.4.1
* 一個microsoft帳號
* 一個自己的網域（需要有https 可用letsencript 筆者是用cloudflare）

### 參考文件：
* https://docs.microsoft.com/en-us/azure/bot-service/nodejs/bot-builder-nodejs-quickstart?view=azure-bot-service-3.0

### 申請Azure並建立bot:

註： 以下爲免費適用帳號</br>

1. 進入 https://azure.microsoft.com/en-us/free/ 並按照步驟完成註冊
2. 完成上述步驟後會進入儀表板 ![](img/sk1.png)
3. 在所有服務那邊搜尋`bot`，選擇`bot服務`並建立 ![](img/sk2.png)
4. 依照顯示的選項填即可，要注意的是定價層那邊選免費的
5. 接着等待他建立，大概5分鐘內會建立完成，等他建立完成的期間我們先去建立bot service

### 用nodejs快速建立bot:

1. 安裝所需的程式
``` bash
npm install -g yo
npm install -g generator-botbuilder@preview
```
2. 利用genrator快速建立樣板
``` bash
yo botbuilder
```
3. 選擇bot名字,剩下的都enter帶過
4. 現在會有一個新資料夾，名字是你的bot，我們進去並用編輯器打開app.js
``` javascript
const { BotFrameworkAdapter, MemoryStorage, ConversationState } = require('botbuilder');
const restify = require('restify');

// Create server
let server = restify.createServer();
server.listen(process.env.port || process.env.PORT || 3978, function () {
    console.log(`${server.name} listening to ${server.url}`);
});

// Create adapter
const adapter = new BotFrameworkAdapter({
    appId: process.env.MICROSOFT_APP_ID,
    appPassword: process.env.MICROSOFT_APP_PASSWORD
});

// Add conversation state middleware
const conversationState = new ConversationState(new MemoryStorage());
adapter.use(conversationState);

// Listen for incoming requests
server.post('/api/messages', (req, res) => {
    // Route received request to adapter for processing
    adapter.processActivity(req, res, (context) => {
        if (context.activity.type === 'message') {
            const state = conversationState.get(context);
            const count = state.count === undefined ? state.count = 0 : ++state.count;
            return context.sendActivity(`${count}: You said "${context.activity.text}"`);
        } else {
            return context.sendActivity(`[${context.activity.type} event detected]`);
        }
    });
});
```
5. 先不仔細說明，這程式的作用爲echo你輸入的訊息，我們要注意的是程式裡面有`process.env.MICROSOFT_APP_ID`,`process.env.MICROSOFT_APP_PASSWORD`這兩個參數要設定，這時候我們就要回到azure的控制板去拿到id跟passwd，這時候bot應該創建完成了。
6. 
