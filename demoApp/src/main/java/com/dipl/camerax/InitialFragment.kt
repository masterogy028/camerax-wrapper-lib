package com.dipl.camerax

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.dipl.camerax.databinding.FragmentInitialBinding
import com.dipl.camerax.utils.ViewBindingFragment

class InitialFragment :
    ViewBindingFragment<FragmentInitialBinding>(bindViewBy = {
        FragmentInitialBinding.inflate(it)
    }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_initialFragment_to_cameraXSummaryFragment)
        }
    }
}
