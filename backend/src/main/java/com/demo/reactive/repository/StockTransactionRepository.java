package com.demo.reactive.repository;

import com.demo.reactive.model.StockTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StockTransactionRepository extends ReactiveCrudRepository<StockTransaction, Long> {
    
    Flux<StockTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
}
