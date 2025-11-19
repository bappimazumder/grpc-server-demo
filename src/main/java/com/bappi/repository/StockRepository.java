package com.bappi.repository;

import com.bappi.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByStockSymbol(String symbol);
}
