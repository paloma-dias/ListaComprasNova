package com.example.listacomprasnova.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.listacomprasnova.databinding.ItemItemBinding
import com.example.listacomprasnova.databinding.ItemCategoriaHeaderBinding
import com.example.listacomprasnova.model.Item

sealed class ItemAdapterItem {
    data class ItemData(val item: Item) : ItemAdapterItem()
    data class Header(val categoriaNome: String) : ItemAdapterItem()
}

class ItemAdapter(
    itensBrutos: List<Item>,
    private val onToggleComprado: (Item) -> Unit,

    // NOVOS CALLBACKS:
    private val onEditClick: (Item) -> Unit,
    private val onDeleteClick: (Item) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itensAgrupados: List<ItemAdapterItem> = emptyList()

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1

    init {
        updateItens(itensBrutos)
    }
    inner class ItemViewHolder(private val binding: ItemItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.textNomeItem.text = item.nome
            binding.textQuantidade.text = "${item.quantidade} ${item.unidade}"

            binding.checkboxComprado.isChecked = item.comprado

            binding.textNomeItem.alpha = if (item.comprado) 0.5f else 1.0f

            binding.checkboxComprado.setOnClickListener {
                onToggleComprado(item.copy(comprado = binding.checkboxComprado.isChecked))
            }


            binding.buttonEditarItem.setOnClickListener {
                onEditClick(item)
            }

            binding.buttonDeletarItem.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemCategoriaHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(categoriaNome: String) {
            binding.textCategoriaNome.text = categoriaNome
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (itensAgrupados[position]) {
            is ItemAdapterItem.Header -> VIEW_TYPE_HEADER
            is ItemAdapterItem.ItemData -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemCategoriaHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = ItemItemBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("ViewType desconhecido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itensAgrupados[position]) {
            is ItemAdapterItem.Header -> (holder as HeaderViewHolder).bind(item.categoriaNome)
            is ItemAdapterItem.ItemData -> (holder as ItemViewHolder).bind(item.item)
        }
    }

    override fun getItemCount(): Int = itensAgrupados.size

    fun updateItens(itensBrutos: List<Item>) {
        val listaProcessada = mutableListOf<ItemAdapterItem>()

        val naoComprados = itensBrutos.filter { !it.comprado }
        val comprados = itensBrutos.filter { it.comprado }

        processarItens(naoComprados, listaProcessada)

        if (comprados.isNotEmpty()) {
            listaProcessada.add(ItemAdapterItem.Header("Comprados"))
            comprados.sortedBy { it.nome }.forEach {
                listaProcessada.add(ItemAdapterItem.ItemData(it))
            }
        }

        this.itensAgrupados = listaProcessada
        notifyDataSetChanged()
    }

    private fun processarItens(itens: List<Item>, listaProcessada: MutableList<ItemAdapterItem>) {
        val itensAgrupadosPorCategoria = itens
            .sortedBy { it.nome }
            .groupBy { it.categoria }
            .toSortedMap()

        itensAgrupadosPorCategoria.forEach { (categoria, itensNaCategoria) ->
            listaProcessada.add(ItemAdapterItem.Header(categoria.name.replace("_", " ").toLowerCase().capitalize()))

            itensNaCategoria.forEach {
                listaProcessada.add(ItemAdapterItem.ItemData(it))
            }
        }
    }
}