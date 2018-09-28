## GlusterFS Note

### 目標：
* 利用虛擬機建立glusterfs cluster
* 利用docker plugin來mount volume
* Brick 架構：

|節點名稱|	Brick| Path|	Size|	Disk|	VG|	Path|
|------|--------|-----|------|-----|---|-----|
|S1|	lv_01|	10GB|	/dev/sda1|	vg_group|	/bricks/brick_01|
|S1|	lv_02| 10GB|	/dev/sda1|	vg_group|	/bricks/brick_02|
|S2|	lv_01| 10GB|	/dev/sda1|	vg_group|	/bricks/brick_01|
|S2|	lv_02| 10GB|	/dev/sda1|	vg_group|	/bricks/brick_02|

### 參考：
* http://www.l-penguin.idv.tw/book/Gluster-Storage_GitBook/index.html

### 建立虛擬機：
1. 採用vagrant box `envimation-VAGRANTSLASH-ubuntu-xenial-docker` 40gb * 2
2. 增加硬碟大小：
```bash
# Clone the .vmdk image to a .vdi.
vboxmanage clonehd "virtualdisk.vmdk" "new-virtualdisk.vdi" --format vdi
# Resize the new .vdi image (40960 == 40 GB).
vboxmanage modifyhd "new-virtualdisk.vdi" --resize 40960
# Optional; switch back to a .vmdk.
VBoxManage clonehd "cloned.vdi" "resized.vmdk" --format vmdk
```
3. 安裝依賴
```bash
apt update
apt-get -y install thin-provisioning-tools
```
4. 建立一個硬碟分區
```bash
fdisk /dev/sda
Command (m for help): n
Select (default p): p
# 此時已經建立完畢
Command (m for help): p
# 顯示現有硬碟情形，應該會像這樣：
#
# Device     Boot    Start       End   Sectors  Size Id Type
# /dev/sda1  *        2048  19455999  19453952  9.3G 83 Linux
# /dev/sda2       19456000  20479999   1024000  500M 83 Linux
# /dev/sda3       20480000 125829119 105349120 40.1G 83 Linux
Command (m for help): w
# 寫入硬碟
reboot
```
5. 啓用LVM
```bash
apt-get -y install lvm2
pvcreate /dev/sda3
# Physical volume "/dev/sda3" successfully created
vgcreate vg_group /dev/sda3
# Volume group "vg_group" successfully created
lvcreate -L 40G -T vg_group/brick_spool
# Logical volume "brick_spool" created.
lvs
#  LV          VG       Attr       LSize  Pool Origin Data%  Meta%  Move Log Cpy%Sync Convert
#  brick_spool vg_group twi-a-tz-- 40.00g             0.00   0.48
lvcreate -V20G -n lv_01 -T vg_group/brick_spool
#  Logical volume "lv_01" created.
lvs
#  LV          VG       Attr       LSize  Pool        Origin Data%  Meta%  Move Log Cpy%Sync Convert
#  brick_spool vg_group twi-aotz-- 40.00g                    0.00   0.49
#  lv_01       vg_group Vwi-a-tz-- 20.00g brick_spool        0.00
lvcreate -V20G -n lv_02 -T vg_group/brick_spool
#  Logical volume "lv_02" created.
lvs
#  LV          VG       Attr       LSize  Pool        Origin Data%  Meta%  Move Log Cpy%Sync Convert
#  brick_spool vg_group twi-aotz-- 40.00g                    0.00   0.50
#  lv_01       vg_group Vwi-a-tz-- 20.00g brick_spool        0.00
#  lv_02       vg_group Vwi-a-tz-- 20.00g brick_spool        0.00
apt -y install xfsprogs
mkfs.xfs -i size=512 /dev/vg_group/lv_01
mkfs.xfs -i size=512 /dev/vg_group/lv_02
```
6. 掛載volume
```bash
mkdir -p /bricks/{brick_01,brick_02}
apt install vim -y

# 編輯 /etc/fstab 檔案讓開機時也自動掛載 LV
/dev/vg_group/lv_01 /bricks/brick_01 xfs rw,noatime,nouuid,inode64 0 0
/dev/vg_group/lv_02 /bricks/brick_02 xfs rw,noatime,nouuid,inode64 0 0

# 掛載目錄
mount -a

# 建立 brick 資料目錄
mkdir /bricks/{brick_01,brick_02}/brick

# 檢查是否正確掛載
df

```
7. 重複上述動作建立S2

### 安裝glusterfs-servers

1. `apt install glusterfs-server -y`
2. 確認兩臺的ip位置，並修改/etc/hosts，以S1爲例：
```bash
#/etc/hosts
127.0.0.1       localhost
127.0.1.1       base-debootstrap
192.168.x.x   S2
127.0.0.1     S1
```
3. 新增節點
`gluster peer probe S2`
4. 確認節點狀態
`gluster peer status`
5. 移除節點指令： `gluster peer detach S2`

### 設定gluster-servers

採用Distributed-Replicated 模式</br>
設定的brick爲:</br>

```bash
S1:lv_01==S2:lv01
S1:lv_02==S2:lv02
```

1. 建立 Volume
```bash
gluster volume create distrepl_vol replica 2 \
 S1:/bricks/brick_01/brick/ \
 S2:/bricks/brick_01/brick/ \
 S1:/bricks/brick_02/brick/ \
 S2:/bricks/brick_02/brick/
```
2. 檢查volume:
`gluster volume list`
3. 啟動volume:
`gluster volume start distrepl_vol`
4. 檢查狀態：
`gluster volume info distrepl_vol`

### 直接替換 Brick:

更換 Brick 前請先確定所屬節點已被加入 Storage Pool。

1. 設定替換作業
`glsuter volume replace-brick distrepl_vol \
 S1:/bricks/brick_01/brick/ \
 S1:/bricks/brick_03/brick/ \
 start`

2. 查看檔案移動狀態
`glsuter volume replace-brick distrepl_vol \
 S1:/bricks/brick_01/brick/ \
 S1:/bricks/brick_03/brick/ \
 status`

3. 通知 Volume 確認移除舊的 Brick
`glsuter volume replace-brick distrepl_vol \
 S1:/bricks/brick_01/brick/ \
 S1:/bricks/brick_03/brick/ \
 commit`

4. 確認 volume 成員
`gluster volume info distrepl_vol`

### 新增 Brick:

若要在 Distributed-Replicated 模式增加 Brick 的話，則必需是 Replicated 數量的倍數。以本例來說因為 replica 設定為 2，所以爾後再新增 Brick 時就要新增為 2 的倍數。

1. 新增 Brick
`glsuter volume add-brick distrepl_vol \
  S1:/bricks/brick_03/brick/ \
  S2:/bricks/brick_03/brick/`

2. 因為 Distributed 的數量改變，所以要將實體檔案位置重新分配：
`gluster volume rebalance distrepl_vol start`

### glusterfs 掛載：

client請先安裝glusterfs client `apt-get install glusterfs-client`

1. 直接掛載：
`mount -t glusterfs rw S1:/distrepl_vol /mnt/distrepl_vol`

2. 開機自動掛載：
```bash
# 寫入/etc/fstab
S1:/distrepl_vol    /mnt/distrepl_vol    glusterfs    rw    0    0
```

### 利用docker plugin掛載

1. 安裝
`docker plugin install sapk/plugin-gluster`

2. docker compose:
```yaml
volumes:
  some_vol:
    driver: sapk/plugin-gluster
    driver_opts:
      voluri: "<volumeserver>:<volumename>"
```
