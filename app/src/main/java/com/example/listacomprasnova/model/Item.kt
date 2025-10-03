package com.example.listacomprasnova.model

data class Item(
    val id: Long,
    val listaId: Long,
    var nome: String,
    var quantidade: Double,
    var unidade: String,
    var categoria: Categoria,
    var comprado: Boolean = false
)