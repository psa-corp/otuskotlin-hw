import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Ignore
import kotlin.test.Test

/*
* Реализовать функцию, которая преобразует список словарей строк в ФИО
* Функцию сделать с использованием разных функций для разного числа составляющих имени
* Итого, должно получиться 4 функции
*
* Для успешного решения задания, требуется раскомментировать тест, тест должен выполняться успешно
* */
class HomeWork1Test {

    @Test
    fun mapListToNamesTest() {
        val input = listOf(
            mapOf(
                "first" to "Иван",
                "middle" to "Васильевич",
                "last" to "Рюрикович",
            ),
            mapOf(
                "first" to "Петька",
            ),
            mapOf(
                "first" to "Сергей",
                "last" to "Королев",
            ),
        )
        val expected = listOf(
            "Рюрикович Иван Васильевич",
            "Петька",
            "Королев Сергей",
        )
        val res = mapListToNames(input)
        assertEquals(expected, res)

    }

    private fun mapListToNames(input: List<Map<String, String>>): List<String> {
        return input.
        map {
            listOf(getValue("last", it), getValue("first", it), getValue("middle", it))
                .filter {i -> i.isNotBlank() }
                .joinToString(" ")
        }.toList()
    }

    private fun getValue(key: String, map: Map<String, String>): String {
        return map.getOrDefault(key, "")
    }
}