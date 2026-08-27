# CareSlot Backend API

CareSlot Backend là dịch vụ API chính cho nền tảng đặt lịch khám bệnh trực tuyến **CareSlot**, cung cấp các tính năng quản lý phòng khám, bác sĩ, khung giờ khám (slots), đặt lịch khám, thanh toán và thông báo tự động.

---

## 🚀 Công Nghệ Sử Dụng (Tech Stack)

- **Language & Framework**: Java 21, Spring Boot 4.1.1 (Spring WebMVC, Spring Data JPA, Spring Validation)
- **Database**: MySQL 8.x
- **Database Migration**: Flyway
- **Caching / Slot Holding**: Spring Data Redis
- **Security & Authentication**: JWT (JSON Web Token), Role-Based Access Control (RBAC)
- **Notification**: Spring Boot Mail (SMTP / Gmail)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Utilities**: Lombok, Maven Compiler Plugin

---

## 🛠️ Yêu Cầu Tiền Đề (Prerequisites)

Trước khi khởi chạy dự án, hãy đảm bảo hệ thống của bạn đã cài đặt các công cụ sau:
- **JDK 21** trở lên
- **Apache Maven 3.8+** (hoặc sử dụng `mvnw` / `mvnw.cmd` đi kèm)
- **MySQL 8.0+**
- **Redis Server** (chạy tại `localhost:6379`)

---

## ⚙️ Cấu Hình Hệ Thống

Cấu hình mặc định nằm trong file `src/main/resources/application.yaml`. 

### 1. Cơ sở dữ liệu MySQL
Tạo cơ sở dữ liệu MySQL có tên `careslot_db`:
```sql
CREATE DATABASE careslot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Biến Môi Trường (Environment Variables)
Bạn có thể thiết lập các biến môi trường hoặc sử dụng file cấu hình cá nhân `application-local.yaml` (đã được bao gồm trong `.gitignore`):

| Biến môi trường | Mô tả | Mặc định |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | Chuỗi kết nối JDBC MySQL | `jdbc:mysql://localhost:3306/careslot_db` |
| `SPRING_DATASOURCE_USERNAME` | Username MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password MySQL | `1234` |
| `SPRING_REDIS_HOST` | Địa chỉ Host Redis | `localhost` |
| `SPRING_REDIS_PORT` | Cổng kết nối Redis | `6379` |
| `MAIL_USERNAME` | Email gửi thông báo | `your-email@gmail.com` |
| `MAIL_PASSWORD` | App Password của Gmail | `your-app-password` |
| `JWT_SECRET` | Khóa bí mật cho JWT token | *(Chuỗi Hex 256-bit mặc định)* |

---

## 📦 Hướng Dẫn Chạy Dự Án

### 1. Clone và cài đặt các phụ thuộc
```bash
git clone <repository-url>
cd BE/care-slot
```

### 2. Kiểm tra & Build dự án
```bash
# Trên Windows
.\mvnw.cmd clean package -DskipTests

# Trên Linux/macOS
./mvnw clean package -DskipTests
```

### 3. Khởi chạy ứng dụng
```bash
# Chạy trực tiếp qua Maven Wrapper
.\mvnw.cmd spring-boot:run

# Hoặc chạy file JAR sau khi build
java -jar target/care-slot-0.0.1-SNAPSHOT.jar
```

Dịch vụ Backend sẽ chạy tại địa chỉ: `http://localhost:8080/api/v1`

---

## 📑 Tài Liệu API (Swagger UI)

Khi dự án đã khởi chạy thành công, truy cập tài liệu API trực quan tại:
- **Swagger UI**: [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api/v1/v3/api-docs](http://localhost:8080/api/v1/v3/api-docs)

---

## 📂 Cấu Trúc Thư Mục

```text
care-slot/
├── src/
│   ├── main/
│   │   ├── java/com/org/care_slot/
│   │   │   ├── config/          # Cấu hình Security, Swagger, Redis, Mail, v.v.
│   │   │   ├── controller/      # REST API Controllers
│   │   │   ├── dto/             # Data Transfer Objects (Request/Response)
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── enums/           # Core Enums (Role, Slot Status, Appointment Status)
│   │   │   ├── exception/       # Global Exception Handler
│   │   │   ├── repository/     # Spring Data JPA Repositories
│   │   │   ├── service/         # Business Logic & Service Interfaces
│   │   │   └── CareSlotApplication.java
│   │   └── resources/
│   │       ├── application.yaml # Config ứng dụng
│   │       └── db/migration/    # File Flyway SQL Migration (V1__init_schema.sql)
│   └── test/                    # Unit & Integration Tests
├── pom.xml                      # Quản lý phụ thuộc Maven
└── README.md
```

---

## 🔑 Các Chức Năng Chính (Core Modules)

1. **Authentication & User Management**: Đăng ký, đăng nhập, phân quyền 3 vai trò (`ROLE_ADMIN`, `ROLE_CLINIC`, `ROLE_PATIENT`).
2. **Quản lý Phòng khám & Bác sĩ**: Quản lý hồ sơ phòng khám, chuyên khoa, lịch làm việc của bác sĩ.
3. **Quản lý Khung giờ & Đặt lịch (Slot & Appointment)**:
   - Tạo slot tự động theo ca khám.
   - Giữ slot tạm thời (Hold slot) phòng tránh đặt trùng lịch.
   - Tạo cuộc hẹn và chuyển trạng thái lịch khám (`PENDING_PAYMENT` -> `CONFIRMED` -> `CHECKED_IN`).
4. **Thanh toán (Payment Gateway)**: Tích hợp cổng thanh toán trực tuyến và ghi nhận lịch sử giao dịch.
5. **Thông báo qua Email**: Gửi email xác nhận đặt lịch thành công và nhắc nhở lịch khám.
