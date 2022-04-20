package com.dipl.camerax.utils

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Base class of activities with implementation of viewBinding feature.
 * @constructor used to proceed lambda function of [bindViewBy] type in order to inflate and bind view by generated ViewBinding specific class
 * @param [T] used to declare specific ViewBinding class for implemented activity as viewBinding type. Example: Activity<ActivityMainBinding> for activity_main.xml
 * */
open class Activity<T : ViewBinding>(private val bindViewBy: bindViewBy<T>) : AppCompatActivity() {

    /**
     * Reference to viewBinding object of type [T] used to get views inside subclasses.
     * */
    protected lateinit var activityBinding: T

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutInflater = LayoutInflater.from(this)
        activityBinding = bindViewBy(layoutInflater)
        setContentView(activityBinding.root)
    }
}

/**
 * Read only activity property for getting viewBinding instance. This implementation can be used by calling viewBinding(viewBounder: (LayoutInflater) -> ViewBinding)
 * @constructor used to proceed viewBinder function with return type of ViewBinding subtype
 * */
class ActivityViewBindingProperty<T : ViewBinding>(private val viewBinder: bindViewBy<T>) :
    ReadOnlyProperty<Activity, T> {

    // used to cache viewBinding instance
    private var viewBinding: T? = null

    override fun getValue(activity: Activity, property: KProperty<*>): T {
        viewBinding?.let { return it }
        viewBinding = viewBinder(LayoutInflater.from(activity))
        return viewBinding!!
    }
}

/**
 * Function used to get viewBinding reference inside an activity. Example: val viewBinding by viewBinding { ActivityMainBinding.inflate(it) }
 * */
fun <T : ViewBinding> activityViewBinding(viewBounder: bindViewBy<T>): ActivityViewBindingProperty<T> {
    return ActivityViewBindingProperty(viewBounder)
}
