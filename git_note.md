## git 指令筆記：

### cherry-pick :
只選出需要的commit並跟現在的branch進行merge
```
$ git checkout master
Switched to branch 'master'
$ git cherry-pick 99daed2
```

### Squash:
<<<<<<< HEAD
=======

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


12
34
>>>>>>> 9003935... a
