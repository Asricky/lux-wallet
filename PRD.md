# Lux Wallet — Product Requirements Document

Versi produk: **v3 / 1.0.3** · Revisi: **19 September 2026**
Platform: Android 10+ · Bahasa: Indonesia · Pemilik repo: Asricky

Dokumen ini menjadi spesifikasi produk aktif. Arah tampilan dan komponen ada di [DESIGN.md](DESIGN.md), petunjuk instalasi di [README.md](README.md).

## 1. Tujuan dan batas produk

Lux Wallet membantu pengguna mengetahui posisi uang, mencatat aktivitas bank/e-wallet dari notifikasi, dan membagi uang yang tersedia sampai pemasukan berikutnya. Lumi adalah penguin pendamping produk.

Fungsi inti berjalan offline dan data finansial disimpan di perangkat. Aplikasi tidak meminta kredensial bank, melakukan pembayaran, memindahkan dana, membeli investasi, atau menganggap saldo estimasi sebagai saldo bank yang terverifikasi.

Prioritas: navigasi dapat diprediksi, nominal tidak terhitung dua kali, perhitungan dapat dijelaskan, dan pengguna tetap dapat mengoreksi catatan.

## 2. Navigasi dan alur utama

- Empat tujuan utama: Beranda, Kalender, Aset, Lainnya.
- Ponsel memakai bilah bawah; lebar minimal 600 dp memakai bilah samping. Label tetap terlihat.
- Catat transaksi merupakan tombol aksi, bukan tab. Setelah disimpan atau dibatalkan, pengguna kembali ke halaman asal.
- Halaman lanjutan memiliki app bar dan panah kembali; bilah utama disembunyikan saat formulir/detail dibuka.
- Memilih menu utama selalu membuka akar menu itu. Formulir atau detail lama tidak dibuka kembali sebagai menu utama.
- Tombol kembali Android pada menu utama selain Beranda kembali ke Beranda. Pada Beranda, perilaku keluar mengikuti Android.
- Panah kembali dan tombol kembali sistem mengikuti alur yang sama. Sheet/dialog ditutup terlebih dahulu.
- Catat transaksi: detail → ringkasan → simpan. Kembali dari ringkasan mempertahankan isian; keluar dari draft berisi memerlukan pilihan lanjut atau buang.
- Saat menyimpan, tombol simpan dinonaktifkan. Halaman ditutup hanya setelah penyimpanan berhasil. Kegagalan tampil dan isian dipertahankan.
- Pilihan rekening/kategori/periode membuka bottom sheet. Keyboard dilepas saat pemilih dibuka.

## 3. Onboarding

Empat langkah: perkenalan dan privasi, sumber notifikasi, saldo rekening, target bulanan. Setiap langkah setelah perkenalan dapat kembali tanpa menyimpan rekening baru. Nilai input bertahan saat perubahan konfigurasi.

Akses notifikasi bisa diberikan sekarang atau nanti; pencatatan manual tetap tersedia. Akun yang tidak diisi tidak dibuat. Aset nonlikuid, kewajiban, dan tujuan tambahan dikelola setelah onboarding.

Rekening dan profil disimpan pada langkah terakhir dalam transaksi database. Pengulangan setelah kegagalan tidak membuat akun provider yang sama lagi. Status onboarding selesai ditulis setelah penyimpanan berhasil.

## 4. Sumber, parsing, dan bukti

Sumber bawaan: myBCA, SeaBank, ShopeePay, GoPay. Hanya paket yang diizinkan diproses. Paket tambahan memerlukan pemetaan eksplisit dari diagnostik. Notifikasi Lux Wallet sendiri tidak menjadi sumber keuangan.

Observation menyimpan sumber, paket, kunci notifikasi Android, waktu kirim/terima, original event time bila ada, isi notifikasi, hash payload, hash isi kanonik, status parser, dan relasi ke transaksi logis.

Parser membaca text, bigText, subText, dan textLines. Notifikasi gagal, pending, OTP, serta promosi tidak dijadikan transaksi. Nilai, merchant, atau jenis yang belum pasti tidak dikarang dan masuk peninjauan. Format rupiah Indonesia dan notasi desimal bank diproses oleh parser yang diuji terpisah dari input nominal manual.

