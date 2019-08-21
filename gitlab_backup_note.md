## gitlab 備份筆記

### 目的： 異機備份

### 環境： 兩臺不同版本的ubuntu

### 做法：

#### step 1. 備份gitlab: </br> </br>

`sudo gitlab-rake gitlab:backup:create` </br> </br>
正常輸出：

    Dumping database tables:
    - Dumping table events... [DONE]
    - Dumping table issues... [DONE]
    - Dumping table keys... [DONE]
    - Dumping table merge_requests... [DONE]
    - Dumping table milestones... [DONE]
    - Dumping table namespaces... [DONE]
    - Dumping table notes... [DONE]
    - Dumping table projects... [DONE]
    - Dumping table protected_branches... [DONE]
    - Dumping table schema_migrations... [DONE]
    - Dumping table services... [DONE]
    - Dumping table snippets... [DONE]
    - Dumping table taggings... [DONE]
    - Dumping table tags... [DONE]
    - Dumping table users... [DONE]
    - Dumping table users_projects... [DONE]
    - Dumping table web_hooks... [DONE]
    - Dumping table wikis... [DONE]
    Dumping repositories:
    - Dumping repository abcd... [DONE]
    Creating backup archive: $TIMESTAMP_gitlab_backup.tar [DONE]
    Deleting tmp directories...[DONE]
    Deleting old backups... [SKIPPING]

</br>

#### setp 2. 複製備份到另外一臺機器: </br> </br>

檔案備份位置會在 `/var/opt/gitlab/backup/{seed}_{date}_{version}_gitlab_backup.tar` </br>
範例檔名： `1493107454_2018_04_25_10.6.4-ce_gitlab_backup.tar`</br>
接着下轉移指令：
`scp -P {port} {account}@{addr}:{file_location} {destnation_location}` </br>
通常會有權限問題，所以要先把檔案轉到可讀取的目錄 </br>
例如： `cp 1493107454_2018_04_25_10.6.4-ce_gitlab_backup.tar /home/zero/` </br>
然後再下 scp </br> </br>

#### step 3. 在欲轉移的機器安裝gitlab </br></br>

1.  首先加入repository，以ce版本爲例： <br>
    `curl -s https://packages.gitlab.com/install/repositories/gitlab/gitlab-ee/script.deb.sh | sudo bash` </br>
2.  安裝與源機器版本相同的gitlab: </br>
    `apt-get install -y gitlab-ce=10.1.4-ce.0` </br></br>

## 可安裝版本在這邊尋找 <br>https://packages.gitlab.com/gitlab/gitlab-ce</br>

#### step 4. 還原gitlab: </br></br>

1.  停止執行中的gitlab: </br>

```
    sudo gitlab-ctl stop unicorn
    sudo gitlab-ctl stop sidekiq
    # Verify
    sudo gitlab-ctl status
```

2.  還原：</br>
    `gitlab-rake gitlab:backup:restore BACKUP=1493107454_2018_04_25_10.6.4-ce` </br> </br>

#### step 5. 調整設定檔 </br></br>

1.  覆盖原来gitlab的 db_key_base 到新的gitlab </br>
    db_key_base `位置在 /etc/gitlab/gitlab-secrets.json` </br>

2. 調整 `/etc/gitlab/gitlab.rb`：
```
external_url 'gitlab.example.com'

gitlab_rails['gitlab_ssh_host'] = 'ssh.example.com'

gitlab_rails['gitlab_shell_ssh_port'] = 2757

web_server['external_users'] = ['www-data']

 nginx['enable'] = false
```

3. 新增nginx config:

