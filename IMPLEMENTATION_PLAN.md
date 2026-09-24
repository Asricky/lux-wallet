# Rencana penyempurnaan v5

Audit: aplikasi Compose/NavHost, ledger atomik Room v4, DataStore dan WorkManager. Detail saat ini menyimpan kategori di luar konfirmasi dan tidak menavigasi setelah sukses; perbaiki menjadi satu penyimpanan atomik. Listener sudah berdiri sendiri tetapi pemulihan process/boot belum lengkap. Deduplikasi masih bergantung pada pasangan paket. Kalender memakai PaydayMath dengan snapshot historis; rekomendasi baru harus terpisah dari target tersimpan.

1. Gunakan sembilan wilayah gambar Lumi-app-cover melalui drawable native dan alias launcher; pertahankan alias lama untuk upgrade.
2. Simpan detail atomik, cegah klik ganda, kembali ke Beranda setelah sukses; Food & Drink paling atas; review ringkas dengan tinjau/hapus berkonfirmasi.
3. Tambah arsip aset melalui migrasi v4 ke v5; rekening selalu diarsipkan agar ledger/histori tetap utuh.
4. Pulihkan listener lewat lifecycle service, startup/boot/update dan retry terbatas, bukan polling; status dan diagnostics persisten.
5. Deduplikasi lintas BCA memakai bukti referensi/isi/merchant yang kuat; kandidat ambigu tetap ditinjau tanpa nominal kedua.
6. Kalender ringkasan visual, rekomendasi budget transparan dan grafik periode aktif; ringkas Rencana/Saran Lumi.
7. Regresi navigasi, ledger, dedup, arsip, migrasi, listener, ikon, grafik dan rekomendasi; render, lint, build APK v5, README otomatis dan push.


## Implementasi v5

- Selesai: transaksi atomik dan navigasi setelah commit, kategori prioritas, arsip aset/rekening, migrasi 4→5, parser/deduplikasi BCA lintas aplikasi, pemulihan listener dan diagnostics.
- Selesai: sembilan cover native dan alias launcher, mood aktual terpusat, metrik kalender/planner, rekomendasi adaptif, tren bulanan, Saran Lumi ringkas.
- Verifikasi selesai: 137 pengujian lulus; lint 0 error; build APK v5 berhasil. Rendering native, migrasi v1/v2/v3/v4, dan pemulihan listener tanpa Activity diperiksa. Sertifikat signing sesuai versi sebelumnya.
- Distribusi: app-debug-v5.apk, README otomatis, checksum, signing tetap, lalu push main dengan identitas Asricky.
