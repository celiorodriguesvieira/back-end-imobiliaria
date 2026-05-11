package br.pucpr.auth.corretores

import br.pucpr.auth.imoveis.Imovel
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "corretores")
class Corretor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var nome: String = "",
    var email: String = "",
    var telefone: String? = null,

    @JsonIgnore
    @OneToMany(mappedBy = "corretor")
    var imoveis: MutableList<Imovel> = mutableListOf()
)
