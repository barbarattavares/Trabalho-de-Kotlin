package com.escola.bibliotecamobile.ui.busca

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import com.escola.bibliotecamobile.R
import com.escola.bibliotecamobile.databinding.ActivityMainBinding
import com.escola.bibliotecamobile.ui.resultados.ResultadosActivity
import com.google.android.material.chip.Chip

/**
 * Tela 1 — Busca. O usuário digita um título/autor (ou toca numa sugestão),
 * o termo é validado e enviado para a tela de resultados por Intent.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBuscar.setOnClickListener { pesquisar() }

        // Botão "buscar" do teclado também dispara a pesquisa.
        binding.etTermo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                pesquisar()
                true
            } else {
                false
            }
        }

        // Remove a mensagem de erro assim que o usuário volta a digitar.
        binding.etTermo.doAfterTextChanged { binding.tilTermo.error = null }

        // Cada sugestão preenche o campo e já pesquisa.
        binding.chipGroupSugestoes.children.filterIsInstance<Chip>().forEach { chip ->
            chip.setOnClickListener {
                binding.etTermo.setText(chip.text)
                binding.etTermo.setSelection(chip.text.length)
                pesquisar()
            }
        }
    }

    private fun pesquisar() {
        val termo = binding.etTermo.text?.toString()?.trim().orEmpty()

        val erro = when {
            termo.isEmpty() -> getString(R.string.erro_termo_vazio)
            termo.length < TAMANHO_MINIMO -> getString(R.string.erro_termo_curto, TAMANHO_MINIMO)
            else -> null
        }

        if (erro != null) {
            binding.tilTermo.error = erro
            binding.etTermo.requestFocus()
            return
        }

        binding.tilTermo.error = null
        esconderTeclado()
        startActivity(ResultadosActivity.criarIntent(this, termo))
    }

    private fun esconderTeclado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etTermo.windowToken, 0)
    }

    companion object {
        private const val TAMANHO_MINIMO = 3
    }
}
