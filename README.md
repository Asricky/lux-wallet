# Lux Wallet

Aplikasi Android untuk memantau keuangan pribadi melalui notifikasi **myBCA, SeaBank, ShopeePay, dan GoPay**. Data diproses di perangkat, dapat digunakan offline, tanpa login rekening atau backend.

Identitas visual memakai dompet hitam dengan aksen emas, tema terang/gelap, ringkasan kekayaan bersih, arus kas, dan ruang belanja harian.

## Download APK

[**Download app-debug-v1.apk**](https://github.com/Asricky/lux-wallet/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v1.apk)

Semua APK tersedia di [folder APK debug](app/build/outputs/apk/debug). Dari GitHub, buka file APK lalu pilih **Download raw file**.

Lokasi di komputer:

```text
lux-wallet\app\build\outputs\apk\debug\
└── app-debug-v1.apk
```

Pilih angka versi terbesar untuk pembaruan terbaru. Nama aplikasi di launcher tetap **Lux Wallet**; nomor versi ada pada nama file APK dan versi paket Android.

## Instalasi dan mulai memakai

1. Unduh APK, buka di Android 10 atau lebih baru, lalu izinkan instalasi dari aplikasi pengunduh jika diminta.
2. Buka Lux Wallet, pilih sumber notifikasi, dan isi saldo awal rekening yang digunakan. Format seperti `1500000` atau `1.500.000` diterima.
3. Izinkan **Akses notifikasi** lewat onboarding atau kartu status di Beranda/Pengaturan.
4. Pastikan notifikasi transaksi dari aplikasi bank/e-wallet juga aktif.
5. Periksa kartu **Pemantauan notifikasi aktif**. Notifikasi transaksi baru yang dikenali akan masuk otomatis.

Saldo merupakan estimasi: saldo awal ditambah transaksi setelah waktu saldo awal. Aplikasi tidak mengambil riwayat bank yang sudah hilang dari notifikasi. Snapshot saldo dari notifikasi disimpan sebagai bukti, bukan mengganti saldo estimasi secara diam-diam.

Jika Android menampilkan **Setelan dibatasi**, buka info aplikasi Lux Wallet, menu titik tiga, lalu **Izinkan setelan dibatasi** bila pilihan itu tersedia. Setelah itu aktifkan akses notifikasi kembali.

## Jika transaksi belum tercatat

- Pastikan rekening sumber sudah dibuat dan sumber terkait aktif pada Pengaturan.
- Gunakan **Sambungkan ulang** pada kartu status. Setelah update APK, buka kembali Lux Wallet.
- Periksa **Lainnya → Perlu ditinjau** untuk transaksi atau format notifikasi yang belum dikenali.
- Pada perangkat yang membatasi aplikasi latar belakang, izinkan aktivitas latar belakang untuk Lux Wallet lewat pengaturan perangkat.
- Aktifkan **Pengaturan → Diagnostik → Mode diagnostik**, lalu buka Notification Lab untuk melihat notifikasi yang diterima.
- Mode penemuan paket hanya mencatat nama paket aplikasi. Jika perlu, masukkan nama paket aplikasi keuangan dan izinkan pemetaannya secara eksplisit; matikan penemuan setelah selesai.

ID SeaBank Indonesia yang digunakan adalah `id.co.bankbkemobile.digitalbank`, sesuai [listing resmi Google Play](https://play.google.com/store/apps/details?id=id.co.bankbkemobile.digitalbank). Gmail dan aplikasi lain tidak diproses tanpa pemetaan eksplisit.

## Build dan penomoran update

Toolchain proyek: Android SDK 34, minSdk 29, Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, JDK 17–21 disarankan. Buat `local.properties` berisi lokasi SDK:

```properties
sdk.dir=D:\\Android\\Sdk
```

Pada Windows, gunakan perintah ini untuk **setiap update yang akan dibagikan**:

```powershell
.\build-update.ps1
# Jika semua dependensi telah tersimpan:
.\build-update.ps1 -Offline
```

Skrip menjalankan unit/integration test, Android Lint, dan build APK. Build pertama menghasilkan `app-debug-v1.apk`, berikutnya `app-debug-v2.apk`, lalu v3 dan seterusnya. Versi terakhir yang berhasil disimpan dalam `version.properties`, sehingga urutan tetap berlanjut setelah folder build dibersihkan. Build yang gagal tidak menaikkan nomor versi. APK lama tidak dihapus oleh skrip.

Untuk membangun ulang versi saat ini tanpa menaikkan nomor:

```powershell
.\gradlew.bat assembleDebug
```

Instal lewat ADB (sesuaikan nomor file):

```powershell
adb install -r app/build/outputs/apk/debug/app-debug-v1.apk
```

Commit `version.properties` dan APK bernomor baru bersama perubahan sumber. Folder build lain, konfigurasi lokal, dan keystore diabaikan Git.

## Pembaruan tanpa kehilangan data

Pertahankan application ID debug `com.luxwallet.app.debug`, sertifikat signing yang sama, dan migrasi Room yang kompatibel. Jangan uninstall aplikasi sebelum memasang pembaruan.

Build debug memakai keystore debug lokal Android. Simpan salinan keystore tersebut secara pribadi jika berpindah komputer; keystore baru tidak dapat memperbarui instalasi yang ditandatangani keystore lama. Keystore dan kata sandi tidak disertakan dalam repo.

Build release opsional memakai `keystore.properties` lokal dengan `storeFile`, `storePassword`, `keyAlias`, dan `keyPassword`, lalu jalankan `assembleRelease`. Paket release `com.luxwallet.app` terpisah dari paket debug.

## Perubahan v1

- Logo dompet hitam/emas, palet terang/gelap, dashboard baru, dan label utama berbahasa Indonesia.
- Perbaikan dropdown rekening, kategori, filter arus kas, dan tema.
- ID SeaBank resmi, pemetaan paket, status listener, pemulihan koneksi, serta pembacaan expanded/inbox notifications.
- Pemrosesan transaksi dan saldo atomik, retry worker, pencegahan pemrosesan ulang, serta perlindungan saldo awal.
- Dua pembelian bernominal sama tetap dicatat; transfer internal hanya dicocokkan dengan tipe transaksi yang sesuai.
- Notifikasi gagal, pending, OTP, dan promosi tidak dijadikan transaksi; format asing ditampilkan untuk ditinjau.
- Dashboard mengikuti perubahan transaksi; nominal tidak dihitung dengan floating point.
- Penyimpanan transaksi manual menampilkan kegagalan dan menunggu sukses sebelum menutup form.
- Mengabaikan transaksi membalik saldo ledger tepat sekali; penyimpanan detail tidak saling menimpa.
- Kunci biometrik menunggu preferensi dimuat dan mengunci kembali saat aplikasi masuk latar belakang.

## Pengujian

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

Suite mencakup sembilan contoh notifikasi, parser nominal, transfer, deduplikasi, perhitungan anggaran, dan integrasi Room termasuk rollback saat penulisan ledger gagal.

Belum diverifikasi pada HP fisik dalam revisi ini. Format notifikasi dapat berubah antar versi aplikasi bank. GoPay tetap ditandai untuk ditinjau karena belum tersedia sampel notifikasi asli yang terkalibrasi. Penggabungan/pemisahan transaksi secara manual belum tersedia.

## Privasi dan cadangan

Pemrosesan inti berjalan lokal. Tidak ada pengiriman isi notifikasi ke server. Database Room berada di penyimpanan privat aplikasi; cadangan file dienkripsi menggunakan Android Keystore. Cadangan tersebut terikat kunci instalasi/perangkat, sehingga **bukan** cadangan portabel untuk reinstall atau pindah perangkat. Ekspor CSV tersedia untuk arsip transaksi.

Notifikasi yang tidak dikenal dipertahankan untuk ditinjau; retensi mentah berlaku pada notifikasi yang sudah diproses. Proteksi tangkapan layar dan kunci biometrik dapat diaktifkan dari Pengaturan.
