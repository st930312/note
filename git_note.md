## git 指令筆記：

### cherry-pick :
只選出需要的commit並跟現在的branch進行merge
```
$ git checkout master
Switched to branch 'master'
$ git cherry-pick 99daed2
```

### Squash:

#### Merge Squash:
當其他分支合併進來時，只顯示一個commit</br>
1. 建新分支 `$ git checkout -b test1`
2. 修改內容並commit
```bash
$ git add .
$ git commit -m '1'
$ git add .
$ git commit -m '2'
```
3. 切換回master `$ git checkout master`
4. Merge Squash `$ git merge test1 --squash`
5. commit
```bash
$ git add .
$ git commit -m '12'
```
此時查看git log可以看到沒有merge的記錄，只有剛才新增的commit記錄。

#### Rebase Squash:
這是用來整理自己commit記錄，方便配合版本號或著merge記錄。</br>
時常會在一天沒有開發完功能，卻要記錄而臨時commit。或著功能開發到一半有bug要修正，就只好臨時commit。</br>
##### 使用方法：
1. 輸入`$ git rebase -i` </br>
他會顯示一個vim視窗，像這樣：
```
pick d8e1dfd 1.支援relay josn 2.修正bug 3. 固定字體大小
pick ecefb84 更新新版relay
pick 6925dc7 更新firebase 註冊/移除方式
pick 7f5c5e8 測試自定對話 layout , 調整語音時間
pick dd17501 1.改寫json reciver 2.修正登入token判斷
pick 3b156e0 1.修正歷史訊息錯誤
pick 10c369a 加回stamp

# Rebase a5d8205..10c369a onto a5d8205 (8 commands)
#
# Commands:
# p, pick = use commit
# r, reword = use commit, but edit the commit message
# e, edit = use commit, but stop for amending
# s, squash = use commit, but meld into previous commit
# f, fixup = like "squash", but discard this commit's log message
# x, exec = run command (the rest of the line) using shell
# d, drop = remove commit
#
# These lines can be re-ordered; they are executed from top to bottom.
#
# If you remove a line here THAT COMMIT WILL BE LOST.
#
# However, if you remove everything, the rebase will be aborted.
#
# Note that empty commits are commented out
```
歷史順序從上到下，最下面是最新的。</br>
我想要把 10c369a 藏起來，所以把前面的pick改成 s </br>
然後儲存離開，接着輸入</br>`git log --graph --pretty=oneline --abbrev-commit`</br>
可以發現3b156e0消失了，但更動還存在。</br>

### rebase完，但發現錯了要如何回復？

使用`git reflog`
可以看到之前下的指令所造成的變更:
```
f689730 (HEAD -> master) HEAD@{0}: rebase -i (finish): returning to refs/heads/master
f689730 (HEAD -> master) HEAD@{1}: rebase -i (continue): add git note
e41bb82 (origin/master) HEAD@{2}: rebase -i (start): checkout refs/remotes/origin/master
9003935 HEAD@{3}: reset: moving to HEAD@{4}
e41bb82 (origin/master) HEAD@{4}: rebase -i (finish): returning to refs/heads/master
e41bb82 (origin/master) HEAD@{5}: rebase -i (start): checkout refs/remotes/origin/master
e41bb82 (origin/master) HEAD@{6}: rebase -i (finish): returning to refs/heads/master
e41bb82 (origin/master) HEAD@{7}: rebase -i (start): checkout refs/remotes/origin/master
...
```
選擇想要回覆的號碼下指令即可，例：`git reset --hard HEAD@{2}`
