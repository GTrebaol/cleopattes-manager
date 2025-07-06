package com.gtreb.cleopattesmanager.ui

import androidx.compose.ui.unit.dp

/**
 * Centralized dimensions for the entire app
 * This file contains all spacing, sizing, and layout dimensions used across the application
 */
object Dimensions {
    
    // ===== LAYOUT SPACING =====
    
    // Top padding for screens (to avoid native UI elements like tabbars)
    const val TOP_PADDING = 60
    
    // Standard padding values
    const val PADDING_XS = 2
    const val PADDING_SM = 4
    const val PADDING_MD = 8
    const val PADDING_LG = 12
    const val PADDING_XL = 16
    const val PADDING_XXL = 24
    const val PADDING_XXXL = 32
    
    // ===== HEADER DIMENSIONS =====
    
    const val HEADER_PADDING = PADDING_MD
    const val HEADER_BUTTON_SPACING = PADDING_SM
    const val HEADER_BUTTON_HORIZONTAL_PADDING = PADDING_LG
    const val HEADER_BUTTON_VERTICAL_PADDING = 6
    
    // ===== PLANNING SCREEN DIMENSIONS =====
    
    const val DAY_VIEW_HORIZONTAL_PADDING = 5
    const val DAY_VIEW_VERTICAL_PADDING = PADDING_XL
    const val TIME_LABEL_WIDTH = 50
    const val TIME_SLOT_ROW_HEIGHT = 50
    const val TIME_SLOT_CARD_HORIZONTAL_PADDING = PADDING_XS
    const val TIME_SLOT_CARD_VERTICAL_PADDING = 0
    const val TIME_SLOT_CARD_INTERNAL_PADDING = 6
    const val TIME_SLOT_CARD_BORDER_WIDTH = 1
    const val TIME_SLOT_CARD_ELEVATION = 2
    const val TIME_SLOT_CARD_CORNER_RADIUS = 0
    
    // Card heights based on duration
    const val SHORT_DURATION_CARD_HEIGHT = 25
    const val MEDIUM_DURATION_CARD_HEIGHT = 50
    const val LONG_DURATION_CARD_HEIGHT = 50
    
    // Duration thresholds (in minutes)
    const val SHORT_DURATION_THRESHOLD = 30
    const val MEDIUM_DURATION_THRESHOLD = 60
    
    // Time range
    const val START_HOUR = 8
    const val END_HOUR = 20
    
    // ===== CARD DIMENSIONS =====
    
    const val CARD_ELEVATION_SM = 2
    const val CARD_ELEVATION_MD = 4
    const val CARD_ELEVATION_LG = 8
    
    const val CARD_CORNER_RADIUS_SM = 4
    const val CARD_CORNER_RADIUS_MD = 8
    const val CARD_CORNER_RADIUS_LG = 12
    
    // ===== BUTTON DIMENSIONS =====
    
    const val BUTTON_HEIGHT_SM = 32
    const val BUTTON_HEIGHT_MD = 40
    const val BUTTON_HEIGHT_LG = 48
    
    const val BUTTON_PADDING_HORIZONTAL_SM = PADDING_MD
    const val BUTTON_PADDING_HORIZONTAL_MD = PADDING_LG
    const val BUTTON_PADDING_HORIZONTAL_LG = PADDING_XL
    
    const val BUTTON_PADDING_VERTICAL_SM = 4
    const val BUTTON_PADDING_VERTICAL_MD = 8
    const val BUTTON_PADDING_VERTICAL_LG = 12
    
    // ===== TEXT FIELD DIMENSIONS =====
    
    const val TEXT_FIELD_HEIGHT = 56
    const val TEXT_FIELD_PADDING_HORIZONTAL = PADDING_MD
    const val TEXT_FIELD_PADDING_VERTICAL = PADDING_SM
    
    // ===== DIALOG DIMENSIONS =====
    
    const val DIALOG_PADDING = PADDING_XL
    const val DIALOG_SPACING = PADDING_MD
    
    // ===== LIST DIMENSIONS =====
    
    const val LIST_ITEM_HEIGHT_SM = 40
    const val LIST_ITEM_HEIGHT_MD = 56
    const val LIST_ITEM_HEIGHT_LG = 72
    
    const val LIST_SPACING_SM = PADDING_XS
    const val LIST_SPACING_MD = PADDING_SM
    const val LIST_SPACING_LG = PADDING_MD
    
    // ===== ICON DIMENSIONS =====
    
    const val ICON_SIZE_SM = 16
    const val ICON_SIZE_MD = 24
    const val ICON_SIZE_LG = 32
    const val ICON_SIZE_XL = 48
    
    // ===== BORDER DIMENSIONS =====
    
    const val BORDER_WIDTH_SM = 1
    const val BORDER_WIDTH_MD = 2
    const val BORDER_WIDTH_LG = 3
    
    // ===== ALPHA VALUES =====
    
    const val ALPHA_DISABLED = 0.38f
    const val ALPHA_MEDIUM = 0.6f
    const val ALPHA_HIGH = 0.8f
    const val ALPHA_OVERLAY = 0.3f
    
    // ===== ANIMATION DURATIONS =====
    
    const val ANIMATION_DURATION_SM = 150
    const val ANIMATION_DURATION_MD = 300
    const val ANIMATION_DURATION_LG = 500
    
    // ===== SHADOW DIMENSIONS =====
    
    const val SHADOW_OFFSET_X = 0
    const val SHADOW_OFFSET_Y = 2
    const val SHADOW_RADIUS = 4
    const val SHADOW_ALPHA = 0.25f
    
    // ===== TEXT CONSTANTS (Temporary - should be replaced with stringResource) =====
    
    const val APP_TITLE = "Cal"
    const val TODAY_BUTTON_TEXT = "Aujourd'hui"
    const val ADD_BUTTON_TEXT = "+"
    const val WEEK_VIEW_TITLE = "Vue Semaine"
    const val MONTH_VIEW_TITLE = "Vue Mois"
    const val TIME_SLOT_DETAILS_TITLE = "Détails du rendez-vous"
    const val SERVICE_LABEL = "Service: "
    const val ANIMAL_LABEL = "Animal: "
    const val CLIENT_LABEL = "Client: "
    const val NOTES_LABEL = "Notes: "
    const val VIEW_ANIMAL_BUTTON_TEXT = "Voir l'animal"
    const val EDIT_BUTTON_TEXT = "Modifier"
    const val DELETE_BUTTON_TEXT = "Supprimer"
    const val CLOSE_BUTTON_TEXT = "Fermer"
    const val FILTERS_TITLE = "Filtres"
    const val ALL_CLIENTS_TEXT = "Tous les clients"
    const val ALL_ANIMALS_TEXT = "Tous les animaux"
    const val ALL_SERVICES_TEXT = "Tous les services"
    const val CLIENT_LABEL_TEXT = "Client"
    const val ANIMAL_LABEL_TEXT = "Animal"
    const val SERVICE_LABEL_TEXT = "Service"
    const val CLEAR_FILTERS_TEXT = "Effacer les filtres"
    const val TIME_SLOT_CARD_ALPHA = 0.8f
}

/**
 * Extension functions for easy dimension access
 */
val Int.dp get() = this.dp
val Float.dp get() = this.dp 