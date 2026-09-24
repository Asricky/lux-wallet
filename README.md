# Lux Wallet

Aplikasi Android untuk memantau keuangan pribadi melalui notifikasi **BCA mobile/myBCA, SeaBank, ShopeePay, dan GoPay**. Data diproses di perangkat, dapat digunakan offline, tanpa login rekening atau backend.

Kenali **Lumi**, karakter teal dengan aksen emas, pendamping keuanganmu. Lux Wallet menyediakan tema terang/gelap, kalender pemasukan/pengeluaran, rencana saldo sampai gajian, serta saran dan pengingat lokal.

<img src="Lumi-app-cover.png" width="360" alt="Sembilan cover Lumi, maskot Lux Wallet" />

Spesifikasi produk: [PRD.md](PRD.md) · Panduan tampilan: [DESIGN.md](DESIGN.md).

<!-- LATEST_RELEASE_START -->
## Download APK terbaru

[**Download app-debug-v5.apk**](https://github.com/Asricky/lux-wallet/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v5.apk)

Versi terbaru: **v5 (1.0.5)**. Android 10 atau lebih baru.

Lokasi file: `lux-wallet\app\build\outputs\apk\debug\app-debug-v5.apk`.

Semua versi sebelumnya tersedia di [folder APK debug](app/build/outputs/apk/debug). Nama launcher tetap **Lux Wallet**.

SHA-256: `410BC10C2C7CFBEE7C7ED42C5FE87FBB8DDDE89561F63267C8309E9E8618D0D7`

### Pembaruan v5

- Sembilan cover dan ikon Lumi dari Lumi-app-cover.png, mengikuti transaksi aktual, budget, dan pencapaian; alias launcher lama tetap tersedia saat upgrade.
- Simpan & konfirmasi atau abaikan duplikat langsung kembali ke Beranda; Food & Drink selalu di urutan pertama. Catatan tanpa rekening dapat ditinjau dengan memilih rekening aktif.
- Kalender mendapat kartu Masuk/Keluar/Selisih, grafik Tren Pengeluaran per bulan dengan pemilih tanggal, serta rekomendasi budget tanpa mengubah target tersimpan.
- Rencana sampai gajian diringkas menjadi kartu saldo, hari tersisa, safe to spend, budget, pemakaian, penyangga, dan proyeksi saldo. Saran Lumi memakai satu insight utama dan maksimal dua pendukung.
- Perlu ditinjau memiliki aksi Tinjau/Hapus dengan konfirmasi. Aset dan rekening bisa diedit, diarsipkan, serta dipulihkan tanpa menghapus histori.
- Pemulihan listener saat disconnect, startup, boot, dan update; percobaan terbatas dengan jeda bertahap. Pengaturan menampilkan status koneksi, waktu notifikasi terakhir, dan aktivitas parser.
- Deduplikasi lintas BCA mobile/myBCA memakai bukti referensi atau identitas kejadian. Kemiripan yang belum pasti ditahan tanpa ledger tambahan; transaksi berbeda tetap dicatat.
- Migrasi Room v5 menambah status arsip aset tanpa menghapus saldo, transaksi, ledger, atau rencana. README dan tautan APK terbaru tetap diperbarui otomatis setiap rilis.
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

## Kalender, rencana, dan rekomendasi budget

Kalender menampilkan kartu Masuk/Keluar/Selisih serta arus kas harian bertanda. Grafik **Tren Pengeluaran** mengikuti bulan yang dipilih dan hanya menampilkan tanggal yang sudah berjalan. Ketuk titik atau geser pemilih tanggal untuk melihat nominal dan transaksi hari tersebut. Mode sembunyikan nominal juga menutup grafik.

**Saran budget hari ini** selalu bertanggal hari ini, termasuk saat melihat kalender bulan lain. Empat angka dibedakan: rekomendasi per hari, budget tersimpan, sisa budget hari ini, dan aman dibelanjakan. Rekomendasi tidak mengubah anggaran tersimpan atau memindahkan uang.

Perhitungan menggunakan nilai lebih rendah antara sisa saldo snapshot rencana dan total estimasi rekening aktif (jika tersedia). Pemasukan/pengeluaran aktual sudah masuk snapshot berjalan sehingga tidak ditambahkan dua kali. Cadangan tagihan/transportasi yang belum terpakai, penyangga, modal bisnis, dan investasi tetap dilindungi. Dana bebas bersama pemakaian hari ini dibagi sisa hari. Aman dibelanjakan dibatasi sisa target harian dan rekomendasi; perkiraan gaji tanggal 25/1 tidak dianggap uang tersedia. Jika saldo rekening belum sesuai, koreksi melalui Aset lalu konfirmasi rencana baru.

Rencana sampai gajian menampilkan ringkasan visual dan proyeksi saldo sebelum pemasukan. Proyeksi mengasumsikan pemakaian sisa budget serta cadangan tagihan/transportasi; ini simulasi, bukan saldo bank yang terverifikasi. Rincian alokasi/jadwal dapat dibuka. Saran Lumi menampilkan satu insight utama dan paling banyak dua angka pendukung; pengingat, ikon, dan edukasi investasi ada dalam bagian yang bisa dibuka.

## Peninjauan, aset, dan perlindungan transaksi ganda

- **Tinjau** membuka detail untuk memilih kategori, mengedit catatan, dan menyimpan. Food & Drink berada paling atas. Simpan/abaikan yang berhasil langsung kembali ke Beranda dengan data terbaru.
- **Hapus** di daftar peninjauan meminta konfirmasi, mengabaikan catatan, dan membalik pengaruh saldo jika sebelumnya sudah dihitung. Bukti dan histori tetap tersimpan.
- Notifikasi yang belum dikenali dapat dibaca untuk dicatat manual bila valid; nilainya belum masuk saldo otomatis. Catatan tanpa rekening harus dipasangkan dengan rekening aktif yang benar. Transaksi sebelum saldo awal perlu diabaikan karena sudah tercakup saldo awal.
- Ketuk aset/rekening untuk mengedit nama dan nilai. **Hapus dari aset aktif** mengarsipkannya. Aset manual dipulihkan dari **Arsip aset**; rekening dari **Kelola / tambah rekening → Aktifkan kembali**. Koreksi rekening tetap memakai ledger penyesuaian, bukan pemasukan.
- Lintas BCA mobile/myBCA: rekening tunggal, nominal, arah, jenis, waktu, referensi, nama pihak, serta isi notifikasi diperiksa. Referensi identik atau identitas kejadian yang kuat digabung menjadi satu transaksi dengan beberapa observation. Nama/nominal saja tidak cukup untuk menghapus transaksi.
- Kemiripan dalam 30 detik tanpa bukti kuat menjadi calon duplikat **belum dihitung**. Abaikan bila satu pembayaran; konfirmasi sebagai transaksi berbeda bila memang dua pembayaran. Referensi berbeda, pihak berbeda, atau waktu berjauhan tetap dipisahkan. Bila beberapa rekening memakai provider sama, pilih rekening saat peninjauan; aplikasi tidak menebak rekening pertama.

## Pemulihan pemantau dan ikon Lumi

Pengaturan pemantauan membedakan **Aktif / Terputus / Perlu izin**, disertai waktu notifikasi diterima dan parser terakhir. Listener terpisah dari Activity. Notifikasi yang sudah diterima dipersistenkan melalui scope aplikasi; antrean parser dapat dilanjutkan setelah restart. Disconnect, startup proses, boot, dan update meminta rebind bila izin masih ada, disusul maksimal tiga percobaan dengan backoff. Tidak ada polling service atau foreground service terus-menerus.

Android tetap mengendalikan binding dan penghematan baterai. Setelah **Paksa berhenti**, buka aplikasi kembali. Notifikasi yang hilang sebelum diterima dan tidak tersedia lagi di Android tidak dapat dipulihkan dari bank. Uji otomatis menggunakan Robolectric; perilaku OEM dan launcher HP fisik tetap perlu diverifikasi pada perangkat pengguna.

Sembilan ikon berasal dari [Lumi-app-cover.png](Lumi-app-cover.png). Pilih **Saran Lumi → Pengingat & ikon Lumi**: otomatis atau satu ekspresi tetap. Mode otomatis diperbarui dari data lokal selama proses aplikasi hidup; tidak membangunkan perangkat hanya untuk mengganti ikon. Alias baru diaktifkan sebelum alias lain dinonaktifkan, tanpa membunuh proses. Launcher dapat menyimpan cache ikon sesaat. Alias tiga versi lama tetap dideklarasikan agar update tidak memutus pintasan lama.

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

Suite mencakup parser dan ledger, duplikasi Rp3, migrasi Room dari v1/v2/v3/v4 ke v5, navigasi dan tombol kembali, alur catat transaksi sebenarnya, format rupiah/cursor, rencana gajian, cadangan transportasi/tagihan, kalender historis, izin/privasi notifikasi, ikon launcher, serta render tema terang/gelap.

Validasi v5: **137 pengujian unit/integrasi/UI lulus**; build APK berhasil; Android Lint **0 error, 13 warning** (kompatibilitas resource/ikon, resource lama, dan parameter komponen). Render sembilan launcher, Beranda/Kalender terang–gelap, serta Planner/Coach/Tren pada layar 320 dp diperiksa. Alur konfirmasi/abaikan kembali Beranda, arsip aset, rekomendasi, BCA lintas aplikasi, dan pemulihan listener tanpa Activity turut diuji. Belum diverifikasi pada HP fisik dalam revisi ini. Format notifikasi dapat berubah antar versi aplikasi bank. GoPay tetap ditandai untuk ditinjau karena belum tersedia sampel notifikasi asli yang terkalibrasi. Penggabungan/pemisahan transaksi secara manual belum tersedia.

## Privasi dan cadangan

Pemrosesan inti berjalan lokal. Tidak ada pengiriman isi notifikasi ke server. Database Room termasuk riwayat rencana kalender berada di penyimpanan privat aplikasi; cadangan file dienkripsi menggunakan Android Keystore. Cadangan tersebut terikat kunci instalasi/perangkat, sehingga **bukan** cadangan portabel untuk reinstall atau pindah perangkat. Ekspor CSV tersedia untuk arsip transaksi.

Notifikasi yang tidak dikenal dipertahankan untuk ditinjau. Retensi mengosongkan isi mentah notifikasi yang sudah diproses; hash dan metadata identitas tetap disimpan untuk mencegah kiriman ulang dihitung kembali. Proteksi tangkapan layar dan kunci biometrik dapat diaktifkan dari Pengaturan.
