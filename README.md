# 🚀 ZIVPN Manager by YINN STORE

> **ZIVPN Manager** adalah aplikasi Android modern untuk mengelola server VPN / VPS API langsung dari smartphone dengan tampilan clean, ringan, dan real-time.  
> Dibuat menggunakan **Kotlin + Jetpack Compose**, aplikasi ini memudahkan admin mengatur server, memantau status, mengelola user, serta melakukan action penting tanpa harus buka terminal atau login SSH manual.

---

## ✨ Tentang Project

**ZIVPN Manager** dikembangkan oleh **YINN STORE** sebagai solusi mobile untuk mempermudah pengelolaan server berbasis API.

Aplikasi ini cocok untuk:
- **Admin VPN**
- **Pengelola VPS**
- **Seller / Reseller layanan VPN**
- **User internal** yang butuh monitoring server secara cepat dan praktis

Dengan aplikasi ini, semua proses penting bisa dilakukan langsung dari Android, seperti:
- tambah server
- test koneksi server
- lihat user aktif
- buat akun baru
- buat trial
- renew akun
- hapus akun
- trigger pengecekan expired
- monitor ping server secara live

---

## 🔥 Fitur Utama

### ⚡ Real-Time Monitoring
- Monitoring server secara **langsung**
- Update **ping real-time**
- Indikator status:
  - **Hijau** = cepat
  - **Kuning** = sedang
  - **Merah** = lambat / buruk
- Grafik **naik turun ping** mengikuti respon server
- Informasi sistem server ditampilkan langsung di dashboard

### 🖥️ Server Management
- Tambah server baru
- Edit server
- Hapus server
- Ganti server aktif
- Test koneksi server
- Simpan banyak server dalam aplikasi

### 👤 User Management
- Buat akun user baru
- Buat akun trial
- Renew akun
- Hapus akun
- Cari user dengan kolom pencarian
- Refresh data user cepat

### 📊 Dashboard Interaktif
- Total user
- Status server
- Ping server
- Grafik performa ping
- System info
- Quick actions

### 🎨 Modern UI
- Dibangun dengan **Jetpack Compose**
- UI clean, rounded, modern
- Dark / Light theme
- Toast notification modern
- Pull to refresh
- Card status dengan efek glow / neon

---

## 📱 Tampilan Halaman

### **Dashboard**
Halaman utama untuk melihat:
- status server aktif
- total user
- ping real-time
- grafik ping
- info sistem server
- quick action

### **Users**
Halaman untuk:
- melihat seluruh daftar user
- mencari user
- renew user
- delete user

### **Servers**
Halaman khusus untuk:
- melihat semua server yang disimpan
- pilih server aktif
- edit server
- hapus server

### **Settings**
Halaman untuk:
- ganti tema dark / light
- melihat info aplikasi

---

## 🧠 Keunggulan Aplikasi

- **Ringan** dan cepat dijalankan
- UI modern dan nyaman dipakai
- Tidak perlu buka terminal
- Semua action penting tersedia di satu aplikasi
- Cocok untuk operasional harian admin
- Praktis untuk monitoring kondisi server kapan saja

---

## 🛠️ Teknologi yang Digunakan

Project ini dibuat menggunakan stack berikut:

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **MVVM Architecture**
- **Coroutines**
- **Android ViewModel**
- **State Management Compose**
- **GitHub Actions** untuk build APK

---

## 🏗️ Struktur Project

```bash
app/
├── src/main/java/com/gptnmanager/
│   ├── data/
│   │   ├── ApiService.kt
│   │   ├── Models.kt
│   │   ├── ServerStorage.kt
│   │   └── ...
│   │
│   ├── ui/
│   │   ├── GPTNApp.kt
│   │   ├── theme/
│   │   └── ...
│   │
│   ├── MainActivity.kt
│   └── MainViewModel.kt
│
├── src/main/res/
│   ├── values/
│   ├── xml/
│   └── ...
│
└── build.gradle.kts