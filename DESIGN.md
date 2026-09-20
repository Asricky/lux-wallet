# Lux Wallet — Panduan desain

Revisi 20 September 2026 · v4. Perhitungan dan perilaku: [PRD.md](PRD.md).

## Arah produk

Aplikasi keuangan personal yang tenang, ringkas, dan ramah. Dahulukan nominal yang mudah dibaca, keadaan uang sebenarnya, serta satu tindakan yang jelas. Pola bank/dompet digital menjadi referensi interaksi familiar; identitas Lux Wallet tetap mengikuti Lumi milik produk.

## Identitas Lumi

<img src="app/src/main/res/drawable-nodpi/lumi_emotions.png" width="540" alt="Sembilan ekspresi Lumi" />

Lumi: burung membulat teal, jambul daun, wajah/perut krem, huruf L dan kaki emas, material lembut tiga dimensi. Referensi asli pemilik: [Lumi-mascot.png](Lumi-mascot.png). Atlas transparan berada di `drawable-nodpi/lumi_emotions.png`, sembilan sel persegi sama besar. Urutan baris: Happy/Proud/Calm, Excited/Curious/Nervous, Shocked/Sad/Angry. Komponen `Lumi` mengambil wilayah atlas saat menggambar; teks kondisi tetap menjadi komponen native.

| Ekspresi | Bukti pemicu |
| --- | --- |
| Happy | Pemakaian budget 0–50% |
| Calm | Belum ada rencana aktif atau penggunaan normal >50–<75% |
| Nervous | Budget 75–100%; angka persen memperjelas kedekatan ke batas |
| Sad | Budget >100–120% |
| Angry | Budget >120% atau kekurangan dana bebas; bahasa tetap suportif |
| Curious | Ada transaksi berstatus perlu ditinjau |
| Shocked | Pengeluaran terbaru >3× rata-rata minimal tiga catatan tujuh hari sebelumnya |
| Excited | Pemasukan nyata baru tercatat, bukan pindah saldo/penyesuaian |
| Proud | Hari yang sudah selesai dengan rencana dan pemakaian <75% |

Risiko budget mendahului ekspresi peristiwa. Tidak mengarang pencapaian/angka. Sisa anggaran tidak dicatat menjadi pendapatan. Maksimal satu Lumi utama per layar; ukuran 64–100 dp untuk pendamping informasi, 140 dp untuk onboarding. Jangan tampilkan avatar kosong atau hukuman visual.

Launcher memakai tiga foreground transparan `lumi_launcher*.png`, latar aqua, safe zone adaptive icon. Alias lama dipertahankan agar upgrade tetap bisa diluncurkan. Mode otomatis memetakan ekspresi ke tenang/senang/fokus saat aplikasi dibuka, bukan mengubah ikon terus-menerus di latar belakang. Nama launcher tetap Lux Wallet. Aset vektor lama di docs/brand adalah arsip v3, bukan acuan desain baru.

## Warna

| Token | Nilai | Peran |
| --- | --- | --- |
| Teal | #16B8B2 | Identitas Lumi/aksen |
| Deep teal | #087A78 | Aksi/teks aktif tema terang dengan kontras memadai |
| Aqua / mint | #DDF8F6 / #EAFBF7 | Container dan latar ikon |
| Gold / light gold | #F5B942 / #FFF2C9 | Aksen kecil, pencapaian |
| Teks | #16324F / #64748B | Primer / sekunder |
| Background / surface | #F7FAFC / #FFFFFF | Tema terang |
| Background / surface gelap | #0D1A22 / #142731 | Tema gelap |
| Primary gelap | #4FD1C5 | Aksi dengan kontras di latar gelap |
| Positive / warning / danger / info | #22C55E / #F59E0B / #EF4444 / #3B82F6 | Arah warna semantik; teks memakai varian lebih gelap/terang agar terbaca |

Gunakan token Theme dan SemanticColors, jangan menaruh warna baru per halaman tanpa alasan. Gradasi hanya pada hero/aksen penting. Grafik, kartu, dan form tetap sederhana. Status harus mempunyai label/simbol selain warna.

## Komponen