GoPay tetap memerlukan peninjauan karena kalibrasi sampel asli belum lengkap. Format notifikasi bank bisa berubah; diagnosis harus menunjukkan bukti yang diterima, bukan menganggap koneksi berarti semua transaksi pasti terbaca.

## 5. Transaksi, deduplikasi, dan ledger

- Satu kejadian finansial dapat memiliki beberapa observations, tetapi hanya satu dampak ledger.
- Pembaruan dengan identitas kejadian yang sama tidak membuat transaksi baru.
- Ringkasan myBCA identik dalam jendela 30 detik dengan identitas belum pasti menjadi calon duplikat: nominal ditahan, tidak mengubah saldo/arus kas sampai dikonfirmasi berbeda.
- Nominal sama saja tidak cukup untuk menghapus transaksi. Dua referensi eksplisit berbeda dipertahankan sebagai kejadian berbeda.
- Peninjauan duplikat lama hanya bekerja bila bukti yang diperlukan masih tersedia. Pembalikan dampak ganda berlangsung tepat sekali.
- Mengabaikan transaksi membalik dampak ledger tepat sekali. Mengonfirmasi calon duplikat berbeda menerapkan dampak nominal tepat sekali.
- Transfer antar akun sendiri dan top-up sendiri adalah perpindahan saldo; bukan pemasukan atau pengeluaran.
- Pencatatan transaksi dan perubahan saldo berlangsung atomik. Pemrosesan dapat diulang tanpa menggandakan dampak.
- Koreksi saldo rekening mencatat selisih sebagai penyesuaian, bukan arus kas. Saldo laporan dari notifikasi tidak diam-diam mengganti estimasi.
- Catatan manual wajib memiliki nominal positif, akun sumber, serta kategori atau tujuan yang sesuai. Tujuan transfer tidak boleh sama dengan sumber.

Jenis transaksi mencakup pemasukan, pengeluaran, QRIS, transfer internal/eksternal, top-up, pembayaran marketplace, refund, biaya, bunga, cashback, penyesuaian saldo, dan jenis belum dikenali.

## 6. Rekening, aset, dan kewajiban

Rekening memiliki saldo awal dan waktu acuannya, saldo estimasi, provider, kepemilikan, status aktif, serta pilihan hitung dalam kekayaan. Transaksi sebelum waktu saldo awal tidak ditambahkan kembali ke saldo estimasi.

Aset dibagi menjadi simpanan, investasi, lainnya, serta utang. Nominal akun/aset/kewajiban dapat diedit manual. Kekayaan bersih = aset yang disertakan − kewajiban. Kekayaan bersih tidak sama dengan uang likuid yang bisa dibelanjakan.

Tombol mata menyembunyikan nominal pada Beranda, Aset, kalender, dan ringkasan rencana. Proporsi sebaran aset ikut disembunyikan. Saat pengguna secara eksplisit membuka editor nominal, isian tetap terlihat agar dapat diperiksa.

## 7. Beranda

Header memuat Lumi, identitas Lux Wallet, indikator pemantauan kecil yang dapat diketuk, dan pengaturan. Hijau berkedip berarti listener tersambung dan tidak melaporkan kegagalan; merah berarti akses/koneksi perlu diperiksa.

Konten: kekayaan bersih, pintasan riwayat/rencana/kalkulator/peninjauan, ruang belanja sampai gajian, saran Lumi, transaksi terbaru, sebaran aset. Tidak ada kartu besar terpisah untuk status notifikasi.

Tanpa rencana saldo terkonfirmasi, tampilkan ajakan membuat rencana; jangan menampilkan perkiraan pendapatan bulanan sebagai uang yang tersedia.

## 8. Rencana saldo sampai pemasukan

Tanggal pemasukan bawaan adalah 25 dan 1, dengan tanggal terdekat setelah hari ini. Pengguna boleh menggeser tanggal 1–62 hari ke depan jika jadwal berbeda atau terlambat. Nominal perkiraan kedua pemasukan bersifat opsional dan **tidak** menambah saldo atau budget.

