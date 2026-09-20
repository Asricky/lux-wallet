# Changelog

## 1.0.4 — 2026-09-20

- Tampilan teal/mint, sapaan nama dari profil, sembilan ekspresi Lumi, dan ikon launcher baru dari referensi Lumi.
- Tab menyimpan posisi/pilihan tanpa fade dan tanpa memulihkan formulir lama; CTA memakai chevron yang konsisten.
- Nominal tersembunyi memakai `********` pada saldo, aset, arus kas, transaksi, target, dan hasil simulasi.
- Kalender menampilkan arus kas harian bertanda, misalnya +100K atau −1.25M, terpisah dari hasil terhadap budget.
- Konfirmasi transaksi Android dengan antrean persisten, filter per jenis, tautan detail, snackbar, serta peringatan budget.
- Migrasi Room v4 mempertahankan ledger dan saldo; transfer memakai satu konfirmasi. Pembaruan notifikasi setelah transfer digabung tetap melekat pada transaksi yang sama.
- README diperbarui otomatis oleh proses rilis: link unduh terbaru, versi, checksum APK, dan catatan perubahan dari changelog.

## 1.0.3 — 2026-09-19

- Perbaikan navigasi akar menu, panah kembali, alur draft/ringkasan, fokus keyboard, dan bilah adaptif empat menu.
- Pemisah ribuan otomatis pada input rupiah; onboarding empat langkah menyimpan rekening pada tahap akhir.
- Rencana saldo sampai tanggal pemasukan, cadangan tagihan/transportasi, dan kalender surplus/minus budget historis.
- Lumi: maskot penguin hitam–emas, tiga ekspresi, ikon yang dapat dipilih, saran keuangan dan pengingat lokal berizin.
- Migrasi Room v3 dan snapshot kalender dalam cadangan; PRD dan panduan desain baru.
- APK bernomor v3; signature dan application ID tetap untuk update di atas v2.


## 1.0.2 — 2026-09-19

- Beranda ringkas, indikator notifikasi berkedip, riwayat transaksi, dan sembunyikan nominal.
- Pemilih berbentuk bottom sheet, pilihan Light/Dark/Sistem, serta form transaksi dua langkah.
- Edit nominal aset dan koreksi saldo rekening dengan ledger penyesuaian.
- Kalkulator aritmetika dan simulasi pertumbuhan dengan setoran bulanan.
- Cadangan transportasi terpisah dari belanja bebas; target bisnis Rp1 juta dan investasi Rp1,5 juta.
- Identitas kejadian notifikasi, penahanan calon duplikat myBCA, dan peninjauan catatan ganda lama.
- Migrasi database v1 ke v2 tanpa penghapusan data.

## 1.0.1 — 2026-09-19

- Identitas visual dompet hitam dengan aksen emas dan dashboard baru.
- Perbaikan sumber SeaBank, pemulihan listener, dropdown, dan transaksi manual.
- Penulisan ledger atomik, retry, perlindungan saldo awal, dan deduplikasi konservatif.
- Peninjauan format notifikasi yang tidak dikenali dan diagnostik pemetaan paket.
- Perbaikan pembaruan dashboard, perhitungan nominal, penyimpanan detail, dan kunci aplikasi.
- Distribusi APK bernomor melalui folder app/build/outputs/apk/debug.
