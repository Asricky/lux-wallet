# Pembaruan Lumi v6

## Cakupan

- Nama publik Lumi untuk aplikasi, launcher, notifikasi, ekspor, dan repository Asricky/lumi.
- Application ID, sertifikat, database, namespace, ID channel serta alias lama dipertahankan agar upgrade menjaga data.
- Pesan asli langsung terlihat dalam tinjauan; digit disamarkan mengikuti mode privasi.
- Checkbox per kartu, pilih semua/batal pilih, hapus terpilih, satu konfirmasi batch.
- Hapus atomik, pengecekan ulang status, ledger hanya untuk transaksi terkait, dan rollback seluruh batch saat gagal.
- README ringkas, hero ikon native, tautan APK v6, panduan terpisah, dan updater rilis yang mempertahankan struktur.

## Validasi dan distribusi

Pengujian mencakup pesan/privasi, pilihan campuran transaksi dan notifikasi gagal, pembatalan, snapshot pilihan ketika notifikasi baru masuk, idempotensi, rollback ledger, serta label aplikasi dan package upgrade. Seluruh regresi, lint, dan APK bernomor diselesaikan sebelum rename repo serta push sebagai Asricky. Berkas lokal dan kredensial tidak disertakan.

Hasil v6: 141 pengujian lulus (0 gagal), lint 0 error dan 13 peringatan, APK 1.0.6 berlabel Lumi. Tinjauan diverifikasi melalui rendering native pada lebar 320 dp dan 393 dp. Sertifikat APK cocok dengan versi sebelumnya. Pengujian pada HP fisik belum dilakukan.
