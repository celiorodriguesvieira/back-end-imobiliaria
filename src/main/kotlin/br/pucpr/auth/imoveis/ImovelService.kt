package br.pucpr.auth.imoveis

import br.pucpr.auth.corretores.CorretorService
import br.pucpr.auth.exception.BadRequestException
import br.pucpr.auth.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
open class ImovelService(
    val repository: ImovelRepository,
    val corretorService: CorretorService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val sortableFields = setOf("id", "preco", "area", "quartos", "titulo")

    open fun insert(imovel: Imovel): Imovel {
        if (imovel.preco <= BigDecimal.ZERO) {
            throw BadRequestException("Preço deve ser maior que zero")
        }
        val saved = repository.save(imovel)
        log.info("Imóvel criado: id={} titulo={}", saved.id, saved.titulo)
        return saved
    }

    open fun findById(id: Long): Imovel =
        repository.findByIdOrNull(id)
            ?: throw NotFoundException("Imóvel $id não encontrado")

    open fun update(id: Long, dados: Imovel): Imovel {
        val imovel = findById(id)
        imovel.titulo = dados.titulo
        imovel.endereco = dados.endereco
        imovel.bairro = dados.bairro
        imovel.preco = dados.preco
        imovel.quartos = dados.quartos
        imovel.area = dados.area
        imovel.tipo = dados.tipo
        imovel.descricao = dados.descricao
        log.info("Imóvel atualizado: id={}", id)
        return repository.save(imovel)
    }

    open fun delete(id: Long) {
        val imovel = findById(id)
        repository.delete(imovel)
        log.info("Imóvel removido: id={}", id)
    }

    open fun search(
        tipo: TipoImovel?,
        bairro: String?,
        precoMin: BigDecimal?,
        precoMax: BigDecimal?,
        corretorId: Long?,
        sortBy: String,
        sortDir: String
    ): List<Imovel> {
        if (sortBy !in sortableFields) {
            throw BadRequestException("Campo de ordenação inválido: $sortBy. Use: $sortableFields")
        }
        val direction = when (sortDir.uppercase()) {
            "ASC" -> Sort.Direction.ASC
            "DESC" -> Sort.Direction.DESC
            else -> throw BadRequestException("Direção inválida: $sortDir. Use ASC ou DESC")
        }
        val sort = Sort.by(direction, sortBy)
        log.debug("Buscando imóveis: tipo={} bairro={} preco={}-{} corretor={} sort={}.{}",
            tipo, bairro, precoMin, precoMax, corretorId, sortBy, sortDir)
        return repository.search(tipo, bairro, precoMin, precoMax, corretorId, sort)
    }

    open fun atribuirCorretor(imovelId: Long, corretorId: Long): Imovel {
        val imovel = findById(imovelId)
        val corretor = corretorService.findById(corretorId)
        imovel.corretor = corretor
        log.info("Corretor {} atribuído ao imóvel {}", corretorId, imovelId)
        return repository.save(imovel)
    }

    open fun removerCorretor(imovelId: Long): Imovel {
        val imovel = findById(imovelId)
        if (imovel.corretor == null) {
            throw BadRequestException("Imóvel $imovelId não possui corretor vinculado")
        }
        log.info("Corretor removido do imóvel {}", imovelId)
        imovel.corretor = null
        return repository.save(imovel)
    }
}
