package br.com.akirodou.vanillabank.api.service;

import br.com.akirodou.vanillabank.exception.GlobalApplicationException;
import br.com.akirodou.vanillabank.model.dto.ContaCorrentPostDTO;
import br.com.akirodou.vanillabank.model.dto.ValorDTO;
import br.com.akirodou.vanillabank.model.entity.ClienteEntity;
import br.com.akirodou.vanillabank.model.entity.ContaCorrenteEntity;
import br.com.akirodou.vanillabank.model.repository.ContaCorrenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
public class ContaCorrenteService {

    public final String CONTA_CORRENTE_NUMBER_STARTS_WITH = "088011224444";

    ContaCorrenteRepository contaCorrenteRepository;
    MovimentacaoService movimentacaoService;

    @Autowired
    public ContaCorrenteService(ContaCorrenteRepository contaCorrenteRepository, MovimentacaoService movimentacaoService) {
        this.contaCorrenteRepository = contaCorrenteRepository;
        this.movimentacaoService = movimentacaoService;
    }

    public ContaCorrenteEntity save(ClienteEntity titular) {
        Random random = new Random();
        String cartaoNumero = CONTA_CORRENTE_NUMBER_STARTS_WITH +
                String.format("%04d", random.nextInt(9999));
        while (contaCorrenteRepository.existsByCartaoDeCredito(cartaoNumero)) {
            cartaoNumero = CONTA_CORRENTE_NUMBER_STARTS_WITH +
                    String.format("%04d", random.nextInt(9999));
        }
        var entity = new ContaCorrenteEntity();
        entity.setTitular(titular);
        entity.setCartaoDeCredito(cartaoNumero);
        entity.setSaldo(new BigDecimal(0));
        return contaCorrenteRepository.save(entity);
    }

    public List<ContaCorrenteEntity> findAll() {
        return contaCorrenteRepository.findAll();
    }

    public ContaCorrenteEntity findByClienteId(Long id) {
        return contaCorrenteRepository.findByTitularId(id)
                .orElseThrow(() -> new GlobalApplicationException("Conta não encontrada", HttpStatus.NOT_FOUND));
    }

    public ContaCorrenteEntity findById(Long id) {
        return contaCorrenteRepository.findById(id)
                .orElseThrow(() -> new GlobalApplicationException("Conta não encontrada", HttpStatus.NOT_FOUND));
    }

    public ContaCorrenteEntity findByCartao(String cartaoDeCredito) {
        return contaCorrenteRepository.findByCartaoDeCredito(cartaoDeCredito)
                .orElseThrow(() -> new GlobalApplicationException("Conta não encontrada", HttpStatus.NOT_FOUND));
    }

    public Boolean existsById(Long id) {
        return contaCorrenteRepository.existsById(id);
    }

    public Optional<ContaCorrenteEntity> findByCliente(ClienteEntity clienteEntity) {
        return contaCorrenteRepository.findByTitular(clienteEntity);
    }

    public String depositar(Long id, ValorDTO dto) {
        return depositar(id, dto, false);
    }

    public String depositar(Long id, ValorDTO dto, boolean isTransf) {
        if (dto.getValor().compareTo(new BigDecimal(0)) <= 0)
            throw new GlobalApplicationException("O valor de depósito deve ser maior que zero", HttpStatus.BAD_REQUEST);
        var conta = findById(id);
        conta.depositar(dto.getValor());
        contaCorrenteRepository.save(conta);
        if (isTransf)
            return String.format(Locale.US, "%.2f", conta.getSaldo());
        movimentacaoService.save(id, null, "Depósito", dto.getValor());

        return "Você depositou R$ " + dto.getValor() + " na conta com o id: " + conta.getId();
    }

    public String sacar(Long id, ValorDTO dto) {
        return sacar(id, dto, false);
    }

    public String sacar(Long id, ValorDTO dto, boolean isTransf) {
        if (dto.getValor().compareTo(new BigDecimal(0)) <= 0)
            throw new GlobalApplicationException("O valor de saque deve ser maior que zero", HttpStatus.BAD_REQUEST);
        var conta = findById(id);
        if (conta.getSaldo().compareTo(dto.getValor()) < 0)
            throw new GlobalApplicationException("Saldo insuficiente", HttpStatus.BAD_REQUEST);
        conta.sacar(dto.getValor());
        contaCorrenteRepository.save(conta);
        if (isTransf)
            return String.format(Locale.US, "%.2f", conta.getSaldo());
        movimentacaoService.save(id, null, "Saque", dto.getValor());
        return "Você sacou R$ " + dto.getValor() + " na conta com o id: " + conta.getId()
                + " e seu saldo atual é de R$ " + conta.getSaldo();
    }

    public void delete(Long id) {
        try {
            contaCorrenteRepository.deleteById(id);
        } catch (Exception e) {
            throw new GlobalApplicationException("Conta não encontrada", HttpStatus.NOT_FOUND);
        }

    }
}
