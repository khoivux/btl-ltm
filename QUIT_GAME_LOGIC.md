# 🎮 Logic Quit Game - Tài liệu chi tiết

## 📋 Tổng quan
Tài liệu này mô tả chi tiết logic xử lý khi người chơi thoát giữa trận đấu trong Game Đoán Màu.

---

## 🔧 Các thay đổi đã thực hiện

### 1. **Giao diện (GameView.fxml)**
✅ **Thêm nút "Thoát Game"**
- Vị trí: Góc dưới bên phải (layoutX="950.0" layoutY="600.0")
- Style: Nút màu đỏ gradient với hiệu ứng bóng đổ
- Action: Gọi method `endGame()` trong GameController

```xml
<javafx.scene.control.Button onAction="#endGame" 
    style="-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b); 
           -fx-text-fill: white; 
           -fx-font-size: 16px; 
           -fx-font-weight: bold; 
           -fx-background-radius: 10;" 
    text="Thoát Game">
```

---

### 2. **GameController.java**
✅ **Cập nhật method `endGame()`**

**Chức năng:**
1. Hiển thị dialog xác nhận trước khi thoát
2. Cảnh báo người chơi sẽ thua nếu thoát giữa trận
3. Gửi message `EXIT_GAME` đến server
4. Quay về màn hình chính (MainUI)

```java
@FXML
public void endGame() {
    // Xác nhận trước khi thoát
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Xác nhận thoát");
    confirmAlert.setHeaderText("Bạn có chắc muốn thoát game?");
    confirmAlert.setContentText("Nếu thoát giữa trận, bạn sẽ thua và không nhận điểm!");
    
    // Xử lý kết quả
    if (result == yesBtn) {
        client.sendMessage(new Message(MessageType.EXIT_GAME, null));
        client.showMainUI();
    }
}
```

✅ **Cập nhật method `onGameEnd()`**
- Phát hiện trận đấu kết thúc do quit (award = 0 cho người quit)
- Hiển thị thông báo khác biệt cho trường hợp quit vs thắng bình thường

---

### 3. **GameSession.java (Server)**
✅ **Cải tiến method `endMatch(String usernameQuit)`**

**Logic xử lý:**

#### **Trường hợp có người quit:**
```java
if (usernameQuit != null && !usernameQuit.isEmpty()) {
    if (usernameQuit.equals(getPlayer1Username())) {
        award1 = 0;  // Người quit: 0 điểm
        award2 = 2;  // Người còn lại: 2 điểm (thắng)
        winner = getPlayer2Username();
    } else {
        award1 = 2;
        award2 = 0;
        winner = getPlayer1Username();
    }
}
```

#### **Trường hợp kết thúc bình thường:**
```java
else {
    if (score1 > score2) {
        award1 = 2;
        winner = getPlayer1Username();
    } else if (score2 > score1) {
        award2 = 2;
        winner = getPlayer2Username();
    } else {
        award1 = 1;  // Hòa
        award2 = 1;
    }
}
```

---

### 4. **GameManager.java (Server)**
✅ **Method `handleExit(ClientHandler from)`**
- Nhận yêu cầu EXIT_GAME từ client
- Gọi `endSession()` với username của người quit

✅ **Cải tiến method `endSession()`**

**Các bước xử lý:**

1. **Hủy timer:**
   ```java
   if (info.tickTask != null) info.tickTask.cancel(false);
   ```

2. **Thông báo đối thủ:**
   ```java
   if (usernameQuit != null) {
       Message quitNotification = new Message(MessageType.OPPONENT_QUIT, usernameQuit);
       // Gửi cho người chơi còn lại
   }
   ```

3. **Kết thúc match và tính điểm:**
   ```java
   GameSession.MatchResult mr = info.session.endMatch(usernameQuit);
   broadcast(info, new Message(MessageType.MATCH_RESULT, payload));
   ```

4. **Cập nhật trạng thái về AVAILABLE:**
   ```java
   info.ch1.getClientManager().updateStatus(username1, Status.AVAILABLE);
   info.ch2.getClientManager().updateStatus(username2, Status.AVAILABLE);
   ```

5. **Cleanup session:**
   ```java
   userSessionMap.remove(username1);
   userSessionMap.remove(username2);
   ```

---

### 5. **MessageType.java**
✅ **Thêm message type mới:**
```java
OPPONENT_QUIT("opponent_quit")
```

**Mục đích:** Thông báo cho người chơi còn lại rằng đối thủ đã thoát

---

### 6. **Client.java**
✅ **Thêm handler cho `OPPONENT_QUIT`**

```java
case MessageType.OPPONENT_QUIT:
    String quitterUsername = (String) message.getContent();
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đối thủ đã thoát");
        alert.setContentText("Người chơi " + quitterUsername + " đã thoát khỏi trận đấu.\nBạn thắng mặc định!");
        alert.showAndWait();
    });
    break;
```

---

### 7. **ClientHandler.java (Server)**
✅ **Thêm getter cho ClientManager**
```java
public ClientManager getClientManager() {
    return this.clientManager;
}
```

**Mục đích:** Cho phép GameManager cập nhật trạng thái người chơi thông qua ClientManager

---

## 🎯 Luồng xử lý hoàn chỉnh

### **Kịch bản: Player A quit game**

