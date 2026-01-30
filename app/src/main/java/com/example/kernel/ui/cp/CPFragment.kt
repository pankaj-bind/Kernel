package com.example.kernel.ui.cp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kernel.databinding.FragmentCpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CPFragment : Fragment() {

    private var _binding: FragmentCpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CPViewModel by viewModels()
    private lateinit var contestAdapter: ContestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupToolbar()
        setupRetryButton()
        observeUiState()
    }

    private fun setupRecyclerView() {
        contestAdapter = ContestAdapter()
        binding.recyclerViewContests.apply {
            adapter = contestAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                com.example.kernel.R.id.action_refresh -> {
                    viewModel.refresh()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRetryButton() {
        binding.buttonRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = false

            when (state) {
                is CPUiState.Loading -> showLoading()
                is CPUiState.Success -> showContests(state)
                is CPUiState.Empty -> showEmpty()
                is CPUiState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        with(binding) {
            progressBar.isVisible = true
            recyclerViewContests.isVisible = false
            emptyState.isVisible = false
            errorState.isVisible = false
        }
    }

    private fun showContests(state: CPUiState.Success) {
        with(binding) {
            progressBar.isVisible = false
            recyclerViewContests.isVisible = true
            emptyState.isVisible = false
            errorState.isVisible = false
            contestAdapter.submitList(state.contests)
        }
    }

    private fun showEmpty() {
        with(binding) {
            progressBar.isVisible = false
            recyclerViewContests.isVisible = false
            emptyState.isVisible = true
            errorState.isVisible = false
        }
    }

    private fun showError(message: String) {
        with(binding) {
            progressBar.isVisible = false
            recyclerViewContests.isVisible = false
            emptyState.isVisible = false
            errorState.isVisible = true
            textErrorMessage.text = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
