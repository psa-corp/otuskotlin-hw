import kotlin.test.Test

class ObjectsExample {
    /**
        companion object (объект-компаньон)
        Назначение: Определяет члены, которые принадлежат самому классу, а не его экземплярам.
        Это похоже на статические поля и методы в Java.
        Использование: Позволяет вызывать функции и получать доступ к свойствам непосредственно через имя класса,
        что удобно для фабричных методов или общих утилитных функций.
    **/
    companion object {
        init {
            println("companion inited") // init when ObjectsExample will be loaded
        }

        fun doSmth() {
            println("companion object")
        }
    }
    /**
        object (объект-одиночка)
        Назначение: Создает единственный экземпляр класса, который никогда не будет меняться и может быть доступен из любого места в программе.
        Использование: Подходит для случаев, когда нужно реализовать паттерн "одиночка"
        (singleton) или когда есть набор функций и свойств, которые должны быть доступны глобально, без привязки к конкретному классу.
    **/
    object A {
        init {
            println("A inited") // lazy init whet getting A first time
        }

        fun doSmth() {
            println("object A")
        }
    }
}

class ObjectsTest {
    @Test
    fun test() {
        ObjectsExample()
        ObjectsExample.doSmth()
        ObjectsExample.A.doSmth()
        ObjectsExample.A.doSmth()
    }
}