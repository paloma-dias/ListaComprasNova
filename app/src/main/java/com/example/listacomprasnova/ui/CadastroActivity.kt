package com.example.listacomprasnova.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listacomprasnova.data.DataStore
import com.example.listacomprasnova.databinding.ActivityCadastroBinding
import com.example.listacomprasnova.model.Usuario
import com.example.listacomprasnova.util.EmailValidator

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Criar Conta"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupListeners() {
        binding.btnCadastrar.setOnClickListener {
            performCadastro()
        }
        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun performCadastro() {
        val nome = binding.editNome.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val senha = binding.editSenha.text.toString()
        val confirmacao = binding.editConfirmacaoSenha.text.toString()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmacao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_LONG).show()
            return
        }

        if (!EmailValidator.isValid(email)) {
            Toast.makeText(this, "O formato do e-mail é inválido.", Toast.LENGTH_LONG).show()
            return
        }

        if (senha != confirmacao) {
            Toast.makeText(this, "A senha e a confirmação de senha não coincidem.", Toast.LENGTH_LONG).show()
            return
        }

        val novoUsuario = Usuario(
            id = 0L,
            nome = nome,
            email = email,
            senha = senha
        )

        DataStore.insertUsuario(novoUsuario)

        Toast.makeText(this, "Cadastro realizado com sucesso! Faça login.", Toast.LENGTH_LONG).show()

        finish()
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
}