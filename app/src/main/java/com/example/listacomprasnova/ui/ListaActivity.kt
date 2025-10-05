package com.example.listacomprasnova.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listacomprasnova.R
import com.example.listacomprasnova.data.DataStore
import com.example.listacomprasnova.databinding.ActivityListaBinding
import com.example.listacomprasnova.databinding.DialogListaCrudBinding
import com.example.listacomprasnova.model.Lista
import com.example.listacomprasnova.ui.adapter.ListaAdapter

class ListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaBinding
    private lateinit var adapter: ListaAdapter
    private var usuarioId: Long = 0L
    private var tempListImageUri: String? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                tempListImageUri = uri.toString()
                Toast.makeText(this, "Foto selecionada! Clique em Salvar.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioId = intent.getLongExtra("usuario_id", 0L)

        if (usuarioId == 0L) {
            Toast.makeText(this, "Erro: Usuário inválido.", Toast.LENGTH_SHORT).show()
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
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "Logout efetuado. Dados descartados.", Toast.LENGTH_SHORT).show()
    }

    private fun carregarListas(query: String = "") {
        val todasListas = DataStore.getAllListas(usuarioId)
        val listasFiltradas = if (query.isEmpty()) {
            todasListas.sortedBy { it.nome }
        } else {
            todasListas
                .filter { it.nome.contains(query, ignoreCase = true) }
                .sortedBy { it.nome }
        }

        adapter.updateListas(listasFiltradas)

        val isEmpty = listasFiltradas.isEmpty()
        binding.recyclerViewListas.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = ListaAdapter(
            listas = emptyList(),
            onItemClick = { lista ->
                val intent = Intent(this, ItensActivity::class.java).apply {
                    putExtra("lista_id", lista.id)
                    putExtra("lista_nome", lista.nome)
                }
                startActivity(intent)
            },
            onEditClick = { lista ->
                abrirDialogCriarLista(lista)
            },
            onDeleteClick = { lista ->
                deletarLista(lista)
            }
        )
        binding.recyclerViewListas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewListas.adapter = adapter
    }

    private fun setupFab() {
        binding.fabNovaLista.setOnClickListener {
            abrirDialogCriarLista(null)
        }
    }


    private fun deletarLista(lista: Lista) {
        val numItens = DataStore.getAllItens(lista.id).size
        val mensagem = if (numItens > 0) {
            "Confirmar exclusão da lista \"${lista.nome}\"?\n${numItens} itens também serão excluídos."
        } else {
            "Confirmar exclusão da lista \"${lista.nome}\"?"
        }

        AlertDialog.Builder(this)
            .setTitle("Excluir Lista")
            .setMessage(mensagem)
            .setPositiveButton("Sim") { _, _ ->
                DataStore.deleteLista(lista.id)
                carregarListas()
                Toast.makeText(this, "Lista e seus itens excluídos!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun abrirDialogCriarLista(lista: Lista?) {
        val isEditing = lista != null
        val titulo = if (isEditing) "Editar Lista" else "Adicionar Nova Lista"

        val dialogBinding = DialogListaCrudBinding.inflate(layoutInflater)

        tempListImageUri = lista?.imagemUri

        if (isEditing && lista != null) {
            dialogBinding.editNomeLista.setText(lista.nome)

            if (lista.imagemUri != null) {
                dialogBinding.imagePreview.setImageURI(Uri.parse(lista.imagemUri))
            }
        }

        if (tempListImageUri == null) {
            dialogBinding.imagePreview.setImageResource(R.drawable.ic_menu_gallery)
        }

        dialogBinding.buttonSelecionarImagem.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar") { _, _ ->
                salvarLista(dialogBinding, lista)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarLista(binding: DialogListaCrudBinding, listaOriginal: Lista?) {
        val nome = binding.editNomeLista.text.toString().trim()

        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome da lista é obrigatório.", Toast.LENGTH_LONG).show()
            return
        }

        if (listaOriginal != null) {
            val listaAtualizada = listaOriginal.copy(
                nome = nome,
                imagemUri = tempListImageUri
            )
            DataStore.updateLista(listaAtualizada)
            Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show()
        } else {
            val novaLista = Lista(
                id = 0L,
                usuarioId = usuarioId,
                nome = nome,
                imagemUri = tempListImageUri
            )
            DataStore.insertLista(novaLista)
            Toast.makeText(this, "Lista adicionada!", Toast.LENGTH_SHORT).show()
        }

        tempListImageUri = null
        carregarListas()
    }
}