## Gitlab Code Review Flow

1. 專案設定爲private
2. 將開發者加入member中，權限爲devloper
3. 開發者clone專案之後依據新功能創建新的branch  git checkout -b {functions}
4. 開發者將新的branch push到remote origin
5. 開發完成後透過網頁發起新的merage request
6. master收到通知後審核code 沒問題即可透過網頁merage
7. 開發者回到master進行pull
8. loop step.3 - step.7
