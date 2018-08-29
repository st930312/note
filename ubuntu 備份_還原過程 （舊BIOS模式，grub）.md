# ubuntu 備份/還原過程 （舊BIOS模式，grub）
1.mount行動硬碟到/mnt下，並且將系統的 / 壓縮成tar到/mnt/目錄下
```
tar -cvpzf /mnt/ubuntu[日期].tar --exclude=/ubuntu20160401.tar --exclude=/proc --exclude=/lost+found  --exclude=/mnt --exclude=/sys --exclude=/media --exclude=/dev/
```
2.用live usb開啟ubuntu，分割sda硬碟成兩個partition，partition 1 (16M)為BIOS Boot system 格式為ext2 ，partition 2 為 Linux File System 格式為ext2
```
df -h              //先看系統配置狀態
fdisk -l /dev/sda   //查看硬碟資訊
fdisk /dev/sda      //用fdisk分割

mkfs.ext2 /dev/sda1
mkfs.ext2 /dev/sda2
```
4.掛載行動硬碟到/backup，將要還原的系統硬碟掛載到/mnt，並解壓縮
```
sudo mount /dev/sda2 /mnt
sudo moutn /dev/sdb2 /backup

tar xvpfz /backup/ubuntu[日期].tar -C /mnt
```

5.更改/mnt/etc/fstab配置
```
vi /mnt/etc/fstab

＋ /dev/sda2     /     ext2   errors=remount-ro  0   1
```

6.更新grub
```
sudo mount --bind /dev /mnt/dev &&
sudo mount --bind /dev/pts /mnt/dev/pts &&
sudo mount --bind /proc /mnt/proc &&
sudo mount --bind /sys /mnt/sys

sudo chroot /mnt

grub-install /dev/sdX
grub-install --recheck /dev/sdX
update-grub
```

7.完成，重開機