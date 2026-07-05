package br.pucpr.auth.avatar

import br.pucpr.auth.storage.S3Storage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
open class AvatarService(private val storage: S3Storage) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val http = RestClient.create()

    fun keyFor(corretorId: Long) = "avatares/$corretorId.png"

    open fun resolve(corretorId: Long, nome: String, email: String): String? {
        return try {
            val bytes = fetchGravatar(email) ?: fetchUiAvatar(nome)
            storage.upload(keyFor(corretorId), bytes, "image/png")
        } catch (e: Exception) {
            log.warn("Falha ao gerar/enviar avatar do corretor {}: {}", corretorId, e.message)
            null
        }
    }

    open fun remove(corretorId: Long) {
        try {
            storage.delete(keyFor(corretorId))
        } catch (e: Exception) {
            log.warn("Falha ao remover avatar do corretor {}: {}", corretorId, e.message)
        }
    }

    private fun fetchGravatar(email: String): ByteArray? {
        val hash = sha256(email.trim().lowercase())
        val url = "https://www.gravatar.com/avatar/$hash?d=404&s=200"
        return try {
            http.get().uri(url).retrieve().body(ByteArray::class.java)
                ?.also { log.info("Gravatar encontrado para {}", email) }
        } catch (e: HttpClientErrorException.NotFound) {
            log.info("Sem Gravatar para {}, usando ui-avatars", email)
            null
        }
    }

    private fun fetchUiAvatar(nome: String): ByteArray {
        val name = URLEncoder.encode(nome.ifBlank { "?" }, StandardCharsets.UTF_8)
        val url = "https://ui-avatars.com/api/?name=$name&format=png&size=200"
        return http.get().uri(url).retrieve().body(ByteArray::class.java)
            ?: error("ui-avatars não retornou imagem")
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
