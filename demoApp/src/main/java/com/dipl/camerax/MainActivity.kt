package com.dipl.camerax

import com.dipl.camerax.databinding.ActivityMainBinding
import com.dipl.camerax.utils.Activity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : Activity<ActivityMainBinding>(bindViewBy = { ActivityMainBinding.inflate(it) })
