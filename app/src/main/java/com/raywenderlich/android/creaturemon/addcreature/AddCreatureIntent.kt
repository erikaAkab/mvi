package com.raywenderlich.android.creaturemon.addcreature

import com.raywenderlich.android.creaturemon.mvibase.MviIntent

sealed class AddCreatureIntent : MviIntent {
    data class AvatarIntent(val drawable: Int) : AddCreatureIntent()
    data class NameIntent(val name: String) : AddCreatureIntent()
    data class IntelligenceIntent(val inteligenceIndex: Int) : AddCreatureIntent()
    data class EnduranceIntent(val enduranceIndex: Int) : AddCreatureIntent()
    data class StrengthIntent(val strengthIndex: Int) : AddCreatureIntent()
    data class SaveIntent(
            val drawable: Int,
            val name: String,
            val inteligenceIndex: Int,
            val strengthIndex: Int,
            val enduranceIndex: Int) : AddCreatureIntent()
}