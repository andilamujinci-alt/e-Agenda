package com.example.suratapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.suratapp.databinding.ItemSuratBinding
import com.example.suratapp.data.models.SuratMasuk
import com.example.suratapp.utils.DateUtils

class SuratMasukAdapter(
    private val onItemClick: (SuratMasuk) -> Unit
) : RecyclerView.Adapter<SuratMasukAdapter.ViewHolder>() {

    private var originalList = listOf<SuratMasuk>()
    private var filteredList = listOf<SuratMasuk>()

    fun submitList(list: List<SuratMasuk>) {
        // Urutkan berdasarkan tahun (setelah "/") lalu nomor (sebelum "/"), descending
        originalList = list.sortedWith(compareByDescending<SuratMasuk> { extractYearNumber(it.nomorAgenda) }
            .thenByDescending { extractAgendaNumber(it.nomorAgenda) })
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
        filteredList = filteredList.sortedWith(compareByDescending<SuratMasuk> { extractYearNumber(it.nomorAgenda) }
            .thenByDescending { extractAgendaNumber(it.nomorAgenda) })
        notifyDataSetChanged()
    }

    fun filterByDate(filterType: Int) {
        filteredList = when (filterType) {
            0 -> originalList
            1 -> originalList.filter { it.tanggalDiterima == DateUtils.getTodayDb() }
            2 -> {
                val weekStart = DateUtils.getWeekStartDb()
                originalList.filter { it.tanggalDiterima >= weekStart }
            }
            3 -> {
                val monthStart = DateUtils.getMonthStartDb()
                originalList.filter { it.tanggalDiterima >= monthStart }
            }
            else -> originalList
        }
        filteredList = filteredList.sortedWith(compareByDescending<SuratMasuk> { extractYearNumber(it.nomorAgenda) }
            .thenByDescending { extractAgendaNumber(it.nomorAgenda) })
        notifyDataSetChanged()
    }

    fun filterByStatus(status: String) {
        filteredList = if (status.isEmpty()) {
            originalList
        } else {
            originalList.filter { it.statusSurat.contains(status, ignoreCase = true) }
        }
        filteredList = filteredList.sortedWith(compareByDescending<SuratMasuk> { extractYearNumber(it.nomorAgenda) }
            .thenByDescending { extractAgendaNumber(it.nomorAgenda) })
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

    inner class ViewHolder(private val binding: ItemSuratBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(surat: SuratMasuk) {
            binding.apply {
                tvNomorSurat.text = surat.nomorSurat
                tvPengirim.text = "Dari: ${surat.pengirim}"
                tvNomorAgenda.text = "No. Agenda: ${surat.nomorAgenda}"
                tvPerihal.text = surat.perihal
                tvTanggal.text = DateUtils.formatToDisplay(surat.tanggalDiterima)
                tvStatus.text = surat.statusSurat

                tvBadgeBaru.visibility = if (DateUtils.isToday(surat.tanggalDiterima)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                root.setOnClickListener { onItemClick(surat) }
            }
        }
    }


    private fun extractAgendaNumber(nomorAgenda: String): Int {
        return try {
            nomorAgenda.split("/")[0].toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }


    private fun extractYearNumber(nomorAgenda: String): Int {
        return try {
            nomorAgenda.split("/").getOrNull(1)?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
