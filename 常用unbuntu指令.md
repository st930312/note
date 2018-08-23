# 常用unbuntu指令

## acra創建步驟:
1. cd acra-storage
2. couchapp push http://[login]:[password]@[your.couchdb.host]:[port]/acra-[yourappname]

 **註：yourappname 必須是小寫**

## 備份mysql資料庫：
* mysqldump -u root -p --all-databases > backup.sql;
* 還原： mysql -u root -p < alldb.sql

## 備份ubuntu:
* tar -cvpzf ubuntu20160401.tgz --exclude=/ubuntu20160401.tgz --exclude=/proc --exclude=/lost+found  --exclude=/mnt --exclude=/sys --exclude=/media --exclude=/dev/

## ubuntu啟動命令位置：
* 啟動指令下在 /etc/rc.local
