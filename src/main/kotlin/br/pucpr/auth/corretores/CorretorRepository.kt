package br.pucpr.auth.corretores

import org.springframework.data.jpa.repository.JpaRepository

interface CorretorRepository : JpaRepository<Corretor, Long> {
    fun findByEmail(email: String): Corretor?
}
