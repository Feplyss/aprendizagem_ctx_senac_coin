package com.senac.projetoIntegrador.senaccoin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.senac.projetoIntegrador.senaccoin.exceptions.BalanceNotFoundException;
import com.senac.projetoIntegrador.senaccoin.exceptions.InsuficientBalanceException;
import com.senac.projetoIntegrador.senaccoin.exceptions.UserNotFoundException;
import com.senac.projetoIntegrador.senaccoin.request.NewTransactionRequest;
import com.senac.projetoIntegrador.senaccoin.response.BalanceResponse;
import com.senac.projetoIntegrador.senaccoin.response.NewTransactionResponse;
import com.senac.projetoIntegrador.senaccoin.response.RetrieveStatementResponse;
import com.senac.projetoIntegrador.senaccoin.response.StatementResponse;
import com.senac.projetoIntegrador.senaccoin.service.ISenacCoinService;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/senaccoin", produces = "application/json")
public class SenacCoinController {

    @Autowired
    ISenacCoinService service;

    @PostMapping
    public ResponseEntity<NewTransactionResponse> addNewMovement(
            @RequestBody(required = true) NewTransactionRequest newSenacCoinMovement)
            throws UserNotFoundException, BalanceNotFoundException, InsuficientBalanceException {

        service.addNewTRansaction(newSenacCoinMovement);
        NewTransactionResponse response = new NewTransactionResponse();
        response.setStatus("OK");

        return new ResponseEntity<NewTransactionResponse>(response, HttpStatus.OK);

    }

    @GetMapping("/statement/{id}")
    public ResponseEntity<RetrieveStatementResponse> retrieveStatementByUserId(
            @PathVariable(required = true, value = "id") String userId) throws UserNotFoundException{
        List<StatementResponse> senacCoinMovimentacaoDto = service.getSenacCoinStatement(userId).stream()
                .map(item -> new StatementResponse(
                        item.getSenacCoinMovimentacaoDate(),
                        item.getSenacCoinMovimentacaoObservacao(), 
                        item.getSenacCoinMovimentacaoValor(),
                        item.getSenacCoinMovimentacaoStatus()))
                .collect(Collectors.toList());

        RetrieveStatementResponse response = new RetrieveStatementResponse();
        response.setTransactions(senacCoinMovimentacaoDto);

        return new ResponseEntity<RetrieveStatementResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<BalanceResponse> retrieveBalance(@PathVariable(required = true, value = "id") String userId) throws UserNotFoundException{
        Long balance = service.getUserBalance(userId);

        BalanceResponse response = new BalanceResponse();
        response.setBalance(balance);

        return new ResponseEntity<BalanceResponse>(response, HttpStatus.OK);
    }
}
