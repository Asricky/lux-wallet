# Lux Wallet — Panduan desain

Revisi 19 September 2026 · Berlaku sejak v3. Spesifikasi perilaku dan perhitungan ada di [PRD.md](PRD.md).

## Karakter produk

Tenang, jelas, dan dekat dengan keseharian. Utamakan saldo, tindakan berikutnya, serta penjelasan angka. Referensi aplikasi perbankan digunakan untuk pola interaksi yang familiar, bukan menyalin logo, ilustrasi, data rekening, atau susunan produknya.

Lumi memberi konteks dan semangat secukupnya. Aplikasi harus tetap terasa sebagai alat keuangan yang dapat dipercaya.

## Identitas Lumi

<p><img src="docs/brand/lumi-calm.svg" width="140" alt="Lumi tenang" /> <img src="docs/brand/lumi-happy.svg" width="140" alt="Lumi senang" /> <img src="docs/brand/lumi-focus.svg" width="140" alt="Lumi fokus" /></p>

Penguin dengan siluet membulat, bulu hitam kebiruan, wajah/perut krem, paruh dan kaki emas, serta dompet kecil di dada. Mata dan alis membedakan ekspresi; siluet utama tidak berubah.

| Ekspresi | Penggunaan |
| --- | --- |
| Tenang | Ikon bawaan, sambutan, belum ada rencana |
| Senang | Rencana aktif dengan ruang uang yang cukup |
| Fokus | Budget/pendanaan perlu ditinjau atau rencana berakhir |

Jangan menambahkan hukuman visual, tangisan berlebihan, ancaman kehilangan streak, atau ungkapan menyalahkan pengguna. Kondisi finansial adalah informasi, bukan nilai moral.

Aset sumber: `docs/brand/lumi-*.svg`. Aset Android: `drawable/ic_launcher_foreground.xml`, `drawable/lumi_happy.xml`, `drawable/lumi_focus.xml`. Ketiganya memakai viewport 108 × 108; wajah dan ciri utama harus aman pada crop ikon lingkaran maupun squircle. Gunakan vektor untuk menghindari blur.

Ikon Android merupakan adaptive icon dengan lapisan background emas dan foreground Lumi. Pilihan ekspresi menggunakan activity aliases. Mode mengikuti rencana adalah pilihan pengguna; update ekspresi saat aplikasi dibuka. Nama launcher tetap **Lux Wallet**.

## Warna

| Token | Terang | Gelap | Kegunaan |
| --- | --- | --- | --- |
| Background | `#F6F4EF` | `#101110` | Latar utama |
| Surface | `#FFFDF8` | `#191A18` | Form, panel, kartu penting |
| Primary | `#76571D` | `#D8B66A` | Aksi, pilihan aktif |
| Teks utama | `#141413` | `#F5F1E8` | Nominal dan judul |
| Teks sekunder | `#656057` | `#BBB7AC` | Keterangan |
| Garis halus | `#DED8CB` | `#393A33` | Pemisah |
| Latar ikon Lumi | `#E9C77F` | `#E9C77F` | Launcher saja |

Gunakan warna semantik Material untuk error dan container; selalu sertai status dengan teks/simbol. Emas sebagai aksen, bukan warna seluruh halaman. Hindari gradasi berulang, efek glow, bayangan besar, dan deretan kartu yang semuanya tampak sama penting.

## Tipografi dan jarak

- Gunakan keluarga sistem yang ditetapkan di `core/ui/theme/Type.kt`; jangan menambah font dekoratif.
- Nominal utama: headlineLarge/headlineMedium. Judul halaman: headlineSmall atau app bar. Judul seksi: titleMedium/titleLarge. Keterangan: bodySmall, bukan teks sangat kecil.
- Satu judul utama per layar. Step form menjelaskan tahap, tidak mengulang nama aplikasi.
- Margin ponsel 16–20 dp; jarak antarseksi 16–24 dp; isi kartu 16–20 dp.
- Radius field 16 dp; kartu 20 dp; dialog/sheet mengikuti Material. Tidak semua baris perlu dibungkus kartu.
- Target sentuh minimal 48 dp untuk aksi utama. Kalender menggunakan sel minimal 56 dp tinggi dan tujuh kolom.
- Konten utama menyediakan ruang bawah bagi tombol catat agar baris terakhir tetap terbaca dan dapat disentuh.
- Hindari ukuran tetap untuk paragraf. Teks panjang dan skala font perangkat harus dapat membungkus.

## Navigasi

