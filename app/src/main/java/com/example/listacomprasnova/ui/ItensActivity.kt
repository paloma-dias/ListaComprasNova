package com.example.listacomprasnova.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listacomprasnova.R
import com.example.listacomprasnova.data.DataStore
import com.example.listacomprasnova.databinding.ActivityItensBinding
import com.example.listacomprasnova.databinding.DialogItemCrudBinding
import com.example.listacomprasnova.model.Categoria
import com.example.listacomprasnova.model.Item
import com.example.listacomprasnova.ui.adapter.ItemAdapter

class ItensActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItensBinding
    private lateinit var adapter: ItemAdapter
    private var listaId: Long = 0L
    private var listaNome: String = "Itens"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listaId = intent.getLongExtra("lista_id", 0L)
        listaNome = intent.getStringExtra("lista_nome") ?: "Itens"

        if (listaId == 0L) {
            Toast.makeText(this, "Erro: Lista inválida.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar(listaNome)
        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        carregarItens()
    }

    private fun setupToolbar(nome: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = nome
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_itens, menu)

        val searchItem = menu?.findItem(R.id.action_search_item)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                carregarItens(query.orEmpty())
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                carregarItens(newText.orEmpty())
                return true
            }
        })

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                carregarItens()
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun carregarItens(query: String = "") {
        val todosItens = DataStore.getAllItens(listaId)
        val itensFiltrados = if (query.isEmpty()) {
            todosItens
        } else {
            todosItens.filter { it.nome.contains(query, ignoreCase = true) }
        }

        adapter.updateItens(itensFiltrados)

        val isEmpty = itensFiltrados.isEmpty()
        binding.recyclerViewItens.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter(
            itensBrutos = emptyList(),
            onToggleComprado = { itemAtualizado ->
                DataStore.updateItem(itemAtualizado)
                carregarItens()
                Toast.makeText(this, "Item marcado como ${if (itemAtualizado.comprado) "comprado" else "pendente"}", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { item ->
                abrirDialogCriarItem(item)
            },
            onDeleteClick = { item ->
                deletarItem(item)
            }
        )
        binding.recyclerViewItens.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewItens.adapter = adapter
    }

    private fun setupFab() {
        binding.fabNovoItem.setOnClickListener {
            abrirDialogCriarItem(null)
        }
    }

    private fun deletarItem(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Item")
            .setMessage("Confirmar exclusão do item \"${item.nome}\"?")
            .setPositiveButton("Sim") { _, _ ->
                DataStore.deleteItem(item.id)
                carregarItens()
                Toast.makeText(this, "Item excluído!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun abrirDialogCriarItem(item: Item?) {
        val isEditing = item != null
        val titulo = if (isEditing) "Editar Item" else "Adicionar Novo Item"

        val dialogBinding = DialogItemCrudBinding.inflate(layoutInflater)

        ArrayAdapter.createFromResource(
            this,
            R.array.unidades_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.spinnerUnidade.adapter = adapter
        }

        if (isEditing && item != null) {
            dialogBinding.editNomeItem.setText(item.nome)
            dialogBinding.editQuantidade.setText(item.quantidade.toString())

            val unidades = resources.getStringArray(R.array.unidades_array)
            val unidadeIndex = unidades.indexOf(item.unidade)
            dialogBinding.spinnerUnidade.setSelection(unidadeIndex.coerceAtLeast(0))

            // Mapeamento e seleção do RadioButton
            val categoriaMap = mapOf(
                Categoria.FRUTA to dialogBinding.radioFruta,
                Categoria.LEGUME to dialogBinding.radioLegume,
                Categoria.CARNE to dialogBinding.radioCarne,
                Categoria.PADARIA to dialogBinding.radioPadaria,
                Categoria.OUTROS to dialogBinding.radioOutros
            )
            categoriaMap[item.categoria]?.isChecked = true
        }

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar") { dialog, _ ->
                salvarItem(dialogBinding, item)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarItem(binding: DialogItemCrudBinding, itemOriginal: Item?) {
        val nome = binding.editNomeItem.text.toString().trim()
        val qtdStr = binding.editQuantidade.text.toString().trim()
        val unidade = binding.spinnerUnidade.selectedItem.toString()

        val checkedId = binding.radioGroupCategoria.checkedRadioButtonId

        if (checkedId == -1) {
            Toast.makeText(this, "Selecione uma categoria.", Toast.LENGTH_LONG).show()
            return
        }

        val radioText = binding.root.findViewById<RadioButton>(checkedId).text.toString()

        val categoria = when (radioText) {
            "Fruta" -> Categoria.FRUTA
            "Legume/Verdura" -> Categoria.LEGUME
            "Carne" -> Categoria.CARNE
            "Padaria" -> Categoria.PADARIA
            else -> Categoria.OUTROS
        }

        if (nome.isEmpty() || qtdStr.isEmpty()) {
            Toast.makeText(this, "Nome e Quantidade são obrigatórios.", Toast.LENGTH_LONG).show()
            return
        }

        val quantidade = qtdStr.toDoubleOrNull()
        if (quantidade == null || quantidade <= 0) {
            Toast.makeText(this, "Quantidade deve ser um número válido e positivo.", Toast.LENGTH_LONG).show()
            return
        }

        if (itemOriginal != null) {
            val itemAtualizado = itemOriginal.copy(
                nome = nome,
                quantidade = quantidade,
                unidade = unidade,
                categoria = categoria
            )
            DataStore.updateItem(itemAtualizado)
            Toast.makeText(this, "Item atualizado!", Toast.LENGTH_SHORT).show()
        } else {
            val novoItem = Item(
                id = 0L,
                listaId = listaId,
                nome = nome,
                quantidade = quantidade,
                unidade = unidade,
                categoria = categoria,
                comprado = false
            )
            DataStore.insertItem(novoItem)
            Toast.makeText(this, "Item adicionado!", Toast.LENGTH_SHORT).show()
        }

        carregarItens()
    }
}