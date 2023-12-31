package com.senac.projetoIntegrador.senaccoin.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.senac.projetoIntegrador.senaccoin.dto.SenacCoinDto;
import com.senac.projetoIntegrador.senaccoin.dto.SenacCoinMovimentacaoDto;
import com.senac.projetoIntegrador.senaccoin.exceptions.BalanceNotFoundException;
import com.senac.projetoIntegrador.senaccoin.exceptions.UserNotFoundException;
import com.senac.projetoIntegrador.senaccoin.repository.ISenacCoinRepository;

@Repository
public class SenacCoinRepository implements ISenacCoinRepository {

    private JdbcTemplate dbConnection;

    @Autowired
    Queries queries;

    private class SenacCoinMapper implements RowMapper<SenacCoinDto> {

        @Override
        public SenacCoinDto mapRow(ResultSet rs, int rowNum) throws SQLException {

            SenacCoinDto senacCoinDto = new SenacCoinDto();
            senacCoinDto.setSenacCoinSaldo(rs.getLong("senac_coin_saldo"));
            return senacCoinDto;
        }
    }

    private class SenacCoinMovimentacaoMapper implements RowMapper<SenacCoinMovimentacaoDto> {

        @Override
        public SenacCoinMovimentacaoDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            SenacCoinMovimentacaoDto senacCoinMovimentacaoDto = new SenacCoinMovimentacaoDto();
                senacCoinMovimentacaoDto.setSenacCoinMovimentacaoDate(rs.getTimestamp("senac_coin_movimentacao_data"));senacCoinMovimentacaoDto.setSenacCoinMovimentacaoObservacao(rs.getString("senac_coin_movimentacao_observacao"));
                senacCoinMovimentacaoDto.setSenacCoinMovimentacaoValor(rs.getLong("senac_coin_movimentacao_valor"));
                senacCoinMovimentacaoDto.setSenacCoinMovimentacaoStatus(rs.getInt("senac_coin_movimentacao_status"));
            return senacCoinMovimentacaoDto;
        }

    }

    @Autowired
    private void dbConnection(@Qualifier("dbConnection") DataSource dbConn) {
        this.dbConnection = new JdbcTemplate(dbConn);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Integer> addMovement(SenacCoinMovimentacaoDto movement) {
        int numberOfRows = this.dbConnection.update(queries.getInsertMovimentacao(),
                new Object[] {
                        movement.getSenacCoinMovimentacaoDate(),
                        movement.getSenacCoinMovimentacaoObservacao(),
                        movement.getSenacCoinMovimentacaoValor(),
                        movement.getSenacCoinMovimentacaoStatus(),
                        movement.getSenacCoinId(),
                        movement.getUsuarioId()
                });

        return CompletableFuture.completedFuture(Integer.valueOf(numberOfRows));
    }

    @Async("asyncExecutor")
    public CompletableFuture<Integer> updateBalance(SenacCoinMovimentacaoDto movement) throws UserNotFoundException, BalanceNotFoundException{
        int numberOfRows = 0;
        try{
            numberOfRows = this.dbConnection.update(queries.getUpdateSenacCoinAmount(),
                new Object[] { movement.getSenacCoinMovimentacaoValor() * movement.getSenacCoinMovimentacaoStatus(), movement.getUsuarioId(), movement.getSenacCoinId()});
        }catch(EmptyResultDataAccessException | DataIntegrityViolationException e){
            if(e.getClass() == EmptyResultDataAccessException.class) throw new UserNotFoundException();
            if(e.getClass() == DataIntegrityViolationException.class) throw new BalanceNotFoundException();
        }

        return CompletableFuture.completedFuture(Integer.valueOf(numberOfRows));

    }

    public List<SenacCoinMovimentacaoDto> getMovementsByUserId(String userId) throws UserNotFoundException{
        List<SenacCoinMovimentacaoDto> query = dbConnection.query(queries.getGetMovementsByUserId(), new SenacCoinMovimentacaoMapper(), new Object[] { userId });
        if (query.size() == 0) {
            throw new UserNotFoundException();
        }
        return query;
    }

    public Long getBalanceByUserId(String userId) throws UserNotFoundException{
        Long query;
        try{
            query = dbConnection.queryForObject(queries.getGetBalance(), new SenacCoinMapper(), new Object[] { userId }).getSenacCoinSaldo();
        }catch(EmptyResultDataAccessException e){
            throw new UserNotFoundException();
        }
        return query;
    }

}
