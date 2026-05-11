package br.pucpr.auth.imoveis

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/imoveis")
class ImovelController(val service: ImovelService) {

    @PostMapping
    fun insert(@RequestBody imovel: Imovel): ResponseEntity<Imovel> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.insert(imovel))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) = service.findById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody imovel: Imovel) =
        service.update(id, imovel)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun search(
        @RequestParam(required = false) tipo: TipoImovel?,
        @RequestParam(required = false) bairro: String?,
        @RequestParam(required = false) precoMin: BigDecimal?,
        @RequestParam(required = false) precoMax: BigDecimal?,
        @RequestParam(required = false) corretorId: Long?,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDir: String
    ) = service.search(tipo, bairro, precoMin, precoMax, corretorId, sortBy, sortDir)

    @PutMapping("/{id}/corretor/{corretorId}")
    fun atribuirCorretor(@PathVariable id: Long, @PathVariable corretorId: Long) =
        service.atribuirCorretor(id, corretorId)

    @DeleteMapping("/{id}/corretor")
    fun removerCorretor(@PathVariable id: Long) = service.removerCorretor(id)
}
