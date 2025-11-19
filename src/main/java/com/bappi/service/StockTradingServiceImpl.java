package com.bappi.service;

import com.bappi.entity.Stock;
import com.bappi.grpc.StockRequest;
import com.bappi.grpc.StockResponse;
import com.bappi.grpc.StockTradingServiceGrpc;
import com.bappi.repository.StockRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    private final StockRepository stockRepository;

    public StockTradingServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        // Stock name -> DB -> map response -> return
        // super.getStockPrice(request, responseObserver);
        String stockSymbol = request.getStockSymbol();
        Stock stockEntity = stockRepository.findByStockSymbol(stockSymbol);
        StockResponse stockResponse = StockResponse.newBuilder().setStockSymbol(stockEntity.getStockSymbol())
                .setPrice(stockEntity.getPrice())
                .setTimestamp(stockEntity.getLastUpdated().toString())
                .build();

        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();

    }

    public void subscribeStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        try {
            for (int i=0;i<=10;i++ ) {
                StockResponse stockResponse = StockResponse.newBuilder()
                        .setStockSymbol(stockSymbol)
                        .setPrice(new Random().nextDouble(200))
                        .setTimestamp(Instant.now().toString())
                        .build();
                responseObserver.onNext(stockResponse);
                TimeUnit.SECONDS.sleep(1);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

}
