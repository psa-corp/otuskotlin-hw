import kotlin.test.Test

interface IClass {
}

abstract class BaseClass {

    open fun openMethod() {}
    fun closeMethod() {}
}

class FirstClass : BaseClass() {

    override fun openMethod() {}
}
/**
    Использование @Suppress аннотации:
    Это наиболее распространённый и рекомендуемый метод локального подавления предупреждений.
    Его можно применять к отдельным элементам, таким как классы, функции, свойства или параметры.
    Для общих неиспользованных предупреждений: используйте @Suppress("unused").
    Для неиспользуемых параметров: используйте @Suppress("UNUSED_PARAMETER").
    Для неиспользуемых геттеров/сеттеров свойств: используйте @Suppress("UNUSED_PROPERTY_GETTER")или @Suppress("UNUSED_PROPERTY_SETTER").
    Для предупреждений относительно встроенных функций, которые вряд ли будут полезны: используйте @Suppress("NOTHING_TO_INLINE").
 **/
@Suppress("unused")
class InheritedClass(
    arg: String,
    val prop: String = arg
) : IClass, BaseClass() {
    val x: String = arg

    init {
        println("Init in constructor with $arg")
    }

    fun some() {
        println("Some is called with: ${this.prop}")
    }
}

class BaseTest() {

    @Test
    fun baseTest() {
        val obj = InheritedClass("some")
        obj.some()
    }
}