Empat tab berlabel: **Beranda · Kalender · Aset · Lainnya**. Ikon outline, satu indikator aktif, tanpa tombol tengah yang menyamar sebagai tab. Pada lebar ≥600 dp gunakan navigation rail.

Catat adalah floating action button pada Beranda, Kalender, dan Aset. Halaman detail/form punya panah kembali dan tidak menampilkan tab utama. Tombol kembali sistem dan toolbar memakai dispatcher yang sama.

Memilih tab membuka akar menu. Jangan memulihkan formulir lama ketika pengguna memilih tab utama. Back pada tab selain Beranda kembali ke Beranda; Back di Beranda mengikuti sistem.

## Komponen dan interaksi

**Nominal.** Gunakan `MoneyField`: prefiks Rp, keyboard angka, pemisah ribuan otomatis, satu baris, cursor mapping. Simpan digit rupiah tanpa titik. Nilai desimal ambigu tidak boleh berubah menjadi kelipatan sepuluh/seratus akibat membuang separator.

**Pemilih.** Gunakan `ChoiceField`: label di atas, nilai di kiri, chevron di kanan, daftar pilihan pada bottom sheet. Lepaskan fokus keyboard. Pilihan aktif mempunyai checkmark; daftar panjang dapat digulir. Dismiss mengembalikan fokus interaksi ke layar asal.

**Transaksi.** Satu baris memuat tanggal, merchant/keterangan, nominal, sumber/kategori, dan chevron. Transfer sendiri tidak diberi label pengeluaran. Calon duplikat harus jelas belum dihitung.

**Penyimpanan.** Tombol utama berada setelah input/ringkasan, menampilkan “Menyimpan…” saat sibuk. Jangan menutup form sebelum berhasil. Kegagalan menjaga isian. Konfirmasi keluar hanya untuk draft yang benar-benar berubah/berisi.

**Privasi nominal.** Tombol mata memiliki label aksesibilitas yang menjelaskan aksi. Pilihan disimpan. Grafik proporsi dan warna status kalender yang menyiratkan kondisi keuangan ikut disembunyikan ketika nominal disembunyikan.

**Pemantauan.** Lonceng kecil di header Beranda, titik status berkedip lembut sekitar 1,2 detik. Ketukan membuka pengaturan pemantauan. Tidak memakai kartu status besar di Beranda. Tetap dapat dipahami tanpa animasi melalui label status.

## Kalender dan angka anggaran

Waktu lokal perangkat. Header bulan yang jelas, tombol mundur/maju, grid Senin–Minggu. Sel dipilih menunjukkan panel rincian dan transaksi.

Tampilkan dua label berbeda: “Selisih arus kas” dan “Surplus/minus budget”. Hari ini: “Sisa budget sementara”. Hari tanpa rencana: “Belum ada target”. Masa depan: “Belum ada hasil aktual”. Jangan membuat hari tanpa data tampak sebagai keberhasilan finansial.

Bar budget menunjukkan penggunaan aktual terhadap target harian. Saat dana kurang, tampilkan penjelasan dan aksi tinjau rencana; angka besar tidak boleh menyiratkan uang yang belum diterima bisa dibelanjakan.

## Bahasa Lumi

Gunakan Indonesia sehari-hari yang singkat dan spesifik:

- “Konfirmasi saldo yang tersedia.”
- “Budget hari ini sudah terlampaui. Periksa transaksi dan tunda belanja yang bisa menunggu.”
- “Periode rencanamu berakhir. Catat pemasukan jika sudah diterima.”

Hindari klaim “pasti untung”, “investasi terbaik untuk semua”, kalimat promosi panjang, dan jargon teknis pada alur pengguna. Jelaskan alasan saran serta tindakan yang bisa dilakukan. Penjelasan risiko ditempatkan dekat pilihan investasi yang relevan.

Notifikasi tidak menampilkan nominal. Pesan layar kunci bersifat umum. Tidak ada notifikasi otomatis berulang pada hari yang sama atau pada jam tenang.

## Pemeriksaan sebelum rilis

Periksa tema terang/gelap, ponsel sempit/layar lebar, keyboard terbuka, daftar kosong/panjang, nominal besar, mode sembunyikan, kembali dari sheet/form/detail, klik tab berulang, tombol simpan saat lambat/gagal, serta ikon semua ekspresi. Angka pada Beranda, Kalender, dan Rencana harus mengikuti definisi yang sama.

Pengujian UI simulasi tidak menggantikan pemeriksaan launcher, keyboard, biometrik, dan pengiriman notifikasi pada perangkat fisik. Catat lingkungan pengujian aktual pada catatan rilis.
