// SuratKeluarAdapter.kt - Update sama seperti SuratMasukAdapter
package com.example.suratapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.suratapp.databinding.ItemSuratBinding
import com.example.suratapp.data.models.SuratKeluar
import com.example.suratapp.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class SuratKeluarAdapter(
    private val onItemClick: (SuratKeluar) -> Unit
) : RecyclerView.Adapter<SuratKeluarAdapter.ViewHolder>() {

    private var originalList = listOf<SuratKeluar>()
    private var filteredList = listOf<SuratKeluar>()

    fun submitList(list: List<SuratKeluar>) {
        // Sort by tanggal_diterima descending (terbaru di atas)
        originalList = list.sortedByDescending { it.tanggalDiterima }
        filteredList = originalList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { surat ->
                surat.pengirim.contains(query, ignoreCase = true) ||
                        surat.nomorSurat.contains(query, ignoreCase = true) ||
                        surat.nomorAgenda.contains(query, ignoreCase = true) ||
                        surat.perihal.contains(query, ignoreCase = true) ||
                        surat.statusSurat.contains(query, ignoreCase = true)
            }
        }
        // Maintain descending order setelah filter
        filteredList = filteredList.sortedByDescending { it.tanggalDiterima }
        notifyDataSetChanged()
    }

    // SuratMasukAdapter.kt & SuratKeluarAdapter.kt - filterByDate tetap sama
    fun filterByDate(filterType: Int) {
        filteredList = when (filterType) {
            0 -> originalList // Semua
            1 -> originalList.filter { it.tanggalDiterima == DateUtils.getTodayDb() } // Hari Ini
            2 -> { // Minggu Ini
                val weekStart = DateUtils.getWeekStartDb()
                originalList.filter { it.tanggalDiterima >= weekStart }
            }
            3 -> { // Bulan Ini
                val monthStart = DateUtils.getMonthStartDb()
                originalList.filter { it.tanggalDiterima >= monthStart }
            }
            else -> originalList
        }
        // Maintain descending order setelah filter
        filteredList = filteredList.sortedByDescending { it.tanggalDiterima }
        notifyDataSetChanged()
    }

    fun filterByStatus(status: String) {
        filteredList = if (status.isEmpty()) {
            originalList
        } else {
            originalList.filter { it.statusSurat.contains(status, ignoreCase = true) }
        }
        // Maintain descending order setelah filter
        filteredList = filteredList.sortedByDescending { it.tanggalDiterima }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuratBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    // SuratKeluarAdapter.kt - Update bind()
    // SuratKeluarAdapter.kt - ViewHolder bind()
    inner class ViewHolder(private val binding: ItemSuratBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(surat: SuratKeluar) {
            binding.apply {
                tvNomorSurat.text = surat.nomorSurat
                // Label "Ke" untuk surat keluar
                tvPengirim.text = "Ke: ${surat.pengirim}"
                tvNomorAgenda.text = "No. Agenda: ${surat.nomorAgenda}"
                tvPerihal.text = surat.perihal
                tvTanggal.text = DateUtils.formatToDisplay(surat.tanggalDiterima)
                tvStatus.text = surat.statusSurat

                // Show badge "BARU" jika surat hari ini
                tvBadgeBaru.visibility = if (DateUtils.isToday(surat.tanggalDiterima)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                root.setOnClickListener {
                    onItemClick(surat)
                }
            }
        }
    }
}