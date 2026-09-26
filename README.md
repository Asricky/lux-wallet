<div align="center">
  <img src="docs/brand/lumi-icon.png" width="128" alt="Ikon Lumi" />
  <h1>Lumi</h1>
  <p><strong>Kenali uangmu. Rencanakan hari esok.</strong></p>
  <p>Catatan keuangan pribadi dari notifikasi bank dan dompet digital.<br />Diproses di perangkat, bisa digunakan offline, tanpa login rekening.</p>
  <p>
    <a href="#download-aplikasi">Download APK</a> ·
    <a href="docs/USER_GUIDE.md">Panduan penggunaan</a> ·
    <a href="CHANGELOG.md">Riwayat pembaruan</a>
  </p>
</div>

---

<!-- LATEST_RELEASE_START -->
## Download aplikasi

[**Download Lumi - app-debug-v7.apk**](https://github.com/Asricky/lumi/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v7.apk)

**v7 / 1.0.7** &nbsp; | &nbsp; Android 10+ &nbsp; | &nbsp; [Semua versi](app/build/outputs/apk/debug)

### Yang baru

- Notifikasi transaksi, budget, dan pengingat menampilkan logo Lumi berwarna, aksen teal, serta judul yang jelas. Privasi layar kunci tetap dijaga.
- Saran belanja sementara tersedia di Beranda, Rencana, dan Saran Lumi saat rencana belum ada atau berakhir. Perhitungan memakai saldo rekening tercatat, menahan cadangan, dan tidak menambahkan perkiraan gaji.
- Menu **Laporan bulanan** di Lainnya dan Kalender: preview PDF, perbesar halaman, navigasi halaman, dan simpan file melalui pemilih lokasi Android.
- PDF A4 berisi ringkasan masuk/keluar, kategori, kalender, evaluasi budget, rekomendasi bulan berikutnya, dan daftar transaksi. Lumi memberi teguran berdasarkan pelampauan budget; data belum ditinjau ditandai sementara.
- Preview dan unduhan memakai snapshot PDF yang sama. Nominal disamarkan secara default dan bisa disertakan secara eksplisit.
- Validasi: 151 pengujian lulus; lint 0 error, 14 peringatan. Tata letak A4 dan navigasi diuji secara otomatis; preview PDF serta pemilih lokasi simpan belum diverifikasi pada HP fisik.

<details>
<summary>Lokasi file &amp; verifikasi unduhan</summary>

File: `app/build/outputs/apk/debug/app-debug-v7.apk`

SHA-256:

```text
6AC4D3EF9C52AE03A3F21409E2CD8ECC1DF166EF1B6F81C8E2A096C8CE3F0081
```

</details>
<!-- LATEST_RELEASE_END -->

## Uangmu, lebih mudah dipahami

| Fitur | Yang bisa kamu lakukan |
| --- | --- |
| **Catatan otomatis** | Membaca notifikasi BCA mobile/myBCA, SeaBank, ShopeePay, dan GoPay, dengan pemeriksaan duplikat. |
| **Tinjauan praktis** | Baca pesan langsung, pilih beberapa catatan atau semuanya, lalu hapus sekaligus dengan satu konfirmasi. |
| **Laporan bulanan PDF** | Preview laporan berdesain, evaluasi Lumi, rekomendasi bulan berikutnya, lalu download ke lokasi pilihanmu. |
| **Kalender keuangan** | Lihat pemasukan, pengeluaran, selisih harian, dan tren pengeluaran per bulan. |
| **Rencana sampai gajian** | Pisahkan kebutuhan wajib dan belanja; saran sementara tetap tersedia saat rencana belum diperbarui. |
| **Aset & kalkulator** | Koreksi saldo, kelola aset, arsipkan rekening, dan simulasikan rencana uang. |
| **Temani hari bersama Lumi** | Sembilan ekspresi mengikuti kondisi keuangan, dengan tema terang/gelap dan pilihan privasi nominal. |

## Mulai dalam beberapa langkah

1. Unduh APK terbaru di atas, lalu pasang pada **Android 10 atau lebih baru**.
2. Isi rekening dan saldo awal sesuai keadaan sebenarnya.
3. Buka **Pengaturan → Pemantauan notifikasi**, lalu berikan akses notifikasi.
4. Pastikan notifikasi transaksi aplikasi bank aktif. Tinjau catatan yang belum dikenali sebelum menggunakannya sebagai acuan.

**Sudah memakai versi sebelumnya?** Pasang sebagai pembaruan tanpa uninstall. Nama aplikasi menjadi **Lumi**, dengan identitas instalasi dan sertifikat yang tetap sama agar data tersimpan.

## Buat laporan bulanan

Buka **Lainnya → Laporan bulanan** atau **Kalender → Laporan bulanan & PDF**, pilih bulan, lalu **Buat preview PDF**. Gunakan pengatur perbesaran dan tombol halaman untuk membaca. Tekan **Simpan / download PDF** dan pilih folder, misalnya Download.

Nominal disamarkan secara default. Centang **Sertakan nominal dalam PDF** bila ingin laporan lengkap. Preview dan file unduhan sama; bulan berjalan ditandai sementara. Laporan mencakup catatan yang tersimpan di Lumi, bukan rekening koran bank.

## Privasi dan kendali

Data finansial diproses lokal. Lumi tidak meminta kata sandi bank, memindahkan uang, atau membeli investasi. Nominal bisa disembunyikan, dan kunci biometrik tersedia.

Saldo adalah estimasi dari saldo awal dan catatan yang diterima. Rekomendasi budget tidak mengubah anggaranmu otomatis. Android dapat membatasi pemantauan latar belakang; setelah **Paksa berhenti**, buka aplikasi lagi. [Panduan pemantauan, cadangan, dan pemecahan masalah →](docs/USER_GUIDE.md)

## Pengembangan

Kotlin · Jetpack Compose · Room · DataStore · WorkManager

```powershell
# Uji, periksa lint, buat APK bernomor berikutnya, dan perbarui README
.\build-update.ps1 -Offline
```

Setiap rilis menyertakan catatan di `CHANGELOG.md`. Skrip memperbarui **tautan download terbaru, nomor versi, dan checksum** secara otomatis setelah build berhasil. APK tersedia di `app/build/outputs/apk/debug/` dengan urutan `app-debug-v1.apk`, `app-debug-v2.apk`, dan seterusnya.

| Dokumentasi | Isi |
| --- | --- |
| [Panduan penggunaan](docs/USER_GUIDE.md) | Instalasi, izin, perhitungan, privasi, cadangan, dan build |
| [PRD](PRD.md) | Perilaku produk dan aturan finansial |
| [DESIGN](DESIGN.md) | Identitas Lumi serta konsistensi tampilan |
| [CHANGELOG](CHANGELOG.md) | Catatan setiap versi |

Pengujian otomatis mencakup parser, ledger, migrasi, navigasi, pemantauan, dan rendering UI. Hasil rilis terbaru tercantum di changelog; pengujian otomatis tidak menggantikan pemeriksaan pada HP fisik.
