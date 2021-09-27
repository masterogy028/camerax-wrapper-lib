package com.dipl.camerax

import android.os.Bundle
import com.dipl.camerax.databinding.ActivityBaseBinding
import com.dipl.camerax.utils.Activity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaseActivity : Activity<ActivityBaseBinding>(bindViewBy = { ActivityBaseBinding.inflate(it) })
