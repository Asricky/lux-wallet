# Rencana penyempurnaan v4

Audit: aplikasi Kotlin/Compose, Room v3, DataStore, WorkManager. Ledger dan pencocokan transfer berjalan dalam transaksi Room. Konfirmasi harus dicatat atomik bersama ledger; transfer ditunda selama jendela pencocokan. Migrasi hanya menambah tabel antrean, tanpa mengubah saldo/histori. Navigasi memakai satu NavHost; simpan hanya state tab, buang halaman formulir saat berpindah tab.

1. Fondasi teal, transisi instan, state tab, privasi nominal lintas halaman.
2. Nama profil/onboarding dan sapaan personal.
3. Atlas sembilan ekspresi Lumi dari referensi pemilik, aturan ekspresi dari data aktual.
4. Budget dan insight yang membedakan sisa anggaran dari pemasukan.
5. Kalender nominal arus kas ringkas, rincian budget tetap tersedia.
6. Antrean konfirmasi persisten, filter notifikasi, deep link setelah unlock, feedback nonblocking.
7. Konsistensi CTA, aset, dokumentasi desain dan rilis otomatis README.
8. Uji migrasi, dedup, transfer, privasi, navigasi, tampilan terang/gelap; build APK versi berikutnya dan push.

Status: fase 1–8 diimplementasikan. Regresi ledger, migrasi v1/v2/v3, konfirmasi, dan UI lulus. Rilis memakai build-update.ps1; README dipelihara otomatis. Pengiriman/launcher pada HP fisik masih perlu pemeriksaan perangkat.
