package com.dipl.cameraxlib.config

abstract class Option<T>(
    private val optionId: String,
    private val valueClass: Class<T>,
    private val mandatory: Boolean,
) : Comparable<Option<T>> {

    /**
     * Returns whether this option is mandatory.
     *
     * @return 'true' if the [Option] is mandatory; 'false' otherwise.
     */
    fun isMandatory(): Boolean = mandatory

    /**
     * Returns the unique string identifier for this option.
     *
     * This generally follows the scheme
     * 'ognjenbogicevic.useCase.parameterName'
     *
     * @return The identifier.
     */
    fun getOptionId(): String = optionId

    /**
     * Returns the class object associated with the value for this option.
     *
     * @return The class object for the value's type.
     */
    fun getValueClass(): Class<T> = valueClass

    /**
     * Compares the two options via their hashcode value.
     *
     * @return 0 if the hashcodes are equal; positive or negative number otherwise.
     * This is because the Map only needs to checks the equality of the options.
     */
    override fun compareTo(other: Option<T>): Int {
        return optionId.compareTo(optionId)
    }
}

class CameraXOption<T>(optionId: String, valueClass: Class<T>, mandatory: Boolean = true) :
    Option<T>(optionId, valueClass, mandatory) {

    companion object {
        /**
         * Creates an [Option] from an id and value class.
         *
         * @param id         A unique string identifier for this option.
         * @param [valueClass] The class of the value stored by this option.
         * @param <T>        The type of the value stored by this option.
         * @return An [CameraXOption] object which can be used to store/retrieve values from a [ ].
        </T> */
        fun <T> create(id: String, valueClass: Class<T>): Option<T> {
            return CameraXOption(id, valueClass)
        }

        /**
         * Creates an [Option] from an [id], [valueClass] and sets the option as non mandatory (optional).
         *
         * @param id         A unique string identifier for this option.
         * @param [valueClass] The class of the value stored by this option.
         * @param <T>        The type of the value stored by this option.
         * @return An [CameraXOption] object which can be used to store/retrieve values from a [ ].
        </T> */
        fun <T> createNonMandatory(id: String, valueClass: Class<T>): Option<T> {
            return CameraXOption(id, valueClass, false)
        }
    }
}
