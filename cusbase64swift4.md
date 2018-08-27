## Swift 4 自定義Base64

### base64 定義：

轉換的時候，將3位元組的資料，先後放入一個24位元的緩衝區中，先來的位元組占高位。資料不足3位元組的話，於緩衝區中剩下的位元用0補足。每次取出6位元（因為 $2^6 = 64$），按照其值選擇```ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/```中的字元作為編碼後的輸出，直到全部輸入資料轉換完成。

若原資料長度不是3的倍數時且剩下1個輸入資料，則在編碼結果後加2個=；若剩下2個輸入資料，則在編碼結果後加1個=。

### 思路：
用Uint8 做 binary operation </br>
因爲swift沒有bit形態 </br>
所以只能用 UInt8 </br>

### 編碼：
utf8編碼的文字長度爲8個bit </br>
base64一組長度爲6個bit </br>
故最小公倍數爲24 </br>
每三個utf8文字爲一個循環 </br>
所以有三種不同組法：
```javascript
        for uint in data {
            t = t + 1
            t = t % 3
            switch t {
            case 0:
                int = Int(uint >> 2)
                char = keyStr[int!]
                //print("bbbb uint=\(uint) , int=\(int) , char=\(char)")
                res.append(char!)
            case 1:
                int = Int(((lastChar! << 6) >> 2) | (uint >> 4))
                char = keyStr[int!]
                //print("bbbb uint=\(uint) , int=\(int) , char=\(char)")
                res.append(char!)
            case 2:
                int = Int(((lastChar! << 4) >> 2) | (uint >> 6))
                char = keyStr[int!]
                //print("bbbb uint=\(uint) , int=\(int) , char=\(char)")
                res.append(char!)
                int = Int((uint << 2) >> 2)
                char = keyStr[int!]
                //print("bbbb uint=\(uint) , int=\(int) , char=\(char)")
                res.append(char!)
            default:
                break
            }
            lastChar = uint
        }
```  
然後再根據你的餘數來補0：
``` javascript

        switch t {
        case 0:
            int = Int((lastChar! << 6) >> 2)
            char = keyStr[int!]
            //print("bbbb uint=\(lastChar) , int=\(int) , char=\(char)")
            res.append(char!)
            res.append(keyStr.last!)
            res.append(keyStr.last!)
        case 1:
            int = Int((lastChar! << 4) >> 2)
            char = keyStr[int!]
            //print("bbbb uint=\(lastChar) , int=\(int) , char=\(char)")
            res.append(char!)
            res.append(keyStr.last!)
        default:
            break
        }
```

註：keyStr爲base64編譯碼,長度爲65,最後一碼爲補充字元。
data爲string的[UInt8]。
t的初始值爲-1。

### 解碼：
依照編碼的思路反向操作 </br>
每四個base64文字爲一個循環 </br>
所以有四種不同組法：
```javascript

        for char in self {
            t = t + 1
            t = t % 4
            let uintr = UInt8(keyStr.indexOf(char)) << 2
            switch t {
            case 0:
                break
            case 1:
                let uint = uintl! | uintr >> 6
                bytes.append(uint)
                //print("bbbb uintL=\(uintl) , uintR = \(uintr) , uint = \(uint)")
            case 2:
                if keyStr.last! == char {
                    break
                }
                let uint = uintl! << 2 | uintr >> 4
                bytes.append(uint)
                //print("bbbb uintL=\(uintl) , uintR = \(uintr) , uint = \(uint)")
            case 3:
                if keyStr.last! == char {
                    break
                }
                let uint = uintl! << 4 | uintr >> 2
                bytes.append(uint)
                //print("bbbb uintL=\(uintl) , uintR = \(uintr) , uint = \(uint)")
            default:
                break
            }
            uintl = uintr
        }
```
因爲UInt8是8個bit一組，所以在計算的時候要先shift </br>
例如：

<table class="wikitable">

<tbody><tr>
<th scope="row">文字
</th>
<td colspan="8" align="center"><b>M</b>
</td>
<td colspan="8" align="center"><b>a</b>
</td>
<td colspan="8" align="center"><b>n</b>
</td></tr>
<tr>
<th scope="row">ASCII編碼
</th>
<td colspan="8" align="center">77
</td>
<td colspan="8" align="center">97
</td>
<td colspan="8" align="center">110
</td></tr>
<tr>
<th scope="row">二進位位
</th>
<td>0</td>
<td>1</td>
<td>0</td>
<td>0</td>
<td>1</td>
<td>1</td>
<td>0</td>
<td>1</td>
<td>0</td>
<td>1</td>
<td>1</td>
<td>0</td>
<td>0</td>
<td>0</td>
<td>0</td>
<td>1</td>
<td>0</td>
<td>1</td>
<td>1</td>
<td>0</td>
<td>1</td>
<td>1</td>
<td>1</td>
<td>0
</td></tr>
<tr>
<th scope="row">索引
</th>
<td colspan="6" align="center">19
</td>
<td colspan="6" align="center">22
</td>
<td colspan="6" align="center">5
</td>
<td colspan="6" align="center">46
</td></tr>
<tr>
<th scope="row">Base64編碼
</th>
<td colspan="6" align="center"><b>T</b>
</td>
<td colspan="6" align="center"><b>W</b>
</td>
<td colspan="6" align="center"><b>F</b>
</td>
<td colspan="6" align="center"><b>u</b>
</td></tr></tbody></table>

上表中的Base64編碼：T(19) 取得的UInt8爲```00010011```
在進行操作前要先將其左移2個bit,```01001100``` </br>
下一個數字：W(22)也一樣```00010110 => 01011000```
然後
```
01011000 >> 6 => 01000000

  01001100
+ 00000001
--------------
  01001101

  01001101 => M
```
相較於編碼，解碼不需要去處理補充字，直接略過即可。