```
proxy_cache_path proxy_cache keys_zone=gitlab:10m max_size=1g levels=1:2;
proxy_cache gitlab;

map $http_upgrade $connection_upgrade {
     default upgrade;
     ''      close;
}

upstream gitlab-workhorse {
 server unix:/var/opt/gitlab/gitlab-workhorse/socket;
}


server {

  listen 0.0.0.0:443 ssl;

  server_name gitlab.amiasay.net;
  server_tokens off; ## Don't show the nginx version number, a security best practice

  ## Increase this if you want to upload large attachments
  ## Or if you want to accept large git objects over http
  client_max_body_size 0;


  ssl on;
  ssl_certificate /etc/nginx/ssl/pem.crt;
  ssl_certificate_key /etc/nginx/ssl/pem.key;

  # GitLab needs backwards compatible ciphers to retain compatibility with Java IDEs
  ssl_ciphers "ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-SHA384:ECDHE-RSA-AES128-SHA256:ECDHE-RSA-AES256-SHA:ECDHE-RSA-AES128-SHA:ECDHE-RSA-DES-CBC3-SHA:AES256-GCM-SHA384:AES128-GCM-SHA256:AES256-SHA256:AES128-SHA256:AES256-SHA:AES128-SHA:DES-CBC3-SHA:!aNULL:!eNULL:!EXPORT:!DES:!MD5:!PSK:!RC4";
  ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
  ssl_prefer_server_ciphers on;
  ssl_session_cache shared:SSL:10m;
  ssl_session_timeout 5m;

  ## Real IP Module Config
  ## http://nginx.org/en/docs/http/ngx_http_realip_module.html

  ## HSTS Config
  ## https://www.nginx.com/blog/http-strict-transport-security-hsts-and-nginx/
  add_header Strict-Transport-Security "max-age=31536000";

  ## Individual nginx logs for this GitLab vhost
  access_log  /var/log/gitlab/nginx/gitlab_access.log;
  error_log   /var/log/gitlab/nginx/gitlab_error.log;

  if ($http_host = "") {
    set $http_host_with_default "gitlab.amiasay.net";
  }

  if ($http_host != "") {
    set $http_host_with_default $http_host;
  }

  ## If you use HTTPS make sure you disable gzip compression
  ## to be safe against BREACH attack.


  ## https://github.com/gitlabhq/gitlabhq/issues/694
  ## Some requests take more than 30 seconds.
  proxy_read_timeout      3600;
  proxy_connect_timeout   300;
  proxy_redirect          off;
  proxy_http_version 1.1;

  proxy_set_header Host $http_host_with_default;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection $connection_upgrade;
  proxy_set_header X-Forwarded-Proto $scheme;

  location ~ (\.git/gitlab-lfs/objects|\.git/info/lfs/objects/batch$) {
    proxy_cache off;
    proxy_pass http://gitlab-workhorse;
    proxy_request_buffering off;
  }

  location / {
    proxy_cache off;
    proxy_pass  http://gitlab-workhorse;
  }

  location /assets {
    proxy_cache gitlab;
    proxy_pass  http://gitlab-workhorse;
  }

  error_page 404 /404.html;
  error_page 422 /422.html;
  error_page 500 /500.html;
  error_page 502 /502.html;
  location ~ ^/(404|422|500|502)(-custom)?\.html$ {
    root /opt/gitlab/embedded/service/gitlab-rails/public;
    internal;
  }

}
```

#### step 6. 重新啓動gitlab `gitlab-ctl restart`

#### step 7. 重新讀取nginx設定 `service nginx reload`

### 定期備份：

#### step 1. 建立script:

``` bash
#!/bin/bash    
echo "start gitlab backup"
gitlab-rake gitlab:backup:create

echo "start del old backup"
# 資料夾檔案數
FILE_COUNT=`ls -l /var/opt/gitlab/backups/ | wc -l`

# 刪掉除了倒數三個之外的檔案
FILE_NEED_DEL=`ls -l /var/opt/gitlab/backups/ | awk -v count=$((FILE_COUNT - 3)) '{if(NR <= count)print $9}'`
echo $FILE_NEED_DEL

IFS=' ' read -r -a array <<< $FILE_NEED_DEL

for i in "${!array[@]}"
do
  echo "del "${array[$i]}
  rm "/var/opt/gitlab/backups/"${array[$i]}
done
echo "end del old backup"

# 取得最新備份檔名
FILE_NAME="/var/opt/gitlab/backups/"`ls -l /var/opt/gitlab/backups/ | awk '{if(NR == 4)print $9}'`

echo "start scp"

scp -P 2757 $FILE_NAME {account}@{ip}:{destnation_location}

echo "end scp"
```
註：</br>
1. scp 所用到的ssh key 認證方式：

```
#建立ssh key（本機）
ssh-keygen -t rsa -b 4096

#在遠端主機的帳號目錄下新建檔案，例：
mkdir /home/aaa/.ssh
vi /home/aaa/.ssh/authorized_keys

#將步驟一產生的檔案id_rsa.pub裡面的字串複製到遠端authorized_keys裡面，這時候文字檔應該會像：
ssh-rsa AAAAB3NzaC1kc3MAAACBAPWP8FS0iatXx3z7o/alB1pI8a…. root@example.com

此時便可用scp而不需輸入密碼
```

2. wc 指令用法： </br>

```
wc -l <文件名> 輸出行數統計
wc -c <文件名> 輸出字節數統計
wc -m <文件名> 輸出字符數統計
wc -L <文件名> 輸出文件中最長一行的長度
wc -w <文件名> 輸出單詞數統計
```

3. awk 用法請參照鳥哥

#### step 2. 將script移動到/etc/cron.daily下面
註: /etc/cron.daily 下檔名不能爲*.sh