- Spasi 4/8/12/16/20/24/32 dp. Margin ponsel 16–20 dp. Kartu radius 20–28 dp, field 16 dp. Target sentuh tindakan minimal 48 dp.
- Tipografi sistem di `Type.kt`. Headline untuk nominal utama, title untuk judul, body untuk keterangan. Hindari banyak bobot/ukuran bersaing.
- CTA navigasi memakai chevron Material 18–24 dp, terpusat vertikal, berjarak dari teks. Jangan memakai panah literal sebagai hiasan tombol.
- Pemilih `ChoiceField` membuka bottom sheet yang bisa digulir; tutup fokus keyboard dahulu, checkmark pilihan aktif.
- Rupiah `MoneyField`: digit integer dalam model, titik ribuan visual, kursor/hapus/paste tetap benar. Tampilan laporan memakai `privateRupiah` atau `visibleAmount`.
- Mode hide memakai `********` rapat, menutup nominal, proporsi grafik, dan warna finansial kalender. Input serta ringkasan draft tetap terbaca karena pengguna sedang memverifikasi data sebelum simpan.
- Simpan hanya menutup form setelah database berhasil. Draft dipertahankan saat gagal. Back pada ringkasan kembali mengedit; keluar draft yang berubah meminta pilihan lanjut/buang.
- Lonceng kecil Beranda menunjukkan koneksi listener, bukan izin mengirim konfirmasi. Animasi lembut; label aksesibilitas harus tetap menjelaskan status tanpa animasi.

## Navigasi dan performa

Empat tab: Beranda, Kalender, Aset, Lainnya. Bilah bawah di ponsel; rail pada lebar ≥600 dp. Tombol catat adalah aksi, bukan tab kelima. Halaman detail punya app bar dan panah kembali; toolbar dan Back sistem memakai dispatcher yang sama.

`openScreen` menyimpan state akar tab (`saveState`, `restoreState`, `launchSingleTop`) setelah membuang halaman detail. Tab memulihkan pilihan dan posisi gulir, tidak membuka formulir lama. NavHost tidak memakai fade. State finansial berasal dari Flow/ViewModel, bukan salinan lokal yang dimuat ulang setiap klik. Kalender mengelompokkan transaksi sekali untuk seluruh bulan.

Referensi implementasi: [Android multiple back stacks](https://developer.android.com/guide/navigation/backstack/multi-back-stacks).

## Kalender dan budget

Grid Senin–Minggu. Setiap tanggal menunjukkan income minus expense dalam format bertanda +100K/−45K/+1.25M; nol netral. Dua desimal maksimum, tanpa pembulatan yang membesarkan angka. Detail menunjukkan rupiah lengkap. Pada layar sempit label dibuat ringkas satu baris, tanggal tetap dominan.

Budget ditampilkan terpisah: batas, pemakaian, sisa, persen dan progress bar (visual dibatasi 100%, angka boleh lebih). Hari ini masih sementara, hari mendatang belum aktual, hari tanpa rencana tidak disebut surplus. Transportasi/tagihan memakai cadangannya lebih dahulu; saldo perkiraan gaji tidak menjadi dana yang bisa dibelanjakan.

## Notifikasi dan bahasa

Bahasa Indonesia singkat dan tidak menghakimi: “Yuk, susun ulang belanja hari ini”, “Ada transaksi yang perlu kamu cek”. Sertakan alasan nyata, jangan menjanjikan hasil investasi.

Konfirmasi memakai channel Lux Wallet Transactions dan ID logis transaksi. Antrean persisten mencegah duplikat/replay. Transfer ditunda saat menunggu pasangan; pembatasan Android dapat menambah jeda. Satu snackbar singkat tanpa modal, dengan aksi Lihat ketika aplikasi aktif. Pesan layar kunci selalu umum; nominal hanya ada pada notifikasi privat jika mode hide tidak aktif. Pengingat harian edukasi terpisah, opt-in, maksimal sekali sehari pada jam siang.

## Pemeriksaan dan rilis

Periksa terang/gelap, layar 320/393 dp, angka panjang, daftar kosong, pilihan tab setelah kembali, keyboard/sheet, privasi, serta kondisi Lumi. Test otomatis meliputi matematika, parser, ledger, migrasi, notifikasi dan UI. Simulasi tidak menggantikan pemeriksaan launcher/biometrik/notifikasi pada HP fisik.

Setiap update harus menyertakan CHANGELOG dan menjalankan `build-update.ps1`. README wajib selalu memuat link langsung APK versi terbaru, checksum, dan perubahannya; script mengisi blok rilis secara otomatis. Aturan ini tetap berlaku pada pembaruan berikutnya tanpa permintaan ulang pemilik.
