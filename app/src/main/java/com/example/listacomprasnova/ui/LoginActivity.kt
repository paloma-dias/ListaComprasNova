package com.example.listacomprasnova.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listacomprasnova.data.DataStore
import com.example.listacomprasnova.databinding.ActivityLoginBinding
import com.example.listacomprasnova.util.EmailValidator

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    companion object {
        const val EXTRA_USUARIO_ID = "usuario_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonAcessar.setOnClickListener {
            performLogin()
        }

        binding.buttonCriarConta.setOnClickListener {
            navigateToCadastro()
        }
    }

    private fun performLogin() {
        val email = binding.editEmail.text.toString().trim()
        val senha = binding.editSenha.text.toString()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!EmailValidator.isValid(email)) {
            Toast.makeText(this, "Email inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        val usuarioLogado = DataStore.findUser(email, senha)

        if (usuarioLogado != null) {
            Toast.makeText(this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show()

            navigateToLista(usuarioLogado.id)

        } else {
            Toast.makeText(this, "Email ou senha incorretos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLista(usuarioId: Long) {
        val intent = Intent(this, ListaActivity::class.java).apply {
            putExtra(EXTRA_USUARIO_ID, usuarioId)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToCadastro() {
        val intent = Intent(this, CadastroActivity::class.java)
        startActivity(intent)
    }
}