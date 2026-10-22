package com.example.banking.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

//    @Bean
//    public KeyResolver ipKeyResolver() {
//        return exchange -> {
//            if (exchange.getRequest().getRemoteAddress() == null) {
//                return Mono.just("unknown-client");
//            }
//
//            String ipAddress = exchange.getRequest()
//                .getRemoteAddress()
//                .getAddress()
//                .getHostAddress();
//
//            System.out.println("Client IP: " + ipAddress);
//
//            return Mono.just(ipAddress);
//        };
//    }

    @Bean
    public KeyResolver globalKeyResolver() {
        return exchange -> Mono.just("global");
    }
}
