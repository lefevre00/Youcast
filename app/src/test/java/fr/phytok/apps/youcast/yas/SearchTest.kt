package fr.phytok.apps.youcast.yas

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert
import org.junit.Test

class SearchTest {
    val mapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerKotlinModule()
    }

    @Test
    fun should_read_json() {
        val inputStream = javaClass.getResourceAsStream("/get-response.json")
        val value = mapper.readValue<Search>(inputStream, Search::class.java)
        Assert.assertNotNull(value)
    }
}