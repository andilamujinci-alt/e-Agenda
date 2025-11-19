# Aplikasi Manajemen Surat (e-Agenda)

Aplikasi Android untuk tata kelola agenda surat dengan fitur tracking, disposisi, dan tanda tangan digital.

## 🚀 Fitur

- ✅ **Login dengan Supabase Auth** - Autentikasi aman dengan NIP
- ✅ **Surat Masuk & Keluar** - Manajemen lengkap surat
- ✅ **Tracking Status** - Monitor posisi surat real-time
- ✅ **Disposisi** - Tanda tangan digital langsung di HP
- ✅ **Upload File** - Scan atau upload PDF
- ✅ **Filter & Search** - Cari surat dengan mudah
- ✅ **Role Based Access** - Admin dan User
- ✅ **Dashboard** - Statistik surat hari ini

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) atau lebih baru
- Android SDK 24 atau lebih tinggi
- Akun Supabase

## 🔧 Setup Project

### 1. Clone Repository
```bash
git clone https://github.com/andilamujinci-alt/e-Agenda.git
cd aplikasi-manajemen-surat
```

### 2. Setup Supabase Credentials

1. Copy file template credentials:
```bash
cp app/src/main/java/com/example/suratapp/data/SupabaseConfig.kt.example \
   app/src/main/java/com/example/suratapp/data/SupabaseConfig.kt
```

2. Edit `SupabaseConfig.kt` dan isi dengan credentials Supabase Anda:
```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://xxxxx.supabase.co"
    const val SUPABASE_ANON_KEY = "your_actual_anon_key_here"
}
```

### 3. Setup Database Supabase

Jalankan script SQL yang ada di `/database/setup.sql` di Supabase SQL Editor.

### 4. Sync & Build
```bash
# Sync Gradle
./gradlew build

# Atau di Android Studio: File → Sync Project with Gradle Files
```

### 5. Run Aplikasi

Klik tombol Run (▶️) di Android Studio atau:
```bash
./gradlew installDebug
```

## 📱 Login Credentials

### Admin
- NIP: `199001012020011001`
- Password: (sesuai yang Anda set di Supabase)

### User
- NIP: (NIP pegawai)
- Password: (password pegawai)

## 🗂️ Struktur Project
```
app/
├── src/main/
│   ├── java/com/example/suratapp/
│   │   ├── data/
│   │   │   ├── models/          # Data models
│   │   │   ├── repository/      # Repository layer
│   │   │   ├── SupabaseClient.kt
│   │   │   └── UserPreferences.kt
│   │   ├── ui/
│   │   │   ├── login/           # Login screen
│   │   │   ├── main/            # Dashboard & list
│   │   │   ├── input/           # Input surat
│   │   │   └── detail/          # Detail & tracking
│   │   └── utils/               # Helper classes
│   └── res/
│       ├── layout/              # XML layouts
│       ├── values/              # Colors, strings, themes
│       └── values-night/        # Dark mode resources
└── build.gradle.kts
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM
- **Backend**: Supabase (PostgreSQL + Auth + Storage)
- **UI**: Material Design 3
- **Async**: Coroutines + Flow
- **Image Loading**: Coil
- **Camera**: CameraX
- **Signature**: Signature Pad
- **Local Storage**: DataStore

## 📝 Database Schema

### Table: `pegawai`
- id, nip, nama, role, email, created_at

### Table: `surat_masuk`
- id, pengirim, nomor_surat, tanggal_surat, nomor_agenda, tanggal_diterima, perihal, status_surat, file_url, nama_penerima, tanda_tangan, timestamp_disposisi

### Table: `surat_keluar`
- (sama dengan surat_masuk)

## 🤝 Contributing

1. Fork repository
2. Buat branch baru (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## 📄 License

This project is private and proprietary.

## 👨‍💻 Author

**Nama Anda**
- GitHub: [@andilamujinci-alt](https://github.com/andilamujinci-alt)
- Email: andilamujinci@gmail.com

## 🙏 Acknowledgments

- [Supabase](https://supabase.com/) - Backend as a Service
- [Material Design](https://material.io/) - Design System
- [Android Developers](https://developer.android.com/) - Documentation
