package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// ACHROMATIC LIQUID GLASS DESIGN SYSTEM (iOS 26 / visionOS Inspired)
// Core Principle:
// - THE GLASS ITSELF IS PURE ACHROMATIC (pure white/neutral translucency, never tinted)
// - All visual richness and color comes from the VIVID, SATURATED BACKDROP shining through
// - Semantic colors (Success, Warning, Error) are the only color accents in content
// - Text and icons remain pure high-contrast neutral white/slate
// ============================================================================

// 1. Rich, Saturated Vivid Backdrop Scene (No saturation cap)
val VividBackdropBase = Color(0xFF060814)        // Deep rich midnight space
val VividIndigoPool = Color(0xFF4338CA)          // Vivid saturated deep indigo
val VividRoyalBluePool = Color(0xFF1D4ED8)       // Vivid saturated royal blue
val VividVioletPool = Color(0xFF7C3AED)          // Vivid saturated electric violet
val VividMagentaPool = Color(0xFFC026D3)         // Vivid saturated magenta
val VividCyanPool = Color(0xFF0284C7)            // Vivid saturated electric cyan

// Aliases for compatibility
val DuskDeepIndigo = VividBackdropBase
val DuskSlateBlue = VividIndigoPool
val DuskMutedViolet = VividVioletPool
val DuskAtmosphere = VividRoyalBluePool
val MutedMidnightBase = VividBackdropBase
val SlateIndigoPool = VividIndigoPool
val MutedVioletPool = VividVioletPool
val DeepCyanPool = VividCyanPool

// 2. Achromatic Liquid Glass Translucent Surfaces (Pure Neutral White - 0xFFFFFF)
// Clear, low-opacity white washes allowing the vivid background to shine through cleanly
val GlassFillSubtle = Color(0x0FFFFFFF)          // ~6% alpha pure white (deep background pane)
val GlassFill = Color(0x18FFFFFF)                // ~9.5% alpha pure white (back layer cards)
val GlassFillElevated = Color(0x26FFFFFF)        // ~15% alpha pure white (front layer cards/pills)
val GlassFillActive = Color(0x36FFFFFF)          // ~21% alpha pure white (pressed/active buttons)
val GlassFillHero = Color(0x28FFFFFF)            // ~16% alpha pure white (hero actions)

// 3. Crisp Refractive Rim Light and Specular Edges
val GlassBorderSubtle = Color(0x24FFFFFF)        // ~14% alpha crisp border
val GlassBorder = Color(0x45FFFFFF)              // ~27% alpha refractive border
val GlassBorderBright = Color(0x80FFFFFF)        // ~50% alpha specular rim light
val GlassSpecular = Color(0x59FFFFFF)            // ~35% alpha dynamic highlight sweep
val GlassDropShadow = Color(0x4D000000)          // Clean ambient shadow beneath floating glass

// Frosted Top/Bottom Bar Background (Pure neutral frosted glass)
val FrostedBarBackground = Color(0x1C0A0E18)     // Subtle neutral dark-wash with pure glass rim

// 4. Semantic Colors (ONLY applied where functional meaning exists)
val SuccessGreen = Color(0xFF34D399)             // Emerald / Correct / Stable
val SuccessGreenBg = Color(0x2934D399)           // Translucent green glass
val SuccessGreenBorder = Color(0x6634D399)       // Specular green edge

val WarningAmber = Color(0xFFFBBF24)             // Warm Amber / Flagged / Review
val WarningAmberBg = Color(0x29FBBF24)           // Translucent amber glass
val WarningAmberBorder = Color(0x66FBBF24)       // Specular amber edge

val ErrorRed = Color(0xFFF87171)                 // Bright Rose / Wrong / Delete
val ErrorRedBg = Color(0x29F87171)               // Translucent red glass
val ErrorRedBorder = Color(0x66F87171)           // Specular red edge

// 5. High-Contrast Achromatic Text Tones (Pure Crisp White & Neutral Slate)
val TextPrimary = Color(0xFFFFFFFF)              // 100% brightness crisp white
val TextSecondary = Color(0xD9FFFFFF)            // 85% brightness clear white
val TextMuted = Color(0x99FFFFFF)                // 60% brightness neutral white

// 6. Backwards-Compatibility Aliases
val MidnightDark = VividBackdropBase
val MidnightSurface = GlassFill
val MidnightCard = GlassFillSubtle
val MidnightCardElevated = GlassFillElevated
val MidnightBorder = GlassBorder

val IndigoAccent = Color(0xFFFFFFFF)             // Pure crisp white highlight
val ElectricBlue = Color(0xFFFFFFFF)             // Pure crisp white highlight
val PurpleAccent = Color(0xFFE2E8F0)             // Clean neutral slate
val DeepIndigo = VividIndigoPool
