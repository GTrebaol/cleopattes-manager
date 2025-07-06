# ViewModels Documentation

Cette documentation décrit l'architecture des ViewModels de l'application Cleopattes Manager.

## Architecture Générale

### BaseViewModel
Classe de base abstraite qui fournit :
- Gestion d'état commune (loading, error)
- Méthodes utilitaires pour les coroutines
- Structure unifiée pour tous les ViewModels

```kotlin
abstract class BaseViewModel<State, Event> : ViewModel()
```

## ViewModels Principaux

### 1. ClientViewModel
Gère les opérations liées aux clients.

**Fonctionnalités :**
- CRUD des clients
- Recherche et filtrage
- Sélection de clients
- Gestion des erreurs

**États :**
- `clients`: Liste de tous les clients
- `selectedClient`: Client sélectionné
- `searchQuery`: Requête de recherche
- `filteredClients`: Clients filtrés

**Événements :**
- `LoadClients`, `AddClient`, `UpdateClient`, `DeleteClient`
- `SearchClients`, `SelectClient`, `DeselectClient`
- `ClearSearch`

### 2. AnimalViewModel
Gère les opérations liées aux animaux avec support de l'internationalisation.

**Fonctionnalités :**
- CRUD des animaux
- Recherche avec localisation des espèces
- Filtrage par client
- Support multi-langues

**États :**
- `animals`: Liste de tous les animaux
- `selectedAnimal`: Animal sélectionné
- `currentLanguage`: Langue actuelle
- `filteredAnimals`: Animaux filtrés

**Événements :**
- `LoadAnimals`, `AddAnimal`, `UpdateAnimal`, `DeleteAnimal`
- `SearchAnimals`, `FilterByClient`
- `SetLanguage`

### 3. ServiceViewModel
Gère les opérations liées aux services.

**Fonctionnalités :**
- CRUD des services
- Filtrage par catégorie et prix
- Activation/désactivation des services
- Gestion des prix

**États :**
- `services`: Liste de tous les services
- `selectedService`: Service sélectionné
- `categoryFilter`: Filtre par catégorie
- `priceRangeFilter`: Filtre par fourchette de prix

**Événements :**
- `LoadServices`, `AddService`, `UpdateService`, `DeleteService`
- `FilterByCategory`, `FilterByPriceRange`
- `ToggleServiceActive`

### 4. PrestationViewModel
Gère les opérations liées aux prestations.

**Fonctionnalités :**
- CRUD des prestations
- Filtrage par date, statut, client, animal
- Calcul des prix totaux
- Statistiques du tableau de bord

**États :**
- `prestations`: Liste de toutes les prestations
- `selectedPrestation`: Prestation sélectionnée
- `dateRangeFilter`: Filtre par plage de dates
- `statusFilter`: Filtre par statut

**Événements :**
- `LoadPrestations`, `AddPrestation`, `UpdatePrestation`, `DeletePrestation`
- `FilterByDateRange`, `FilterByStatus`, `FilterByClient`, `FilterByAnimal`
- `UpdatePrestationStatus`, `CalculateTotalPrice`

### 5. DashboardViewModel
Gère le tableau de bord avec toutes les statistiques.

**Fonctionnalités :**
- Statistiques agrégées
- Prestations récentes et à venir
- Top clients et services
- Export de données

**États :**
- `dashboardData`: Données du tableau de bord
- `selectedDateRange`: Plage de dates sélectionnée
- `recentPrestations`: Prestations récentes
- `upcomingPrestations`: Prestations à venir

**Événements :**
- `LoadDashboardData`, `SetDateRange`, `RefreshData`
- `ExportData`, `ShowClientDetails`, `ShowServiceDetails`

### 6. SearchViewModel
Gère la recherche globale multi-entités.

**Fonctionnalités :**
- Recherche dans tous les types d'entités
- Filtres de recherche
- Suggestions automatiques
- Historique des recherches

**États :**
- `searchQuery`: Requête de recherche
- `searchResults`: Résultats de recherche
- `selectedFilters`: Filtres appliqués
- `recentSearches`: Historique des recherches

**Événements :**
- `Search`, `ClearSearch`, `SetFilters`
- `SelectResult`, `LoadSuggestions`

### 7. SettingsViewModel
Gère les paramètres de l'application.

**Fonctionnalités :**
- Paramètres de langue et devise
- Sauvegarde et restauration
- Synchronisation
- Export/import de données

**États :**
- `settings`: Paramètres de l'application
- `backupStatus`: Statut de sauvegarde
- `syncStatus`: Statut de synchronisation

**Événements :**
- `UpdateLanguage`, `UpdateCurrency`, `UpdateDateFormat`
- `CreateBackup`, `RestoreBackup`, `ExportData`, `ImportData`
- `SyncData`, `ClearCache`, `ResetApp`

## Patterns Utilisés

### 1. State Management
- Utilisation de `StateFlow` pour la réactivité
- États immutables avec `data class`
- Séparation claire entre état et événements

### 2. Event-Driven Architecture
- Événements encapsulés dans des `sealed class`
- Gestion centralisée des événements
- Traçabilité des actions utilisateur

### 3. Repository Pattern
- Injection de dépendance des repositories
- Abstraction de la couche de données
- Testabilité améliorée

### 4. Coroutines et Flow
- Opérations asynchrones avec `launchWithLoading`
- Gestion automatique des erreurs
- Annulation automatique des coroutines

## Utilisation

### Création d'un ViewModel
```kotlin
class MyViewModel(
    private val repository: AppRepository
) : BaseViewModel<MyViewModel.State, MyViewModel.Event>() {
    
    override fun createInitialState(): State = State()
    
    override fun handleEvent(event: Event) {
        when (event) {
            is Event.LoadData -> loadData()
            is Event.UpdateData -> updateData(event.data)
        }
    }
    
    data class State(val data: String = "")
    sealed class Event {
        object LoadData : Event()
        data class UpdateData(val data: String) : Event()
    }
}
```

### Observation des états
```kotlin
// Dans un Composable
val viewModel = remember { MyViewModel(repository) }
val state by viewModel.uiState.collectAsState()
val isLoading by viewModel.isLoading.collectAsState()
val errorMessage by viewModel.errorMessage.collectAsState()

// Envoi d'événements
viewModel.handleEvent(MyViewModel.Event.LoadData)
```

## Tests

Tous les ViewModels sont testés avec :
- Tests unitaires pour chaque opération
- Mocks des repositories
- Validation des états et événements
- Tests d'intégration pour les flux de données

## Bonnes Pratiques

1. **Séparation des responsabilités** : Chaque ViewModel gère une entité spécifique
2. **Immutabilité** : Les états sont immutables
3. **Réactivité** : Utilisation de Flow pour les mises à jour automatiques
4. **Gestion d'erreurs** : Centralisée dans BaseViewModel
5. **Testabilité** : Architecture facilitant les tests unitaires
6. **Performance** : Utilisation efficace des coroutines et Flow 