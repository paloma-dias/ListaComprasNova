package com.example.listacomprasnova.model

data class Lista(
    val id: Long = 0L,
    val nome: String,
    val usuarioId: Long,
    var imagemUri: String? = null
)