# Lux Wallet

Aplikasi Android untuk memantau keuangan pribadi melalui notifikasi **myBCA, SeaBank, ShopeePay, dan GoPay**. Data diproses di perangkat, dapat digunakan offline, tanpa login rekening atau backend.

Kenali **Lumi**, karakter teal dengan aksen emas, pendamping keuanganmu. Lux Wallet menyediakan tema terang/gelap, kalender pemasukan/pengeluaran, rencana saldo sampai gajian, serta saran dan pengingat lokal.

<img src="app/src/main/res/drawable-nodpi/lumi_launcher.png" width="120" alt="Lumi, maskot Lux Wallet" />

Spesifikasi produk: [PRD.md](PRD.md) · Panduan tampilan: [DESIGN.md](DESIGN.md).

<!-- LATEST_RELEASE_START -->
## Download APK terbaru

[**Download app-debug-v4.apk**](https://github.com/Asricky/lux-wallet/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v4.apk)

Versi terbaru: **v4 (1.0.4)**. Android 10 atau lebih baru.

Lokasi file: `lux-wallet\app\build\outputs\apk\debug\app-debug-v4.apk`.

Semua versi sebelumnya tersedia di [folder APK debug](app/build/outputs/apk/debug). Nama launcher tetap **Lux Wallet**.

SHA-256: `6EB0592EDC43299E1229918D78B9476263119D97C3BFC63B93CE3C0A5FDEE266`

### Pembaruan v4

- Tampilan teal/mint, sapaan nama dari profil, sembilan ekspresi Lumi, dan ikon launcher baru dari referensi Lumi.
- Tab menyimpan posisi/pilihan tanpa fade dan tanpa memulihkan formulir lama; CTA memakai chevron yang konsisten.
- Nominal tersembunyi memakai `********` pada saldo, aset, arus kas, transaksi, target, dan hasil simulasi.
- Kalender menampilkan arus kas harian bertanda, misalnya +100K atau −1.25M, terpisah dari hasil terhadap budget.
- Konfirmasi transaksi Android dengan antrean persisten, filter per jenis, tautan detail, snackbar, serta peringatan budget.
- Migrasi Room v4 mempertahankan ledger dan saldo; transfer memakai satu konfirmasi. Pembaruan notifikasi setelah transfer digabung tetap melekat pada transaksi yang sama.
- README diperbarui otomatis oleh proses rilis: link unduh terbaru, versi, checksum APK, dan catatan perubahan dari changelog.
<!-- LATEST_RELEASE_END -->

## Instalasi dan mulai memakai

1. Unduh APK, buka di Android 10 atau lebih baru, lalu izinkan instalasi dari aplikasi pengunduh jika diminta.
2. Ikuti empat langkah perkenalan, sumber notifikasi, saldo rekening, dan target bulanan. Ketik `1500000`; tampilan menjadi `1.500.000` otomatis. Titik yang ditempel harus memakai kelompok ribuan Indonesia.
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
$latest = Get-ChildItem app/build/outputs/apk/debug/app-debug-v*.apk |
    Sort-Object { [int]($_.BaseName -replace 'app-debug-v', '') } -Descending |
    Select-Object -First 1
adb install -r $latest.FullName
```

Setiap pembaruan wajib menyertakan catatan versi baru di `CHANGELOG.md`. `build-update.ps1` menjalankan pengujian, lint, dan build, lalu otomatis memperbarui bagian rilis README: tautan unduh langsung, lokasi file, versi, SHA-256, serta catatan perubahan. Tidak perlu meminta pemilik mengingatkan pembaruan README lagi. Preview tidak mengubah file; kegagalan build tidak menerbitkan link versi baru.

Commit `README.md`, `CHANGELOG.md`, `version.properties`, dan APK bernomor baru bersama perubahan sumber. Folder build lain, konfigurasi lokal, dan keystore diabaikan Git.

## Pembaruan tanpa kehilangan data

Pertahankan application ID debug `com.luxwallet.app.debug`, sertifikat signing yang sama, dan migrasi Room yang kompatibel. Jangan uninstall aplikasi sebelum memasang pembaruan.

Build debug memakai keystore debug lokal Android. Simpan salinan keystore tersebut secara pribadi jika berpindah komputer; keystore baru tidak dapat memperbarui instalasi yang ditandatangani keystore lama. Keystore dan kata sandi tidak disertakan dalam repo.

Build release opsional memakai `keystore.properties` lokal dengan `storeFile`, `storePassword`, `keyAlias`, dan `keyPassword`, lalu jalankan `assembleRelease`. Paket release `com.luxwallet.app` terpisah dari paket debug.

## Konfirmasi transaksi dan profil

Atur nama panggilan di **Pengaturan → Profil**. Beranda menampilkan `Hi, Nama 👋`, atau `Hi there 👋` bila belum diisi. Tombol mata menyembunyikan nominal secara konsisten; input yang sedang diedit tetap terlihat agar bisa diperiksa.

Buka **Pengaturan → Notifikasi → Konfirmasi transaksi & peringatan budget**, lalu izinkan notifikasi Android. Pilihan transaksi tercatat, pemasukan, pengeluaran, transfer, perlu ditinjau, dan peringatan budget aktif secara default. Izin mengirim notifikasi berbeda dari akses membaca notifikasi bank. Mematikan konfirmasi tidak mematikan pencatatan.

Konfirmasi memakai channel **Lux Wallet Transactions**. Notifikasi biasa dikirim setelah pencatatan tersimpan. Transfer otomatis menunggu pasangan selama jendela pencocokan 30 menit sejak dicatat; pasangan yang cocok mempercepat konfirmasi. Batas baterai Android bisa menunda pengiriman. Jika pasangan baru tiba setelah konfirmasi terkirim, ledger tetap digabung tanpa mengirim konfirmasi kedua. Ketuk notifikasi untuk membuka detail setelah melewati kunci aplikasi. Mode hide tidak mengirim nominal; layar kunci selalu memakai pesan umum. Pemberitahuan yang terlewat karena izin/filter mati tidak diputar ulang.

Lumi menyesuaikan ekspresi dengan penggunaan budget, transaksi yang perlu diperiksa, pemasukan terbaru, atau lonjakan transaksi terhadap catatan minggu terakhir. Hari yang selesai dalam budget dapat menampilkan Lumi bangga. Sisa anggaran adalah kesempatan menabung, bukan pemasukan baru. Peringatan budget muncul pada ambang 75%, lewat 100%, dan lewat 120%, masing-masing maksimal sekali per hari; tampil sebagai satu kartu per hari.

## Arsip perubahan v3

- Empat menu utama **Beranda, Kalender, Aset, Lainnya**. Ponsel memakai bilah bawah; layar lebar memakai bilah samping.
- Tombol **+ Catat** membuka formulir sebagai aksi. Panah kembali ada pada halaman lanjutan. Kembali dari ringkasan membawa isian sebelumnya; keluar dari draft meminta pilihan lanjut/buang. Form lama tidak muncul ketika memilih tab lain.
- Input rupiah bertitik otomatis, termasuk saldo awal, aset, anggaran, transaksi, dan kalkulator investasi. Pemilih melepaskan fokus keyboard sebelum membuka daftar.
- **Kalender** menampilkan pemasukan, pengeluaran, selisih arus kas, dan surplus/minus terhadap budget yang tersimpan pada hari tersebut.
- **Rencana sampai gajian** menghitung kebutuhan dari saldo yang dikonfirmasi, dengan jadwal awal tanggal 25 dan 1. Beranda kini memakai rencana ini sebagai sumber ruang belanja.
- **Saran Lumi** memberikan tindakan berdasarkan rencana, pilihan pengingat harian, dan edukasi investasi. Ikon Lumi bisa tenang/senang/fokus atau mengikuti kondisi rencana.
- Onboarding disederhanakan menjadi empat langkah. Data rekening disimpan saat selesai, sehingga kembali ke langkah sebelumnya tidak menggandakannya.
- Database dimigrasikan ke v3 untuk menyimpan riwayat rencana; snapshot kalender ikut cadangan finansial.

### Mulai mengalokasikan uang sampai gajian

1. Buka **Beranda → Rencana** atau **Lainnya → Rencana sampai gajian**.
2. Isi saldo likuid yang benar-benar tersedia sekarang. Tombol “Gunakan total estimasi rekening” hanya membantu mengambil angka awal; cocokkan dengan saldo asli.
3. Pilih tanggal pemasukan berikutnya. Tanggal bisa digeser bila gaji terlambat.
4. Isi tagihan yang belum dibayar, dana penyangga, serta bagian uang yang ingin dilindungi untuk bisnis dan investasi. Uang yang dilindungi harus masih termasuk saldo yang kamu masukkan.
5. Atur transportasi harian. Default Rp6.000 Senin–Jumat. Cadangan hanya dihitung untuk hari perjalanan sampai sehari sebelum pemasukan.
6. Isi perkiraan pemasukan tanggal 25 dan 1 jika diketahui. **Perkiraan ini tidak menambah saldo/budget.** Konfirmasi dan simpan rencana.
7. Saat gaji benar-benar diterima, catat pemasukan dan konfirmasi saldo terbaru untuk periode berikutnya. Jika mengoreksi saldo rekening atau menemukan transaksi lama yang belum tercatat, konfirmasi rencana lagi. Jika dana bisnis/investasi sudah keluar dari saldo likuid, perbarui saldo dan isi hanya alokasi yang masih tersisa.

Nominal sekitar Rp3,2 juta tidak otomatis dijadikan saldo pasti. Aplikasi meminta angka aktual. Target Rp1 juta bisnis dan Rp1,5 juta investasi juga dapat dikurangi/ditunda jika uang belum mencukupi kebutuhan dekat.

Contoh simulasi, **bukan saldo atau rekomendasi belanja pribadi**: saldo Rp3.200.000 pada 19 September 2026 sampai sebelum 25 September, tagihan Rp300.000, penyangga Rp200.000, bisnis Rp1.000.000, investasi Rp1.500.000, dan transportasi 4 hari kerja × Rp6.000 menyisakan Rp176.000 untuk 6 hari, sekitar Rp29.333/hari. Hasil berubah mengikuti tagihan dan saldo yang benar-benar kamu isi.

Rumus: saldo terkonfirmasi − tagihan − penyangga − bisnis − investasi − transportasi = dana bebas. Dana bebas dibagi jumlah hari periode. Setelah rencana dibuat, transaksi nyata mengurangi sisa dana; belanja hari ini dibatasi target harian dan sisa dana bebas. Tagihan terencana dan transportasi menggunakan cadangannya dahulu; kelebihannya masuk pemakaian budget bebas.

### Membaca kalender

Sejak v4, tiap tanggal menampilkan **pemasukan − pengeluaran**: `+100K`, `−45K`, `+1.25M`. Hijau berarti arus kas positif, merah negatif, nol netral. Nilai K/M dibulatkan turun sampai dua desimal; rincian tanggal menampilkan rupiah penuh. Angka ini bukan surplus budget. Mode sembunyikan saldo menutup nominal dan indikator warna finansial.

Ketuk tanggal untuk melihat transaksi dan rincian. **Selisih arus kas** membandingkan seluruh pemasukan/pengeluaran. **Surplus/minus budget** membandingkan target harian tersimpan dengan pemakaian budget bebas.

Hari tanpa rencana tidak diberi hasil surplus. Hari ini masih sementara; masa depan belum memiliki hasil. Pada hari pertama rencana, pemakaian budget mulai sejak saldo dikonfirmasi, sedangkan arus kas tetap menunjukkan transaksi sehari penuh. Mengubah rencana hari ini tidak mengubah target hari sebelumnya.

### Mengaktifkan pengingat dan ikon Lumi

Buka **Lainnya → Saran Lumi**, atau **Pengaturan → Lumi & pengingat**. Aktifkan Pengingat harian dan izinkan notifikasi Android jika diminta. Izin ini berbeda dari akses membaca notifikasi bank.

Pengingat otomatis maksimal sekali sehari, antara 09.00–20.59 waktu perangkat. Android dapat menunda pengiriman karena penghematan baterai. Ketuk pengingat untuk membuka saran; gunakan “Kirim contoh notifikasi” untuk memeriksa izin. Tidak ada nominal saldo dalam notifikasi, dan layar kunci memakai pesan umum.

Pilihan ikon: Lumi tenang, senang, fokus, atau mengikuti kondisi rencana saat aplikasi dibuka. Launcher bisa membutuhkan waktu untuk menyegarkan ikon. Lumi adalah maskot dan pendamping berbasis aturan lokal; percakapan interaktif belum tersedia.

## Riwayat perubahan v2

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

Rencana awal: **Rp6.000 per hari kerja (Senin–Jumat)**, **Rp1 juta tabungan modal bisnis**, dan **Rp1,5 juta investasi per bulan**. Target bulanan dapat diubah melalui **Lainnya → Anggaran & target**. Alokasi uang saat ini diatur melalui **Beranda → Rencana**. Target ini dicadangkan dalam perhitungan, tidak otomatis memindahkan uang.

1. Buat rekening/e-wallet milikmu.
2. Saat top-up berkala, catat sebagai **Pindah saldo / top-up sendiri**, berapa pun nominal dan waktunya.
3. Saat ongkos benar-benar dipakai, pilih kategori **Transportasi rutin**, termasuk melalui Detail Transaksi untuk catatan otomatis.
4. Cadangan transportasi = Rp6.000 × jumlah hari perjalanan bulan tersebut. Jika realisasi lebih besar, cadangan mengikuti realisasi agar anggaran tidak terlalu optimistis.
5. Biaya kategori Transportasi rutin tetap terlihat di arus kas, tetapi dikeluarkan dari pemakaian uang belanja bebas. Nominal Rp6.000 tidak otomatis dianggap transportasi.

Pada v2 ruang belanja memakai profil pendapatan bulanan. Sejak v3, Beranda memakai **Rencana sampai gajian** dari saldo yang dikonfirmasi; profil bulanan tetap tersedia untuk perencanaan jangka panjang.

Jangan masukkan cadangan transportasi kembali ke kolom kewajiban tetap. Top-up otomatis dengan tujuan yang belum pasti tetap perlu ditinjau; jadwal isi ulang tidak dianggap sebagai jadwal belanja.

### Pilihan investasi

Lihat [panduan investasi dan pemisahan dana bisnis](docs/investasi.md). Tidak ada satu produk yang pasti paling baik. Saran awal bergantung pada kecukupan dana darurat, kapan uang diperlukan, dan toleransi penurunan nilai. Angka hasil di kalkulator adalah asumsi simulasi, bukan penawaran atau janji keuntungan.

## Riwayat perubahan v1

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

Suite mencakup parser dan ledger, duplikasi Rp3, migrasi Room dari v1/v2/v3 ke v4, navigasi dan tombol kembali, alur catat transaksi sebenarnya, format rupiah/cursor, rencana gajian, cadangan transportasi/tagihan, kalender historis, izin/privasi notifikasi, ikon launcher, serta render tema terang/gelap.

Validasi v4: 115 pengujian unit/integrasi/UI lulus, Android Lint tanpa error, render Beranda/Kalender terang–gelap dan Aset/Pengaturan Notifikasi pada layar 320 dp diperiksa. Belum diverifikasi pada HP fisik dalam revisi ini. Format notifikasi dapat berubah antar versi aplikasi bank. GoPay tetap ditandai untuk ditinjau karena belum tersedia sampel notifikasi asli yang terkalibrasi. Penggabungan/pemisahan transaksi secara manual belum tersedia.

## Privasi dan cadangan

Pemrosesan inti berjalan lokal. Tidak ada pengiriman isi notifikasi ke server. Database Room termasuk riwayat rencana kalender berada di penyimpanan privat aplikasi; cadangan file dienkripsi menggunakan Android Keystore. Cadangan tersebut terikat kunci instalasi/perangkat, sehingga **bukan** cadangan portabel untuk reinstall atau pindah perangkat. Ekspor CSV tersedia untuk arsip transaksi.

Notifikasi yang tidak dikenal dipertahankan untuk ditinjau. Retensi mengosongkan isi mentah notifikasi yang sudah diproses; hash dan metadata identitas tetap disimpan untuk mencegah kiriman ulang dihitung kembali. Proteksi tangkapan layar dan kunci biometrik dapat diaktifkan dari Pengaturan.
