package com.dariusepure.autoadvisor.data.auth

import com.dariusepure.autoadvisor.domain.User

data class FirestoreUser(
    val id: String = "",
    val email: String = ""
)

fun User.toFirebase() = FirestoreUser(
    id = this.id,
    email = this.email
)

fun FirestoreUser.fromFirebase() = User(
    id = this.id,
    email = this.email
)

