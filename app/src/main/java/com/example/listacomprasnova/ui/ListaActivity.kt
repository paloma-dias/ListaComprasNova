package com.example.listacomprasnova.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listacomprasnova.R
import com.example.listacomprasnova.data.DataStore
import com.example.listacomprasnova.databinding.ActivityListaBinding
import com.example.listacomprasnova.model.Lista
import com.example.listacomprasnova.ui.adapter.ListaAdapter
import android.view.Window
import android.view.WindowManager

class ListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaBinding
    private lateinit var adapter: ListaAdapter
    private var usuarioId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)
        binding = ActivityListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioId = intent.getLongExtra("usuario_id", 0L)

        if (usuarioId == 0L) {
            Toast.makeText(this, "Erro: Usuário inválido. Faça o login novamente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        carregarListas()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Suas Listas"
    }

    private fun setupRecyclerView() {
        adapter = ListaAdapter(
            listas = emptyList(),
            onItemClick = { lista -> abrirItens(lista) },
            onEditClick = { lista -> criarOuEditarLista(lista) },
            onDeleteClick = { lista -> deletarLista(lista) }
        )
        binding.recyclerViewListas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewListas.adapter = adapter
    }

    private fun setupFab() {
        binding.fabNovaLista.setOnClickListener {
            criarOuEditarLista(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lista, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                carregarListas(query.orEmpty())
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                carregarListas(newText.orEmpty())
                return true
            }
        })

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                carregarListas()
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Toast.makeText(this, "Logout realizado.", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun carregarListas(query: String = "") {
        val todasListas = DataStore.getAllListas(usuarioId)

        val listasFiltradas = if (query.isEmpty()) {
            todasListas
        } else {
            todasListas.filter {
                it.nome.contains(query, ignoreCase = true)
            }
        }

        adapter.updateListas(listasFiltradas)

        val isEmpty = listasFiltradas.isEmpty()
        binding.recyclerViewListas.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun criarOuEditarLista(lista: Lista?) {
        val isEditing = lista != null
        val titulo = if (isEditing) "Editar Lista" else "Nova Lista"

        val input = EditText(this).apply {
            hint = "Nome da lista"
            if (isEditing) setText(lista?.nome)
        }

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = input.text.toString().trim()

                if (nome.isEmpty()) {
                    Toast.makeText(this, "Nome da lista é obrigatório.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isEditing && lista != null) {
                    // Edição
                    val listaAtualizada = lista.copy(nome = nome)
                    DataStore.updateLista(listaAtualizada)
                    Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show()
                } else {
                    val novaLista = Lista(nome = nome, usuarioId = usuarioId)
                    DataStore.insertLista(novaLista)
                    Toast.makeText(this, "Lista criada!", Toast.LENGTH_SHORT).show()
                }

                carregarListas()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun deletarLista(lista: Lista) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Lista")
            .setMessage("Confirmar exclusão de \"${lista.nome}\"? Todos os itens associados serão excluídos.")
            .setPositiveButton("Sim") { _, _ ->
                DataStore.deleteLista(lista.id)
                carregarListas()
                Toast.makeText(this, "Lista excluída!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun abrirItens(lista: Lista) {
        val intent = Intent(this, ItensActivity::class.java).apply {
            putExtra("lista_id", lista.id)
            putExtra("lista_nome", lista.nome)
        }
        startActivity(intent)
    }
}