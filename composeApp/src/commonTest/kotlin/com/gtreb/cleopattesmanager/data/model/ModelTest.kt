package com.gtreb.cleopattesmanager.data.model

import kotlinx.serialization.json.Json
import kotlin.test.*

class ModelTest {
    
    @Test
    fun testClientSerialization() {
        val client = Client(
            id = generateClientId(),
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "123456789",
            address = "123 Main St",
            postalCode = "75001",
            city = "Paris",
            createdAt = System.currentTimeMillis(),
            notes = "Loyal client"
        )
        
        val json = Json.encodeToString(Client.serializer(), client)
        val deserializedClient = Json.decodeFromString(Client.serializer(), json)
        
        assertEquals(client, deserializedClient)
        assertEquals("John Doe", client.fullName())
    }
    
    @Test
    fun testAnimalSerialization() {
        val animal = Animal(
            id = generateAnimalId(),
            name = "Rex",
            speciesId = SpeciesIds.DOG,
            breed = "Golden Retriever",
            birthDate = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L), // 1 year
            weight = 25.5,
            color = "Golden",
            clientId = generateClientId(),
            notes = "Very sociable"
        )
        
        val json = Json.encodeToString(Animal.serializer(), animal)
        val deserializedAnimal = Json.decodeFromString(Animal.serializer(), json)
        
        assertEquals(animal, deserializedAnimal)
        assertEquals("Rex (Dog)", animal.fullName())
    }
    
    @Test
    fun testServiceSerialization() {
        val service = Service(
            id = generateServiceId(),
            name = "Walk",
            description = "30-minute walk",
            durationMinutes = 30,
            price = 15.0
        )
        
        val json = Json.encodeToString(Service.serializer(), service)
        val deserializedService = Json.decodeFromString(Service.serializer(), json)
        
        assertEquals(service, deserializedService)
    }
    
    @Test
    fun testPrestationSerialization() {
        val prestation = Prestation(
            id = generatePrestationId(),
            clientId = generateClientId(),
            animalId = generateAnimalId(),
            serviceId = generateServiceId(),
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + (30 * 60 * 1000L), // +30 minutes
            price = 15.0,
            status = PrestationStatus.PLANNED,
            notes = "First service"
        )
        
        val json = Json.encodeToString(Prestation.serializer(), prestation)
        val deserializedPrestation = Json.decodeFromString(Prestation.serializer(), json)
        
        assertEquals(prestation, deserializedPrestation)
    }
    
    @Test
    fun testIdGenerator() {
        val id1 = IdGenerator.generateId()
        val id2 = IdGenerator.generateId()
        
        assertNotNull(id1)
        assertNotNull(id2)
        assertEquals(36, id1.length) // UUID length
        assertEquals(36, id2.length)
        assert(id1 != id2) // IDs should be unique
    }
    
    @Test
    fun testTypedIdGeneration() {
        val clientId = generateClientId()
        val animalId = generateAnimalId()
        val serviceId = generateServiceId()
        val prestationId = generatePrestationId()
        
        assertNotNull(clientId.value)
        assertNotNull(animalId.value)
        assertNotNull(serviceId.value)
        assertNotNull(prestationId.value)

        
        // Verify they are valid UUIDs
        assertTrue(clientId.isValid())
        assertTrue(animalId.isValid())
        assertTrue(serviceId.isValid())
        assertTrue(prestationId.isValid())
    }
    
    @Test
    fun testAndroidBestPracticesCompliance() {
        // Test that our IDs follow Android best practices
        val clientId = generateClientId()
        
        // 1. Should be resettable (UUID format)
        assertTrue(clientId.isValid(), "ID should be in valid UUID format")
        
        // 2. Should not contain hardware identifiers
        assertTrue(!clientId.value.contains("android_id"), "Should not contain hardware identifiers")
        assertTrue(!clientId.value.contains("imei"), "Should not contain hardware identifiers")
        assertTrue(!clientId.value.contains("mac"), "Should not contain hardware identifiers")
        
        // 3. Should be app-scoped (UUID is app-scoped)
        assertTrue(clientId.value.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }
    
    @Test
    fun testIdValidation() {
        // Test valid UUID
        val validUuid = "550e8400-e29b-41d4-a716-446655440000"
        val clientId = createClientId(validUuid)
        assertTrue(clientId.isValid())
        
        // Test invalid UUID
        assertFailsWith<IllegalArgumentException> {
            createClientId("invalid-uuid")
        }
        
        assertFailsWith<IllegalArgumentException> {
            createClientId("123")
        }
    }
    
    @Test
    fun testGenericIdSystem() {
        // Test that all ID types work with the generic system
        val clientId = generateClientId()
        val animalId = generateAnimalId()
        val serviceId = generateServiceId()
        val prestationId = generatePrestationId()
        
        // All should be valid UUIDs
        assertTrue(clientId.isValid())
        assertTrue(animalId.isValid())
        assertTrue(serviceId.isValid())

        assertTrue(prestationId.isValid())

    }
    
    @Test
    fun testSpeciesTranslations() {
        // Test species translations
        assertEquals("Dog", getLocalizedSpeciesName(SpeciesIds.DOG, "en"))
        assertEquals("Chien", getLocalizedSpeciesName(SpeciesIds.DOG, "fr"))
        assertEquals("Perro", getLocalizedSpeciesName(SpeciesIds.DOG, "es"))
        
        assertEquals("Cat", getLocalizedSpeciesName(SpeciesIds.CAT, "en"))
        assertEquals("Chat", getLocalizedSpeciesName(SpeciesIds.CAT, "fr"))
        assertEquals("Gato", getLocalizedSpeciesName(SpeciesIds.CAT, "es"))
        
        // Test fallback to ID for unknown species
        assertEquals("unknown_species", getLocalizedSpeciesName("unknown_species", "en"))
    }
    
    @Test
    fun testAnimalLocalization() {
        val animal = Animal(
            id = generateAnimalId(),
            name = "Rex",
            speciesId = SpeciesIds.DOG,
            clientId = generateClientId()
        )
        
        // Test localized species names
        assertEquals("Dog", animal.getSpeciesName("en"))
        assertEquals("Chien", animal.getSpeciesName("fr"))
        assertEquals("Perro", animal.getSpeciesName("es"))
        
        // Test localized full names
        assertEquals("Rex (Dog)", animal.fullName("en"))
        assertEquals("Rex (Chien)", animal.fullName("fr"))
        assertEquals("Rex (Perro)", animal.fullName("es"))
    }
    
    @Test
    fun testSpeciesIds() {
        assertEquals(6, SpeciesIds.ALL_SPECIES_IDS.size)
        assertTrue(SpeciesIds.ALL_SPECIES_IDS.contains(SpeciesIds.DOG))
        assertTrue(SpeciesIds.ALL_SPECIES_IDS.contains(SpeciesIds.CAT))
        assertTrue(SpeciesIds.ALL_SPECIES_IDS.contains(SpeciesIds.BIRD))
        assertTrue(SpeciesIds.ALL_SPECIES_IDS.contains(SpeciesIds.RODENT))
        assertTrue(SpeciesIds.ALL_SPECIES_IDS.contains(SpeciesIds.REPTILE))
        assertTrue(SpeciesIds.ALL_SPECIES_IDS.contains(SpeciesIds.OTHER))
    }
} 