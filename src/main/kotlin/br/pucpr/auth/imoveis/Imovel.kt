package br.pucpr.auth.imoveis

import br.pucpr.auth.corretores.Corretor
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "imoveis")
class Imovel(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var titulo: String = "",
    var endereco: String = "",
    var bairro: String = "",
    var preco: BigDecimal = BigDecimal.ZERO,
    var quartos: Int = 0,
    var area: Double = 0.0,

    @Enumerated(EnumType.STRING)
    var tipo: TipoImovel = TipoImovel.CASA,

    var descricao: String? = null,

    @ManyToOne
    @JoinColumn(name = "corretor_id")
    var corretor: Corretor? = null
)
