package com.dipl.cameraxlib.config

import com.dipl.cameraxlib.MissingMandatoryConfigParameterException

/**
 * A Config is a collection of options and values.
 *
 * <p>Config object hold pairs of Options/Values and offer methods for querying whether
 * Options are contained in the configuration along with methods for fetching the associated
 * values for options.
 *
 */
interface Config {

    val mOptions: Map<Option<*>, Any?>

    /**
     * Returns whether this configuration contains the supplied option.
     *
     * @param option to search for in this configuration.
     * @return `true` if this configuration contains the supplied [option]; `false`
     *  otherwise.
     */
    fun containsOption(option: Option<*>): Boolean

    /**
     * Lists all options contained within this configuration.
     *
     * @return A [List] of [Option]s contained within this configuration.
     */
    fun listOptions(): List<Option<*>>

    /**
     * Inserts an option into configuration.
     *
     * @param [option] to be inserted into configuration with
     * @param [value] of the option as a second parameter.
     *
     * @throws ClassCastException if the value is not of type 'T'.
     */
    @Throws(ClassCastException::class)
    fun <T> insertOption(option: Option<T>, value: Any?)

    /**
     * Gets an option.
     *
     * @param [option] to get it's value from the map.
     *
     * @return A [Set] of [CameraXOption]s contained within this configuration.
     */
    fun <T> getOptionValue(option: Option<T>): T?

    /**
     * Merges the custom configuration with default configuration.
     * When the user shall not provide optional configuration parameters the default ones will be used.
     *
     * @throws MissingMandatoryConfigParameterException in case of mandatory parameter not being set.
     */
    @Throws(MissingMandatoryConfigParameterException::class)
    fun mergeWithDefaults()
}

@Suppress("UNCHECKED_CAST")
abstract class CameraXUseCaseConfig : Config {

    override val mOptions: HashMap<Option<*>, Any?> = HashMap()

    override fun containsOption(option: Option<*>): Boolean {
        return mOptions.containsKey(option)
    }

    override fun listOptions(): List<Option<*>> {
        return mOptions.toList().map { it.first }
    }

    override fun <T> insertOption(option: Option<T>, value: Any?) {
        try {
            mOptions[option] = value as T
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }

    }

    override fun <T> getOptionValue(option: Option<T>): T? = mOptions[option] as T?

    /**
     * Builds default configuration for specific cameraX use case.
     * This is done for providing default configuration for the client.
     *
     * @return the default [Config]
     */
    abstract fun buildDefaultConfig(): Config
}
