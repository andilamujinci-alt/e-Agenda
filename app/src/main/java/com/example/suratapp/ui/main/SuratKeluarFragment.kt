package com.example.suratapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.suratapp.databinding.FragmentSuratKeluarBinding
import com.example.suratapp.ui.detail.DetailSuratActivity

class SuratKeluarFragment : Fragment() {

    private var _binding: FragmentSuratKeluarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    private lateinit var adapter: SuratKeluarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuratKeluarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilter()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SuratKeluarAdapter { surat ->
            val intent = Intent(requireContext(), DetailSuratActivity::class.java)
            intent.putExtra("SURAT_KELUAR", surat)
            intent.putExtra("TYPE", "keluar")
            startActivity(intent)
        }

        binding.rvSuratKeluar.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuratKeluar.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupFilter() {
        val filterOptions = arrayOf("Semua", "Hari Ini", "Minggu Ini", "Bulan Ini")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterOptions)
        binding.spinnerFilter.adapter = filterAdapter

        binding.spinnerFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapter.filterByDate(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Filter by status
        val statusOptions = arrayOf("Semua Status", "Sub Bagian Umum", "Sekretaris", "Kepala", "Ekonomi", "Sarpras", "Sosbud", "Litbang", "Program", "Keuangan", "Penerima")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.spinnerStatus.adapter = statusAdapter

        binding.spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = if (position == 0) "" else statusOptions[position]
                adapter.filterByStatus(status)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.suratKeluarList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}