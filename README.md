# Lux Wallet

Aplikasi Android untuk memantau keuangan pribadi melalui notifikasi **myBCA, SeaBank, ShopeePay, dan GoPay**. Data diproses di perangkat, dapat digunakan offline, tanpa login rekening atau backend.

Identitas visual memakai dompet hitam dengan aksen emas, tema terang/gelap, ringkasan kekayaan bersih, arus kas, dan ruang belanja harian.

## Download APK

[**Download app-debug-v2.apk**](https://github.com/Asricky/lux-wallet/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v2.apk)

Semua APK tersedia di [folder APK debug](app/build/outputs/apk/debug). Dari GitHub, buka file APK lalu pilih **Download raw file**.

Lokasi di komputer:

```text
lux-wallet\app\build\outputs\apk\debug\
└── app-debug-v2.apk
```

Versi terbaru: **v2 (1.0.2)**. [APK v1](app/build/outputs/apk/debug/app-debug-v1.apk) tetap tersedia untuk arsip.

Pilih angka versi terbesar untuk pembaruan terbaru. Nama aplikasi di launcher tetap **Lux Wallet**; nomor versi ada pada nama file APK dan versi paket Android.

## Instalasi dan mulai memakai

1. Unduh APK, buka di Android 10 atau lebih baru, lalu izinkan instalasi dari aplikasi pengunduh jika diminta.
2. Buka Lux Wallet, pilih sumber notifikasi, dan isi saldo awal rekening yang digunakan. Format seperti `1500000` atau `1.500.000` diterima.
3. Izinkan **Akses notifikasi** lewat onboarding atau ikon lonceng kecil di Beranda atau halaman pengaturan notifikasi.
4. Pastikan notifikasi transaksi dari aplikasi bank/e-wallet juga aktif.
5. Periksa **ikon lonceng berkedip** di kanan atas Beranda: titik hijau berarti pemantauan tersambung; merah berarti belum aktif atau ada kendala. Ketuk untuk membuka pengaturan notifikasi.

Saldo merupakan estimasi: saldo awal ditambah transaksi setelah waktu saldo awal. Aplikasi tidak mengambil riwayat bank yang sudah hilang dari notifikasi. Snapshot saldo dari notifikasi disimpan sebagai bukti, bukan mengganti saldo estimasi secara diam-diam.

Jika Android menampilkan **Setelan dibatasi**, buka info aplikasi Lux Wallet, menu titik tiga, lalu **Izinkan setelan dibatasi** bila pilihan itu tersedia. Setelah itu aktifkan akses notifikasi kembali.

## Jika transaksi belum tercatat

- Pastikan rekening sumber sudah dibuat dan sumber terkait aktif pada Pengaturan.
- Ketuk lonceng Beranda, lalu gunakan **Sambungkan ulang** di halaman notifikasi. Setelah update APK, buka kembali Lux Wallet.
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
adb install -r app/build/outputs/apk/debug/app-debug-v2.apk
```

Commit `version.properties` dan APK bernomor baru bersama perubahan sumber. Folder build lain, konfigurasi lokal, dan keystore diabaikan Git.

## Pembaruan tanpa kehilangan data

Pertahankan application ID debug `com.luxwallet.app.debug`, sertifikat signing yang sama, dan migrasi Room yang kompatibel. Jangan uninstall aplikasi sebelum memasang pembaruan.

Build debug memakai keystore debug lokal Android. Simpan salinan keystore tersebut secara pribadi jika berpindah komputer; keystore baru tidak dapat memperbarui instalasi yang ditandatangani keystore lama. Keystore dan kata sandi tidak disertakan dalam repo.

Build release opsional memakai `keystore.properties` lokal dengan `storeFile`, `storePassword`, `keyAlias`, dan `keyPassword`, lalu jalankan `assembleRelease`. Paket release `com.luxwallet.app` terpisah dari paket debug.

## Perubahan v2

- Beranda yang ringkas: indikator notifikasi kecil, kartu kekayaan, pintasan, ruang belanja, riwayat transaksi, dan sebaran aset.
- Tombol mata menyembunyikan nominal serta proporsi sebaran aset; pilihan tersimpan dan digunakan juga pada menu Aset.
- **Pengaturan → Tampilan** memiliki pilihan **Light**, **Dark**, dan **Sistem**.
- Pemilih bulan, rekening, kategori, dan jenis transaksi berupa tombol membulat yang membuka daftar dari bawah layar.
- **Catat transaksi** memakai dua langkah: isi detail → periksa ringkasan → simpan. Pilih **Pindah saldo / top-up sendiri** untuk memindahkan uang antar akun milikmu.
- **Aset → ketuk aset/rekening** untuk mengubah nominal. Koreksi rekening menjadi transaksi penyesuaian saldo, tanpa menambah pemasukan/pengeluaran. Aset investasi/lainnya dapat ditambah manual.
- **Kalkulator keuangan** tersedia dari Beranda/Lainnya: aritmetika, masukkan kekayaan bersih, dan simulasi pertumbuhan dengan setoran bulanan.
- Info ruang belanja menjelaskan pendapatan, kewajiban, target tabungan, cadangan transportasi, belanja sebelumnya, serta sisa hari. Bar menampilkan persentase penggunaan hari ini.
- Database v1 dimigrasikan ke v2 tanpa menghapus rekening atau transaksi.

### Duplikasi myBCA Rp3

Pembaruan notifikasi dengan identitas kejadian yang sama tidak membuat transaksi baru. Dua ringkasan myBCA identik yang tiba dalam 30 detik tetapi identitasnya belum pasti masuk **Perlu ditinjau** sebagai **calon duplikat**, tanpa menggandakan saldo/pengeluaran.

Setelah upgrade, aplikasi juga memeriksa pasangan notifikasi lama yang masih tersedia. Catatan yang dicurigai ganda ditahan untuk ditinjau dan dampak saldo gandanya dibalik tepat sekali.

- Jika benar satu pembayaran: buka catatan → **Abaikan duplikat**.
- Jika memang dua pembayaran berbeda: pilih **Ini transaksi berbeda · hitung nominal**.
- Jika isi notifikasi lama sudah dihapus pada versi sebelumnya, perbaikan otomatis tidak dapat mengenalinya. Buka salah satu catatan ganda lalu abaikan secara manual.

Dua pembayaran sungguhan yang ringkasannya persis sama dalam waktu singkat dapat perlu konfirmasi. Nominal saja tidak dipakai untuk menghapus transaksi.

### Transportasi dan target Rp2,5 juta

Rencana awal: **Rp6.000 per hari kerja (Senin–Jumat)**, **Rp1 juta tabungan modal bisnis**, dan **Rp1,5 juta investasi per bulan**. Semua bisa diubah melalui **Beranda → Anggaran**. Target ini dicadangkan dalam perhitungan, tidak otomatis memindahkan uang.

1. Buat rekening/e-wallet milikmu.
2. Saat top-up berkala, catat sebagai **Pindah saldo / top-up sendiri**, berapa pun nominal dan waktunya.
3. Saat ongkos benar-benar dipakai, pilih kategori **Transportasi rutin**, termasuk melalui Detail Transaksi untuk catatan otomatis.
4. Cadangan transportasi = Rp6.000 × jumlah hari perjalanan bulan tersebut. Jika realisasi lebih besar, cadangan mengikuti realisasi agar anggaran tidak terlalu optimistis.
5. Biaya kategori Transportasi rutin tetap terlihat di arus kas, tetapi dikeluarkan dari pemakaian uang belanja bebas. Nominal Rp6.000 tidak otomatis dianggap transportasi.

Ruang belanja dihitung dari pendapatan dikurangi kewajiban/cadangan lain, target tabungan, investasi, transportasi, dan belanja bebas sebelum hari ini; sisanya dibagi sisa hari termasuk hari ini. Belanja hari ini dikurangi satu kali dari alokasi tersebut.

Jangan masukkan cadangan transportasi kembali ke kolom kewajiban tetap. Top-up otomatis dengan tujuan yang belum pasti tetap perlu ditinjau; jadwal isi ulang tidak dianggap sebagai jadwal belanja.

### Pilihan investasi

Lihat [panduan investasi dan pemisahan dana bisnis](docs/investasi.md). Tidak ada satu produk yang pasti paling baik. Saran awal bergantung pada kecukupan dana darurat, kapan uang diperlukan, dan toleransi penurunan nilai. Angka hasil di kalkulator adalah asumsi simulasi, bukan penawaran atau janji keuntungan.

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

Suite mencakup sembilan contoh notifikasi, pembayaran myBCA Rp3, calon duplikat dan konfirmasinya, migrasi Room v1→v2, koreksi saldo, anggaran transportasi/tabungan, kalkulator, rollback ledger, serta interaksi pemilih pada tema terang dan gelap.

Belum diverifikasi pada HP fisik dalam revisi ini. Format notifikasi dapat berubah antar versi aplikasi bank. GoPay tetap ditandai untuk ditinjau karena belum tersedia sampel notifikasi asli yang terkalibrasi. Penggabungan/pemisahan transaksi secara manual belum tersedia.

## Privasi dan cadangan

Pemrosesan inti berjalan lokal. Tidak ada pengiriman isi notifikasi ke server. Database Room berada di penyimpanan privat aplikasi; cadangan file dienkripsi menggunakan Android Keystore. Cadangan tersebut terikat kunci instalasi/perangkat, sehingga **bukan** cadangan portabel untuk reinstall atau pindah perangkat. Ekspor CSV tersedia untuk arsip transaksi.

Notifikasi yang tidak dikenal dipertahankan untuk ditinjau. Retensi mengosongkan isi mentah notifikasi yang sudah diproses; hash dan metadata identitas tetap disimpan untuk mencegah kiriman ulang dihitung kembali. Proteksi tangkapan layar dan kunci biometrik dapat diaktifkan dari Pengaturan.
