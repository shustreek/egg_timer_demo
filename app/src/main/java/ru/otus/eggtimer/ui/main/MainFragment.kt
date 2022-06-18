package ru.otus.eggtimer.ui.main

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collect
import ru.otus.eggtimer.R
import ru.otus.eggtimer.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var binding: MainFragmentBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val fd = requireContext().assets.openFd("alarm.wav")
            viewModel.initSound(fd)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // === Vibration ===
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        viewModel.initVibrator(vibrator)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            bottomNavigation.setOnItemSelectedListener { item ->
                viewModel.onItemSelected(item.itemId)
                true
            }
            startBtn.setOnClickListener { viewModel.onStartBtnClick() }
        }

        viewLifecycleOwner.lifecycleScope
            .launchWhenStarted {
                viewModel.timerState
                    .collect { state ->
                        with(binding) {
                            when (state) {
                                is TimerState.Default -> {
                                    message.text = state.time
                                    startBtn.setText(R.string.start)
                                    bottomNavigation.setItemsEnable(true)
                                }
                                is TimerState.Running -> {
                                    message.text = state.time
                                    startBtn.setText(R.string.stop)
                                    bottomNavigation.setItemsEnable(false)
                                }
                                TimerState.Done -> {
                                    message.setText(R.string.done)
                                    startBtn.setText(R.string.start)
                                    bottomNavigation.setItemsEnable(true)
                                }
                            }
                        }
                    }
            }
    }

    private fun BottomNavigationView.setItemsEnable(enable: Boolean) {
        for (i in 0 until menu.size()) {
            menu[i].isEnabled = enable
        }
    }
}