```
1. Player A nhấn nút "Thoát Game"
   ↓
2. GameController hiển thị dialog xác nhận
   ↓
3. Player A xác nhận thoát
   ↓
4. Client A gửi: Message(EXIT_GAME, null)
   ↓
5. Server (ClientHandler) nhận EXIT_GAME
   ↓
6. GameManager.handleExit(clientA)
   ↓
7. GameManager.endSession(info, "PlayerA")
   ↓
8. GameManager gửi: Message(OPPONENT_QUIT, "PlayerA") → Player B
   ↓
9. Player B nhận thông báo: "PlayerA đã thoát!"
   ↓
10. GameSession.endMatch("PlayerA"):
    - PlayerA: 0 điểm (quit)
    - PlayerB: +2 điểm (thắng)
    ↓
11. Broadcast: Message(MATCH_RESULT, [score1, score2, winner, 0, 2])
    ↓
12. Cập nhật status:
    - PlayerA: AVAILABLE
    - PlayerB: AVAILABLE
    ↓
13. Cleanup session
    ↓
14. Cả 2 player quay về MainUI (Lobby)
```

---

## 📊 Bảng phân chia điểm

| Tình huống | Player 1 | Player 2 | Winner |
|-----------|----------|----------|--------|
| P1 score > P2 score (bình thường) | +2 | +0 | P1 |
| P2 score > P1 score (bình thường) | +0 | +2 | P2 |
| P1 score = P2 score (hòa) | +1 | +1 | null |
| P1 quit | +0 | +2 | P2 |
| P2 quit | +2 | +0 | P1 |

---

## 🔍 Chi tiết Database

**Bảng `detail_matches`:**
```sql
player_id    | match_id | score | is_winner | is_quit
-------------|----------|-------|-----------|--------
1            | 100      | 3     | 1         | 0       <- Player thắng bình thường
2            | 100      | 2     | 0         | 0       <- Player thua bình thường
3            | 101      | 1     | 0         | 1       <- Player quit
4            | 101      | 5     | 1         | 0       <- Player thắng do đối thủ quit
```

---

## ✅ Checklist tính năng

- [x] Thêm nút "Thoát Game" vào giao diện
- [x] Dialog xác nhận trước khi thoát
- [x] Cảnh báo mất điểm khi quit
- [x] Gửi message EXIT_GAME đến server
- [x] Server xử lý logic quit (0 điểm cho người quit)
- [x] Thông báo đối thủ khi có người quit
- [x] Cập nhật trạng thái AVAILABLE sau khi kết thúc
- [x] Hiển thị kết quả đúng (ai quit, ai thắng)
- [x] Lưu thông tin `is_quit` vào database
- [x] Cleanup session và map đúng cách
- [x] Xử lý cả trường hợp disconnect đột ngột

---

## 🧪 Test Cases

### **Test 1: Quit trong lúc chơi**
1. Player A và B bắt đầu trận đấu
2. Player A nhấn "Thoát Game"
3. Xác nhận thoát
4. **Expected:**
   - Player A về MainUI
   - Player B nhận thông báo "Player A đã thoát"
   - Player B nhận +2 điểm
   - Player A nhận 0 điểm
   - Cả 2 có status AVAILABLE

### **Test 2: Hủy quit**
1. Player A nhấn "Thoát Game"
2. Chọn "Ở lại"
3. **Expected:**
   - Dialog đóng
   - Game tiếp tục bình thường

### **Test 3: Quit khi dialog kết quả đang mở**
1. Trận đấu kết thúc bình thường
2. Dialog kết quả hiện ra
3. Player B gửi invite rematch
4. **Expected:**
   - Dialog kết quả đóng
   - Dialog invite hiện ra

---

## 🐛 Known Issues & Solutions

### **Issue 1: Status không cập nhật**
**Giải pháp:** Đã thêm getter `getClientManager()` trong ClientHandler

### **Issue 2: Session không cleanup**
**Giải pháp:** Đã thêm cleanup trong `finally` block của ClientHandler.run()

### **Issue 3: Người chơi còn lại không biết đối thủ quit**
**Giải pháp:** Đã thêm `OPPONENT_QUIT` message type

---

## 📝 Notes

- Người quit luôn nhận 0 điểm
- Người còn lại luôn nhận 2 điểm (thắng mặc định)
- Trạng thái tự động chuyển về AVAILABLE sau khi kết thúc
- Timer sẽ bị hủy ngay lập tức khi có người quit
- Database lưu flag `is_quit = 1` cho người thoát

---

## 🚀 Cách test

### **Server:**
```bash
cd e:\tai lieu mon hoc\ltm\doanmau\btl-ltm
mvn clean compile
java server.RunServer
```

### **Client 1:**
```bash
mvn javafx:run
```

### **Client 2:**
```bash
# Mở terminal mới
mvn javafx:run
```

### **Thao tác test:**
1. Đăng nhập 2 client
2. Client 1 gửi invite cho Client 2
3. Client 2 chấp nhận
4. Trong lúc chơi, Client 1 nhấn "Thoát Game"
5. Kiểm tra kết quả và điểm số

---

## 📞 Support

Nếu có vấn đề, kiểm tra:
1. Server console có log lỗi không
2. Client console có exception không
3. Database có lưu đúng không
4. Status có được cập nhật không

---

**Ngày tạo:** 1 Tháng 11, 2025
**Version:** 1.0
**Author:** GitHub Copilot
