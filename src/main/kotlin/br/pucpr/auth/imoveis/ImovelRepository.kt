package br.pucpr.auth.imoveis

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface ImovelRepository : JpaRepository<Imovel, Long> {

    @Query(
        """
        SELECT i FROM Imovel i
        WHERE (:tipo IS NULL OR i.tipo = :tipo)
          AND (:bairro IS NULL OR LOWER(i.bairro) LIKE LOWER(CONCAT('%', :bairro, '%')))
          AND (:precoMin IS NULL OR i.preco >= :precoMin)
          AND (:precoMax IS NULL OR i.preco <= :precoMax)
          AND (:corretorId IS NULL OR i.corretor.id = :corretorId)
        """
    )
    fun search(
        @Param("tipo") tipo: TipoImovel?,
        @Param("bairro") bairro: String?,
        @Param("precoMin") precoMin: BigDecimal?,
        @Param("precoMax") precoMax: BigDecimal?,
        @Param("corretorId") corretorId: Long?,
        sort: Sort
    ): List<Imovel>
}
