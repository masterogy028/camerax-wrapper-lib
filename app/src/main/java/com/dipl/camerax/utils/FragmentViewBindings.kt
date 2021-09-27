package com.dipl.camerax.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Version of androidx library's [androidx.fragment.app.Fragment] used to bind fragment's view using ViewBinding feature.
 * It also controls the visibility of bottom navigation view via [bottomNavVisibility] parameter that is given through one
 * of its subclasses.
 * @constructor used to proceed lambda function of [bindViewBy] type in order to inflate and bind view by generated ViewBinding specific class
 * @param [T] used to declare specific ViewBinding class for implemented fragment as viewBinding type. Example: Fragment<FragmentMainBinding> for fragment_main.xml
 * */
abstract class ViewBindingFragment<T : ViewBinding>(
    private val bindViewBy: bindViewBy<T>
) :
    androidx.fragment.app.Fragment() {

    private var binding: T? = null

    /**
     * Reference to viewBinding object of type [T] used to get views inside subclasses.
     * This reference can be used inside fragment's view lifecycle.
     * @throws IllegalAccessException if fragment's view does not exist.
     * */
    protected val viewBinding: T
        get() = binding ?: throw IllegalAccessException("viewBinding is null")

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = bindViewBy.invoke(inflater)
        return binding?.root
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        MainHandler.post { binding = null }
    }
}

/**
 * Read only fragment property for getting viewBinding instance. This implementation can be used by calling viewBinding(() -> ViewBinding)
 * @constructor used to proceed viewBinder function with return type of ViewBinding subtype
 * NOTE: this implementation also can be used inside subtypes of [androidx.fragment.app.DialogFragment]
 * */
class FragmentViewBindingProperty<T : ViewBinding>(private val viewBinder: () -> T) :
    ReadOnlyProperty<Fragment, T> {

    // used to cache viewBinding instance
    private var viewBinding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        viewBinding?.let { return it }

        val viewBinding = viewBinder()
        // viewBinding instance has to be set to null if view is destroyed to prevent memory leaks. The implementation uses viewLifecycleOwner to ph.com.globe.globeonesuperapp.register observer
        thisRef.viewLifecycleOwner.lifecycle.addObserver(
            OnDestroyViewObserver {
                // Observer's destroy method is triggered while fragment's performDestroyView() method is running. Since fragment view is still not null
                // while performDestroyView() method is running the implementation needs to post setting reference to null on main handler.
                // In this way viewBinding will be not null until fragment onDestroyView() method is not finished.
                MainHandler.post { this@FragmentViewBindingProperty.viewBinding = null }
            }
        )
        this.viewBinding = viewBinding
        return viewBinding
    }
}

/**
 * Function used to get viewBinding reference inside a fragment. Example: val viewBinding by viewBinding { FragmentMainBinding.bind(requireView()) }
 * */
fun <T : ViewBinding> viewBinding(viewBounder: () -> T): FragmentViewBindingProperty<T> {
    return FragmentViewBindingProperty(viewBounder)
}
