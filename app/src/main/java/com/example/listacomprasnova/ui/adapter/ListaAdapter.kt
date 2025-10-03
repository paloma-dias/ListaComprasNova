package com.example.listacomprasnova.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.listacomprasnova.databinding.ItemListaBinding
import com.example.listacomprasnova.model.Lista

class ListaAdapter(
    private var listas: List<Lista>,

    private val onItemClick: (Lista) -> Unit,
    private val onEditClick: (Lista) -> Unit,
    private val onDeleteClick: (Lista) -> Unit

) : RecyclerView.Adapter<ListaAdapter.ListaViewHolder>() {

    inner class ListaViewHolder(private val binding: ItemListaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lista: Lista) {
            binding.textTituloLista.text = lista.nome

            binding.root.setOnClickListener {
                onItemClick(lista)
            }

            binding.buttonEditar.setOnClickListener {
                onEditClick(lista)
            }

            binding.buttonExcluir.setOnClickListener {
                onDeleteClick(lista)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val binding = ItemListaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        holder.bind(listas[position])
    }

    override fun getItemCount(): Int = listas.size

    fun updateListas(newListas: List<Lista>) {
        this.listas = newListas
        notifyDataSetChanged()
    }
}