package com.example.listacomprasnova.data

import android.util.Log
import com.example.listacomprasnova.model.Categoria
import com.example.listacomprasnova.model.Item
import com.example.listacomprasnova.model.Lista
import com.example.listacomprasnova.model.Usuario

object DataStore {
    private val usuarios = mutableListOf<Usuario>()
    private val listas = mutableListOf<Lista>()
    private val itens = mutableListOf<Item>()

    private var nextUserId = 1L
    private var nextListaId = 1L
    private var nextItemId = 1L

    init {
        Log.d("DataStore", "DataStore inicializado. Usuários: ${usuarios.size}")

        val defaultUser = Usuario(nextUserId++, "Usuário Teste", "teste@teste.com", "1234")
        usuarios.add(defaultUser)

        val listaSuper = Lista(nextListaId++, "Supermercado", defaultUser.id)
        val listaFesta = Lista(nextListaId++, "Festa de Aniversário", defaultUser.id)
        listas.add(listaSuper)
        listas.add(listaFesta)

        itens.add(Item(nextItemId++, listaSuper.id, "Banana", 1.0, "cacho", Categoria.FRUTA, false))
        itens.add(Item(nextItemId++, listaSuper.id, "Cebola", 0.5, "kg", Categoria.LEGUME, false))
        itens.add(Item(nextItemId++, listaSuper.id, "Frango", 2.0, "kg", Categoria.CARNE, true))
        itens.add(Item(nextItemId++, listaSuper.id, "Queijo", 250.0, "g", Categoria.OUTROS, false))
    }



    fun findUser(email: String, senha: String): Usuario? {
        return usuarios.find { it.email == email && it.senha == senha }
    }

    fun insertUsuario(usuario: Usuario): Long {
        val novoUsuario = usuario.copy(id = nextUserId++)
        usuarios.add(novoUsuario)
        return novoUsuario.id
    }

    fun clearDataForLogout() {
        // listas.clear()
        // itens.clear()
    }

    fun insertLista(lista: Lista): Long {
        val newId = nextListaId++
        listas.add(lista.copy(id = newId))
        return newId
    }

    fun getAllListas(usuarioId: Long): List<Lista> {
        return listas.filter { it.usuarioId == usuarioId }.sortedBy { it.nome }
    }

    fun updateLista(listaAtualizada: Lista) {
        val index = listas.indexOfFirst { it.id == listaAtualizada.id }
        if (index != -1) {
            listas[index] = listaAtualizada
        }
    }

    fun deleteLista(listaId: Long) {
        listas.removeAll { it.id == listaId }
        itens.removeAll { it.listaId == listaId }
    }

    fun insertItem(item: Item): Long {
        val newId = nextItemId++
        itens.add(item.copy(id = newId))
        return newId
    }

    fun getAllItens(listaId: Long): List<Item> {
        return itens.filter { it.listaId == listaId }
    }

    fun updateItem(itemAtualizado: Item) {
        val index = itens.indexOfFirst { it.id == itemAtualizado.id }
        if (index != -1) {
            itens[index] = itemAtualizado
        }
    }

    fun deleteItem(itemId: Long) {
        itens.removeAll { it.id == itemId }
    }
}