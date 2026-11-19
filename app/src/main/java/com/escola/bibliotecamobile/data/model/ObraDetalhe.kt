package com.escola.bibliotecamobile.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Representa o JSON de GET /works/{id}.json (usamos só alguns campos):
 *
 * {
 *   "title": "Dom Casmurro",
 *   "description": "texto..."   OU   { "type": "/type/text", "value": "texto..." },
 *   "subjects": ["Fiction", "Brazilian fiction", ...],
 *   "first_publish_date": "1899"
 * }
 *
 * "description" chega em dois formatos diferentes, por isso é lido como JsonElement
 * e convertido para texto em [descricaoTexto].
 */
data class ObraDetalhe(
    @SerializedName("title") val titulo: String?,
    @SerializedName("description") val descricao: JsonElement?,
    @SerializedName("subjects") val assuntos: List<String>?,
    @SerializedName("first_publish_date") val dataPrimeiraPublicacao: String?
) {
    fun descricaoTexto(): String? {
        val texto = when {
            descricao == null || descricao.isJsonNull -> null
            descricao.isJsonPrimitive -> descricao.asString
            descricao.isJsonObject -> descricao.asJsonObject.get("value")?.asString
            else -> null
        }
        // Algumas descrições terminam com uma seção de fontes separada por "----------".
        // Também removemos o negrito em markdown ("**texto**") que algumas descrições trazem.
        return texto?.substringBefore("----------")
            ?.replace("**", "")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
