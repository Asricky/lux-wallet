# Pembaruan Lumi v7

## Perubahan

- Logo Lumi berwarna di notifikasi transaksi, budget, dan pengingat; ikon status monokrom dan label yang jelas. Perilaku izin, deep link, duplikasi dan privasi dipertahankan.
- Saran sementara saat rencana belum aktif: saldo rekening tercatat dikurangi cadangan konservatif, dibagi sampai jadwal pemasukan berikutnya. Tidak mengubah ledger atau snapshot anggaran.
- Laporan bulanan dari snapshot database yang konsisten. Ringkasan, kategori, kalender, evaluasi, rekomendasi, dan daftar transaksi memakai filter arus kas yang eksplisit.
- PdfDocument Android untuk dokumen A4; PdfRenderer untuk preview file yang sama; CreateDocument untuk menyimpan PDF ke lokasi pilihan pengguna.
- Form bulan, opsi nominal, loading/error, pagination, zoom, dan navigasi kembali mengikuti scaffold Lumi.

## Validasi

Pengujian perhitungan meliputi transfer, top-up, koreksi saldo, catatan diabaikan/provisional, tanggal mendatang, zona waktu, Februari kabisat, periode berjalan, hari tanpa rencana, pelampauan budget, cadangan dan saldo negatif. Render A4 native memeriksa 80 transaksi, paginasi, batas teks dan mode privasi. Pengujian UI memeriksa akses menu, periode, checkbox dan kembali; notifikasi memeriksa logo serta publicVersion.

Robolectric 4.13 di Windows tidak menyediakan JNI PdfDocument/PdfRenderer. Tata letak diuji melalui jalur gambar A4 yang sama pada Canvas native, sedangkan pembuatan/preview PDF dan pemilih lokasi penyimpanan akhir perlu pemeriksaan HP fisik. Tidak ada klaim pengujian perangkat fisik.

Rilis menggunakan build-update.ps1, nomor APK berikutnya, README/CHANGELOG/PRD/DESIGN yang diperbarui, identitas instalasi dan sertifikat lama, serta push sebagai Asricky ke Asricky/lumi.

Validasi rilis v7: 151 pengujian lulus (0 gagal/skip); lint 0 error, 14 peringatan, 6 informasi; APK 1.0.7 berlabel Lumi memakai sertifikat yang sama dengan v6. Tidak ada migrasi database. Verifikasi preview PDF dan pemilih lokasi simpan pada HP fisik belum dilakukan.
