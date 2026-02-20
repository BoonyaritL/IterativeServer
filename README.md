# Network Screen Monitor: Functional Programming on Network

โปรเจคนี้คือระบบ **Screen Monitoring** ที่พัฒนาด้วยภาษา Java โดยเน้นการประยุกต์ใช้แนวคิด **Functional Programming (FP)** ในสภาพแวดล้อมการทำงานบนเครือข่าย (Network) เพื่อแสดงให้เห็นว่าเราสามารถเขียนโค้ดที่จัดการกับ Side-effects เช่น I/O และ Network สื่อสารได้อย่างสะอาดและเป็นระเบียบ

## 🚀 ฟีเจอร์หลัก
- **Screen Capture Client**: จับภาพหน้าจอและส่งไปยัง Server ผ่าน Socket แบบวนลูป
- **Iterative Server**: รับการเชื่อมต่อจาก Client ทีละตัว และแสดงภาพหน้าจอที่ได้รับในรูปแบบ GUI
- **Functional Architecture**: ใช้รูปแบบการเขียนโปรแกรมเชิงฟังก์ชันเกือบทั้งหมดในส่วนของ Logic

## 🧩 แนวคิด Functional Programming ที่ใช้

### 1. **Try Monad & Optional Handling**
เราใช้คลาส `Try<T>` (ที่สร้างขึ้นเอง) และ `Optional<T>` เพื่อจัดการกับ Exception และค่าสมมติ (Null) แทนการใช้ `try-catch` block แบบเดิม ทำให้โค้ดไหลลื่นเป็น Pipeline:
```java
captureScreen()
    .flatMap(ScreenCaptureClient::sendToServer)
    .ifPresent(System.out::println);
```

### 2. **Higher-Order Functions & Purity**
มีการใช้ฟังก์ชันที่รับฟังก์ชันอื่นเป็นพารามิเตอร์ เช่น `uncheck` เพื่อแปลง Checked Exception ใน Java ให้ทำงานร่วมกับ Functional Interface ได้อย่างราบรื่น ช่วยให้แยก Logic ออกจาก Error Handling:
```java
private static <T> Optional<T> uncheck(CheckedSupplier<T> supplier) {
    try {
        return Optional.of(supplier.get());
    } catch (Exception e) {
        handleException().accept(e);
        return Optional.empty();
    }
}
```

### 3. **Declarative Pipeline (Data Flow)**
โค้ดเน้นการบอก "จะทำอะไร" (What to do) มากกว่า "ทำอย่างไร" (How to do) ผ่านการใช้ Method References และ Stream-like processing:
```java
uncheck(() -> socket.getInputStream())
    .map(IterativeServer::readImageFromStream)
    .ifPresent(IterativeServer::displayImage);
```

### 4. **Tail Recursion Concept**
ในส่วนของ Main Loop ของทั้ง Client และ Server เราใช้แนวคิดของ Tail Recursion (จำลองผ่านการเรียกฟังก์ชันซ้ำในจุดสุดท้าย) เพื่อรักษาความต่อเนื่องของการทำงานแทนการใช้ `while(true)` แบบ Imperative:
```java
private static void captureAndSendLoop() {
    // ... logic ...
    captureAndSendLoop(); // Recursive call
}
```

### 5. **Side-effect Management**
จำกัด Side-effects (เช่น I/O, Network, GUI Update) ไว้ในฟังก์ชันเฉพาะส่วน และพยายามทำให้ฟังก์ชันประมวลผล (เช่น `scaleImage`) เป็น **Pure Function** ให้มากที่สุด

## 🛠️ โครงสร้างไฟล์
- `IterativeServer.java`: ตัวรับข้อมูลและแสดงผล GUI
- `ScreenCaptureClient.java`: ตัวจับภาพหน้าจอและส่งข้อมูล
- `Try.java`: Custom Implementation ของ Try Monad สำหรับจัดการ Error ในรูปแบบ Functional

## 💻 วิธีการรัน
1. รัน `IterativeServer.java` เพื่อเริ่มรอรับการเชื่อมต่อ
2. รัน `ScreenCaptureClient.java` เพื่อเริ่มส่งภาพหน้าจอไปยัง Server

---
*โปรเจคนี้สร้างขึ้นเพื่อการศึกษาการเขียนโปรแกรมเชิงฟังก์ชันบน Java สำหรับงานจัดการเครือข่ายและ I/O*
