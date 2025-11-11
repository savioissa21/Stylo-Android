package com.example.styloandroid.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.styloandroid.R
import com.example.styloandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
