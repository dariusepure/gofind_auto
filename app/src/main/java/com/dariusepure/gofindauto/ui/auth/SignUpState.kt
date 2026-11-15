package com.dariusepure.gofindauto.ui.auth

sealed interface SignUpState {
    data object Idle : SignUpState
    data object Pending : SignUpState
    data class Error(val message: String) : SignUpState
}

