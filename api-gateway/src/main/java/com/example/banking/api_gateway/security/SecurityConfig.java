package com.example.banking.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http, Converter<Jwt, Mono<AbstractAuthenticationToken>>
            jwtAuthenticationConverter) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/auth/login").permitAll()
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/accounts/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/payments/**").hasAnyRole("USER", "ADMIN")
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(
                        jwtAuthenticationConverter)))
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(
        PasswordEncoder passwordEncoder) {

        UserDetails user = User.builder()
            .username("demo")
            .password(passwordEncoder.encode("Demo@123"))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("Admin@123"))
            .roles("ADMIN")
            .build();

        return new MapReactiveUserDetailsService(user, admin);
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(
        MapReactiveUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {

        var manager =
            new UserDetailsRepositoryReactiveAuthenticationManager(
                userDetailsService);

        manager.setPasswordEncoder(passwordEncoder);

        return manager;
    }


    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtConverter =
            new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(
            authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(
            jwtConverter);
    }

}
