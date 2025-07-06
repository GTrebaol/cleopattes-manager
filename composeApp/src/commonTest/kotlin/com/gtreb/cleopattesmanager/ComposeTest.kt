package com.gtreb.cleopattesmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeTest {

    @Test
    fun simpleCheck() {
        // Test basic Compose state functionality
        var txt by mutableStateOf("Go")
        
        assertEquals("Go", txt)
        
        txt += "."
        assertEquals("Go.", txt)
        
        txt += "."
        assertEquals("Go..", txt)
        
        txt += "."
        assertEquals("Go...", txt)
    }
}