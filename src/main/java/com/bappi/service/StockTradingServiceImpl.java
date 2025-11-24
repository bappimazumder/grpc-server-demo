package com.bappi.service;

import com.bappi.entity.Stock;
import com.bappi.grpc.*;
import com.bappi.repository.StockRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public StreamObserver<StockOrder> bulkStockOrder(StreamObserver<OrderSummary> responseObserver) {

        return new StreamObserver<StockOrder>() {
            private int totalOrders = 0;
            private double totalAmount = 0;
            private int successCount = 0;

            @Override
            public void onNext(StockOrder stockOrder) {
                totalOrders++;
                totalAmount += stockOrder.getPrice() * stockOrder.getQuantity();
                successCount++;
                System.out.println("Order #" + totalOrders + ": " + totalAmount + " " + successCount);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                OrderSummary summary = OrderSummary.newBuilder()
                                .setTotalOrders(totalOrders)
                        .setTotalAmount(totalAmount)
                                .setTotalAmount(totalAmount)
                                        .build();
                responseObserver.onNext(summary);
                responseObserver.onCompleted();

            }
        };
    }

    @Override
    public StreamObserver<StockOrder> liveTrading(StreamObserver<TradeStatus> responseObserver) {
        return new StreamObserver<StockOrder>() {

            @Override
            public void onNext(StockOrder stockOrder) {
                System.out.println("Received order: " + stockOrder);
                String status = "EXECUTED";
                String message = "Order placed successfully.";
                if(stockOrder.getQuantity() <= 0) {
                    status = "FAILED";
                    message = "Invalid quantity.";
                }
                TradeStatus tradeStatus = TradeStatus.newBuilder()
                        .setOrderId(stockOrder.getOrderId())
                        .setStatus(status)
                        .setMessage(message)
                        .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
                responseObserver.onNext(tradeStatus);

            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Received order: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
