package com.sparta.tigercave.security;

import com.sparta.tigercave.jwt.JwtAuthFilter;
import com.sparta.tigercave.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console())
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);    //세션을 사용하지 않는다고 설정한다.

        http.authorizeRequests()                                    //요청에 대한 사용권한 체크
                .antMatchers("/api/**").hasRole("ADMIN")
                .antMatchers("/api/**").hasRole("USER") //.antMatchers() -> 해당 URL로 요청 시 permitAll()-> 접근을 허용해준다.
                .anyRequest().permitAll()
                .and().addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
                //왼쪽은 커스텀한 필터링이 들어간다. 오른쪽에 등록한 필터전에 커스텀필터링 실행

        //궁금한 점
        //그렇다면 토큰이 발행되기 전인 회원가입, 로그인, 게시판 전체 조회 부분을 필터 타게 하지않고
        //나머지 글 작성, 글 수정, 삭제 부분을 허용해야하는거 아닌가?
        //이 부분도 각자 설계하기 나름인 부분인지..

        http.formLogin().loginPage("/api/user/login?error")
                .failureUrl("/")
                .permitAll();
        return http.build();
    }
}
