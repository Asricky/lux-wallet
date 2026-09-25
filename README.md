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

[**Download Lumi - app-debug-v6.apk**](https://github.com/Asricky/lumi/raw/refs/heads/main/app/build/outputs/apk/debug/app-debug-v6.apk)

**v6 / 1.0.6** &nbsp; | &nbsp; Android 10+ &nbsp; | &nbsp; [Semua versi](app/build/outputs/apk/debug)

### Yang baru

- Nama aplikasi, launcher, notifikasi, dan ekspor menjadi **Lumi**; data dan kompatibilitas pembaruan tetap dijaga.
- Pesan notifikasi langsung terlihat pada tinjauan, termasuk format yang belum dikenali. Pesan panjang bisa diperluas; mode privasi menyamarkan digit.
- Pilih beberapa catatan atau **Pilih semua**, lalu **Hapus terpilih** dengan satu konfirmasi. Penghapusan atomik menjaga saldo dan histori.
- README baru lebih ringkas, dengan tombol unduh utama, catatan versi, dan panduan terpisah. Repositori memakai nama **Asricky/lumi**.
- Validasi: 141 pengujian lulus; lint tanpa error (13 peringatan). APK tetap memakai identitas dan sertifikat pembaruan yang sama. Belum diuji pada HP fisik.

<details>
<summary>Lokasi file &amp; verifikasi unduhan</summary>

File: `app/build/outputs/apk/debug/app-debug-v6.apk`

SHA-256:

```text
0AF4E226A4751A233FC895B6E8F842AC062F0D0CE60F8863D8CE53D717BC9F02
```

</details>
<!-- LATEST_RELEASE_END -->

## Uangmu, lebih mudah dipahami

| Fitur | Yang bisa kamu lakukan |
| --- | --- |
| **Catatan otomatis** | Membaca notifikasi BCA mobile/myBCA, SeaBank, ShopeePay, dan GoPay, dengan pemeriksaan duplikat. |
| **Tinjauan praktis** | Baca pesan langsung, pilih beberapa catatan atau semuanya, lalu hapus sekaligus dengan satu konfirmasi. |
| **Kalender keuangan** | Lihat pemasukan, pengeluaran, selisih harian, dan tren pengeluaran per bulan. |
| **Rencana sampai gajian** | Pisahkan tagihan, transportasi, tabungan, investasi, dan belanja harian. |
| **Aset & kalkulator** | Koreksi saldo, kelola aset, arsipkan rekening, dan simulasikan rencana uang. |
| **Temani hari bersama Lumi** | Sembilan ekspresi mengikuti kondisi keuangan, dengan tema terang/gelap dan pilihan privasi nominal. |

## Mulai dalam beberapa langkah

1. Unduh APK terbaru di atas, lalu pasang pada **Android 10 atau lebih baru**.
2. Isi rekening dan saldo awal sesuai keadaan sebenarnya.
3. Buka **Pengaturan → Pemantauan notifikasi**, lalu berikan akses notifikasi.
4. Pastikan notifikasi transaksi aplikasi bank aktif. Tinjau catatan yang belum dikenali sebelum menggunakannya sebagai acuan.

**Sudah memakai versi sebelumnya?** Pasang sebagai pembaruan tanpa uninstall. Nama aplikasi menjadi **Lumi**, dengan identitas instalasi dan sertifikat yang tetap sama agar data tersimpan.

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
