package com.tradesimulator;

import com.tradesimulator.market.PriceUpdateStrategy;
import com.tradesimulator.market.RandomWalkStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Random;

@SpringBootApplication
@EnableScheduling
public class TradeSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeSimulatorApplication.class, args);
    }

    @Bean
    Random random() {
        return new Random();
    }

    @Bean
    PriceUpdateStrategy priceUpdateStrategy(Random random) {
        return new RandomWalkStrategy(random);
    }
}
