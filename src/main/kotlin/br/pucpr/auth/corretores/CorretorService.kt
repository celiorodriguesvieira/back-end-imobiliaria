package br.pucpr.auth.corretores

import br.pucpr.auth.exception.BadRequestException
import br.pucpr.auth.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
open class CorretorService(val repository: CorretorRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    open fun insert(corretor: Corretor): Corretor {
        if (repository.findByEmail(corretor.email) != null) {
            log.warn("Tentativa de cadastro com e-mail duplicado: {}", corretor.email)
            throw BadRequestException("Já existe um corretor com o e-mail ${corretor.email}")
        }
        val saved = repository.save(corretor)
        log.info("Corretor criado: id={} nome={}", saved.id, saved.nome)
        return saved
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
        log.info("Corretor removido: id={}", id)
    }
}