Input rencana:

| Input | Arti |
| --- | --- |
| Saldo tersedia | Total uang likuid yang benar-benar dikonfirmasi sekarang; mencakup uang yang akan dilindungi di bawah |
| Tagihan | Kewajiban yang belum dibayar sebelum tanggal pemasukan |
| Penyangga | Dana yang tidak dialokasikan untuk belanja rutin |
| Modal bisnis | Bagian saldo yang dilindungi untuk bisnis |
| Investasi | Bagian saldo yang dilindungi untuk rencana investasi |
| Transportasi | Biaya per hari perjalanan dan pilihan Senin–Jumat/setiap hari |
| Tanggal pemasukan | Batas eksklusif periode: rencana membiayai hari ini sampai sehari sebelumnya |

Default target bulanan adalah modal bisnis Rp1 juta dan investasi Rp1,5 juta. Rencana periode meminta pengguna mengonfirmasi bagian yang masih ada dalam saldo; tidak ada pemotongan otomatis setiap tanggal gajian. Estimasi total rekening hanya diambil setelah pengguna memilihnya dan tetap perlu diverifikasi.

Rumus awal:

```text
cadangan transportasi = biaya harian × jumlah hari perjalanan dalam periode
saldo dilindungi = tagihan + penyangga + bisnis + investasi + transportasi
dana bebas awal = saldo terkonfirmasi − saldo dilindungi
target harian = max(0, floor(dana bebas awal ÷ jumlah hari periode))
```

Transaksi sesudah waktu konfirmasi diperhitungkan. Saldo yang dikonfirmasi sudah mencakup transaksi sebelumnya, sehingga tidak boleh dikurangi lagi. Pendapatan nyata dalam periode menambah dana tersisa; target harian yang disimpan tidak otomatis dinaikkan. Pengguna dapat mengonfirmasi rencana baru.

Kategori **Transportasi rutin** dan **Tagihan terencana** memakai cadangannya terlebih dahulu. Setelah cadangan habis, kelebihan masuk penggunaan budget bebas. Transfer sendiri, top-up sendiri, calon duplikat yang ditahan, transaksi diabaikan, dan penyesuaian saldo tidak menjadi pemakaian budget.

```text
kas tersisa = saldo terkonfirmasi + pemasukan nyata − pengeluaran nyata
cadangan tersisa = penyangga + bisnis + investasi
                  + max(0, tagihan awal − tagihan dibayar)
                  + max(0, transportasi awal − transportasi dibayar)
dana bebas tersisa = kas tersisa − cadangan tersisa
sisa belanja hari ini = min(target harian − pemakaian hari ini, dana bebas tersisa)
```

Nilai negatif memicu penjelasan kekurangan alokasi; angka belanja yang ditawarkan tidak negatif. Selisih budget tetap terlihat pada detail. Persentase dapat melebihi 100%; bar visual dibatasi penuh.

Setelah rencana berakhir, minta konfirmasi saldo dan tanggal baru. Jadwal gajian tidak membuktikan uang sudah diterima. Koreksi saldo atau transaksi terlambat dari sebelum snapshot memerlukan konfirmasi ulang rencana.

## 9. Kalender pemasukan/pengeluaran

Kalender bulanan dimulai Senin. Tersedia bulan sebelumnya/berikutnya, pemilih periode, ringkasan masuk/keluar/selisih, serta detail tanggal dan daftar transaksi yang dapat dibuka.

Dua indikator harus dibedakan:

1. **Selisih arus kas** = seluruh pemasukan − seluruh pengeluaran hari tersebut.
2. **Surplus/minus budget** = target budget bebas tersimpan − pemakaian budget bebas hari tersebut.

Arus kas mencakup transportasi dan tagihan. Pemakaian budget hanya memasukkan kelebihan di atas cadangannya. Transfer sendiri dan koreksi saldo tidak masuk arus kas.

