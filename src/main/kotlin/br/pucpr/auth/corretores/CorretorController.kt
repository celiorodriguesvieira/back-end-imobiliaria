package br.pucpr.auth.corretores

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/corretores")
class CorretorController(val service: CorretorService) {

    @PostMapping
    fun insert(@RequestBody corretor: Corretor): ResponseEntity<Corretor> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.insert(corretor))

    @GetMapping
    fun list() = service.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) = service.findById(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
