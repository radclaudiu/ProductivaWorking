package com.productiva.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Formas redondeadas para componentes de interfaz
val Shapes = Shapes(
    // Componentes pequeños (botones, chips, etc.)
    small = RoundedCornerShape(4.dp),
    
    // Componentes medianos (tarjetas, campos de texto, etc.)
    medium = RoundedCornerShape(8.dp),
    
    // Componentes grandes (diálogos, hojas modales, etc.)
    large = RoundedCornerShape(12.dp),
    
    // Componentes extra grandes
    extraLarge = RoundedCornerShape(16.dp)
)