Hari tanpa rencana diberi label belum ada target; jangan disimpulkan surplus. Hari mendatang tidak memiliki hasil aktual. Hari ini menunjukkan sisa sementara. Hari pertama rencana memakai transaksi setelah konfirmasi, dengan keterangan bahwa arus kas tetap mencakup satu hari penuh.

Riwayat rencana disimpan di Room. Revisi hari ini mengganti snapshot hari ini; hari-hari sebelumnya tetap memakai target yang berlaku pada hari tersebut. Koreksi transaksi nyata dapat mengubah realisasi historis, tetapi perubahan target hari ini tidak menulis ulang target kemarin.

## 10. Saran Lumi dan notifikasi

Saran menggunakan aturan yang dapat dijelaskan dari data lokal: belum ada rencana, rencana berakhir, alokasi kekurangan dana, budget terlampaui, besok jadwal pemasukan, perlindungan dana kebutuhan dekat, lalu edukasi investasi.

Pengguna mengaktifkan pengingat secara eksplisit di **Saran Lumi**. Android 13+ meminta izin POST_NOTIFICATIONS. Penolakan izin tidak memblokir aplikasi atau saran dalam aplikasi.

WorkManager memeriksa secara berkala; maksimal satu pengingat otomatis per tanggal perangkat, hanya pukul 09.00–20.59. Penghematan baterai dapat menunda atau meniadakan pengiriman hari tersebut. Tidak menjanjikan waktu presisi. Tombol contoh notifikasi adalah aksi pengguna dan terpisah dari kuota otomatis.

Channel pengingat terpisah dari akses membaca notifikasi. Mematikan pengingat membatalkan pekerjaan periodik dan notifikasi yang tampil. Ketukan notifikasi membuka Saran Lumi, termasuk saat aplikasi sudah terbuka; kunci aplikasi tetap harus dilewati bila aktif.

Notifikasi tidak memuat nominal saldo. Versi layar kunci memakai pesan umum. Saran investasi bersifat bersyarat pada kebutuhan likuiditas, dana darurat, horizon, risiko, dan biaya; tidak menjanjikan hasil atau menyebut satu produk selalu terbaik. Sumber primer ditautkan ke OJK dan Kementerian Keuangan.

## 11. Kalkulator dan target bulanan

Kalkulator aritmetika mendukung prioritas operasi, persen, tanda kurung, dan pemakaian angka kekayaan bersih. Simulasi investasi memakai modal awal, setoran akhir bulan, asumsi hasil tahunan efektif, dan durasi. Hasil tidak memperhitungkan biaya, pajak, dan inflasi; bukan transaksi atau imbal hasil terjamin.

Anggaran & target mempertahankan profil bulanan, rencana transportasi, tabungan bisnis, investasi, serta tujuan tambahan. Rencana saldo sampai gajian menjadi sumber ruang belanja Beranda v3. Profil pendapatan bulanan tetap untuk perencanaan, bukan dianggap saldo nyata.

Input rupiah manual menggunakan bilangan bulat sampai 15 digit dengan titik pemisah ribuan visual. Nilai yang disimpan tetap angka rupiah, kursor dan penghapusan bekerja di tengah angka. Paste kelompok Indonesia diterima; format desimal ambigu ditolak tanpa mengalikan nilainya secara diam-diam.

## 12. Identitas Lumi

Lumi adalah penguin hitam dengan wajah krem, aksen emas, dan dompet di dada. Tiga ekspresi tersedia: tenang, senang, fokus. Karakter harus tetap orisinal dan tidak menyalin karakter bank atau aplikasi lain.

Ikon launcher dapat dipilih tetap atau mengikuti kondisi rencana ketika aplikasi dibuka. Perubahan mengaktifkan alias baru sebelum menonaktifkan alias lama agar tetap ada pintu masuk. Launcher tertentu mungkin memperbarui tampilan dengan jeda.

Lumi tidak mengejek, menakut-nakuti, atau menekan pengguna untuk berinvestasi. Fase berikutnya dapat menambahkan pendamping percakapan dengan identitas Lumi, persetujuan data yang jelas, dan fungsi baca-saja terlebih dahulu. Fitur percakapan belum diimplementasikan dalam v3.

