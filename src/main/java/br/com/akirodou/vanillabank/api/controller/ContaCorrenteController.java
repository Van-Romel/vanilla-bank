package br.com.akirodou.vanillabank.api.controller;

import br.com.akirodou.vanillabank.api.service.ClienteService;
import br.com.akirodou.vanillabank.api.service.ContaCorrenteService;
import br.com.akirodou.vanillabank.model.dto.ContaCorrentPostDTO;
import br.com.akirodou.vanillabank.model.dto.ValorDTO;
import br.com.akirodou.vanillabank.model.entity.ContaCorrenteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController //A camada Controller é responsável pela comunicação com o client ( Postiman, Angular etc)
@RequestMapping("/conta/corrente")
public class ContaCorrenteController {

    private final ContaCorrenteService contaCorrenteService;
    private final ClienteService clienteService;

    @Autowired
    public ContaCorrenteController(ContaCorrenteService contaCorrenteService, ClienteService clienteService) {
        this.contaCorrenteService = contaCorrenteService;
        this.clienteService = clienteService;
    }

    //Sempre quando formos realizar um Post/Put enviaremos os dados via Body (Postman)
    @PostMapping
    public ResponseEntity<ContaCorrenteEntity> post(@RequestBody ContaCorrentPostDTO contaCorrentPostDTO) {
        contaCorrentPostDTO.setCpf(contaCorrentPostDTO.getCpf().replace(".", "").replace("-", ""));
        var entity = contaCorrenteService.save(clienteService.findByCpf(contaCorrentPostDTO.getCpf()));
        entity.getTitular().setCpf(entity.getTitular().getCpf().replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4"));
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @GetMapping
    public ResponseEntity<List<ContaCorrenteEntity>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(contaCorrenteService.findAll());
    }

    @GetMapping("/cliente/{cpf}")
    public ResponseEntity<ContaCorrenteEntity> findByClienteCpf(@PathVariable String cpf) {
        cpf = (cpf.replace(".", "").replace("-", ""));
        return ResponseEntity.ok(contaCorrenteService.findByClienteId(
                clienteService.findByCpf(cpf).getId()));
    }

    @GetMapping("/{id}")
    //Quando quermos passar um valor pela URI (URL), usamos a anotação @PathVariable
    public ResponseEntity<ContaCorrenteEntity> findById(@PathVariable Long id) {
        return ResponseEntity.ok(contaCorrenteService.findById(id));
//                .map(resp -> ResponseEntity.ok(resp)).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cartao-de-credito/{cartaoDeCredito}")
    public ResponseEntity<ContaCorrenteEntity> findByCartaoDeCredito(@PathVariable String cartaoDeCredito) {
        return ResponseEntity.ok(contaCorrenteService.findByCartao(cartaoDeCredito));
    }

    //o Spring considera que o retorno do método é o nome da página que ele deve carregar, mas ao utilizar a anotação @ResponseBody, indicamos que o retorno do método deve ser serializado e devolvido no corpo da resposta.
    @PutMapping("/depositar/{id}")
    public ResponseEntity<?> depositar(@PathVariable Long id, @RequestBody ValorDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(
                contaCorrenteService.depositar(id, dto));
    }

    @PutMapping("/sacar/{id}")
    public ResponseEntity<?> sacar(@PathVariable Long id, @RequestBody ValorDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(
                contaCorrenteService.sacar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        contaCorrenteService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
