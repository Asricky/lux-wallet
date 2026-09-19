package com.luxwallet.app.engine

import java.time.LocalDate

data class CoachAdvice(val title: String, val message: String, val action: String)
object MoneyCoach {
    fun advise(status: PaydayStatus?, today: LocalDate): CoachAdvice = when {
        status == null -> CoachAdvice("Mulai dari uang yang tersedia", "Konfirmasi saldo likuid dan tagihan sampai pemasukan berikutnya. Pisahkan kebutuhan harian, transportasi, dan dana yang ingin dilindungi.", "Buat rencana sampai gajian")
        status.expired -> CoachAdvice("Sudah ada pemasukan baru?", "Periode rencanamu berakhir. Catat pemasukan hanya jika sudah diterima, lalu konfirmasi saldo dan jadwal berikutnya. Jika terlambat, geser tanggal dan sesuaikan alokasi.", "Perbarui rencana")
        status.freeRemaining < 0 -> CoachAdvice("Alokasi perlu disesuaikan", "Dana bebas belum menutup rencana. Dahulukan makan, transportasi, dan tagihan; tunda bagian investasi yang belum terjangkau. Hindari berutang untuk mengejar target investasi.", "Tinjau alokasi")
        status.remainingToday < 0 -> CoachAdvice("Budget hari ini sudah terlampaui", "Periksa transaksi hari ini, termasuk calon duplikat. Tunda belanja yang masih bisa menunggu dan tinjau sisa dana sampai gajian.", "Lihat kalender")
        today.plusDays(1) == status.plan.end -> CoachAdvice("Siapkan rencana setelah gajian", "Besok adalah jadwal pemasukan. Cek uang yang benar-benar masuk sebelum membaginya untuk tagihan, bisnis, investasi, dan kebutuhan harian.", "Buka rencana")
        today.dayOfWeek.value % 2 == 0 -> CoachAdvice("Lindungi dana untuk kebutuhan dekat", "Dana kebutuhan sampai gajian dan modal bisnis yang segera dipakai sebaiknya tetap mudah dicairkan. Jangan mengejar hasil investasi dengan dana kebutuhan rutin.", "Lihat pilihan investasi")
        else -> CoachAdvice("Tinjau investasi sesuai waktunya", "Jika kebutuhan dekat dan dana darurat sudah cukup, pertimbangkan instrumen sesuai tujuan: SBN dengan tenor yang cocok, atau reksa dana indeks untuk tujuan panjang dan siap menghadapi penurunan nilai. Baca risiko dan biaya dahulu.", "Lihat pilihan investasi")
    }
    fun canNotify(today: LocalDate, lastDate: String?, hour: Int): Boolean = lastDate != today.toString() && hour in 9..20
}