## 13. Privasi, cadangan, dan diagnostik

Database berada di penyimpanan privat aplikasi. Kunci biometrik dan blokir tangkapan layar dapat diaktifkan. Retensi mengosongkan isi notifikasi yang sudah diproses, mempertahankan hash/metadata deduplikasi. Notifikasi yang belum dikenali tetap dapat ditinjau.

Cadangan manual terenkripsi mencakup rekening, aset, kewajiban, kategori, transaksi, ledger, aturan, anggaran, tujuan, profil, dan snapshot rencana kalender. Cadangan terikat Android Keystore instalasi/perangkat; bukan cadangan portabel untuk reinstall. Ekspor CSV tersedia untuk arsip transaksi. Preferensi tampilan/pengingat bukan bagian dari cadangan finansial.

Mode diagnostik tidak aktif secara default. Penemuan paket hanya mencatat nama paket; pemetaan sumber tambahan dilakukan eksplisit. Kredensial, keystore, konfigurasi lokal, dan berkas alat pengembangan tidak disertakan dalam repo.

## 14. Arsitektur dan migrasi

Kotlin, Jetpack Compose Material 3, Navigation Compose, ViewModel/Flow, Room, DataStore Preferences, WorkManager, dan Android Keystore. Parser dan mesin perhitungan memiliki pengujian terpisah dari UI.

Database v1 → v2 menambah eventTime/contentHash observation. V2 → v3 menambah tabel payday_plans. Upgrade tidak memakai destructive migration. Backup payload tetap dapat membaca cadangan lama tanpa snapshot kalender melalui nilai default kosong.

Pengamatan data UI mengikuti perubahan transaksi dan tanggal lokal. Tidak ada backend, biaya langganan, atau akses layanan daring untuk perhitungan inti. Tautan sumber investasi dibuka oleh pengguna di browser.

## 15. Distribusi dan penerimaan

Artefak berada di `app/build/outputs/apk/debug/app-debug-vN.apk`. Jalankan `build-update.ps1` untuk test, lint, build, dan kenaikan nomor hanya setelah build berhasil. Nama launcher tetap Lux Wallet. Application ID debug dan sertifikat harus tetap sama agar update mempertahankan data.

Penerimaan v3:

- Empat menu dapat dipilih berulang, halaman detail bisa kembali, formulir kembali ke asal setelah disimpan/dibatalkan.
- Rupiah bertitik, cursor mapping, hapus, dan paste diuji.
- Pembayaran Rp3 tidak digandakan; peninjauan dan rollback ledger tetap lulus regresi.
- Perhitungan gajian meliputi tanggal 25/1, akhir bulan/tahun, tahun kabisat, kekurangan dana, transfer, cadangan, dan historis.
- Perkiraan gaji tidak menjadi saldo; hari tanpa rencana tidak dianggap surplus.
- Notifikasi memeriksa izin, channel, batas harian, jam tenang, dan privasi layar kunci.
- Ikon selalu menyisakan satu alias launcher aktif.
- Migrasi dari database v1 dan v2 mempertahankan bukti lama serta membuat tabel rencana.
- Build dan lint selesai; pemeriksaan pada perangkat fisik dicatat terpisah dari pengujian JVM/UI simulasi.

## 16. Batasan dan pengembangan berikutnya

Belum tersedia sinkronisasi cloud, impor riwayat bank yang tidak tertangkap, penggabungan/pemisahan transaksi manual, percakapan Lumi, atau jaminan format notifikasi semua versi bank. Otomasi rencana berdasarkan seluruh tagihan terjadwal dan perhitungan khusus hari libur dapat dikembangkan berikutnya.

Snapshot anggaran bukan saldo bank langsung: data yang terlambat masuk, pencatatan dari akun yang tidak termasuk uang snapshot, atau perpindahan investasi eksternal harus ditinjau. Jangan menambahkan nilai aset nonlikuid ke saldo belanja hanya agar angka alokasi tampak cukup.
