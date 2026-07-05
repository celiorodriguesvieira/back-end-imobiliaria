package br.pucpr.auth.corretores

import br.pucpr.auth.avatar.AvatarService
import br.pucpr.auth.exception.BadRequestException
import br.pucpr.auth.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
open class CorretorService(
    val repository: CorretorRepository,
    val avatarService: AvatarService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    open fun insert(corretor: Corretor): Corretor {
        corretor.id = null
        if (repository.findByEmail(corretor.email) != null) {
            log.warn("Tentativa de cadastro com e-mail duplicado: {}", corretor.email)
            throw BadRequestException("Já existe um corretor com o e-mail ${corretor.email}")
        }
        val saved = repository.save(corretor)
        saved.avatar = avatarService.resolve(saved.id!!, saved.nome, saved.email)
        val result = repository.save(saved)
        log.info("Corretor criado: id={} nome={} avatar={}", result.id, result.nome, result.avatar)
        return result
    }

    open fun refreshAvatar(id: Long): Corretor {
        val corretor = findById(id)
        avatarService.remove(id)
        corretor.avatar = avatarService.resolve(id, corretor.nome, corretor.email)
        val result = repository.save(corretor)
        log.info("Avatar do corretor {} refeito: {}", id, result.avatar)
        return result
    }

    open fun findAll(): List<Corretor> = repository.findAll()

    open fun findById(id: Long): Corretor =
        repository.findByIdOrNull(id)
            ?: throw NotFoundException("Corretor $id não encontrado")

    open fun delete(id: Long) {
        val corretor = findById(id)
        if (corretor.imoveis.isNotEmpty()) {
            log.warn("Tentativa de remover corretor {} com {} imóveis vinculados", id, corretor.imoveis.size)
            throw BadRequestException("Corretor possui imóveis vinculados, desvincule antes de remover")
        }
        repository.delete(corretor)
        avatarService.remove(id)
        log.info("Corretor removido: id={}", id)
    }
}
