package fr.phytok.apps.cachecast.yas

import fr.phytok.apps.cachecast.util.createMapper
import org.junit.Assert
import org.junit.Test

class SearchTest {
    val mapper = createMapper()

    @Test
    fun should_read_json() {
        val inputStream = javaClass.getResourceAsStream("/get-response.json")
        val value = mapper.readValue<Search>(inputStream, Search::class.java)
        Assert.assertNotNull(value)
    }
}