package com.book.manager.presentation.config

import com.book.manager.application.service.AuthenticationService
import com.book.manager.application.service.security.BookManagerUserDetailsService
import com.book.manager.domain.enum.RoleType
import com.book.manager.presentation.handler.BookManagerAccessDeniedHandler
import com.book.manager.presentation.handler.BookManagerAuthenticationEntryPoint
import com.book.manager.presentation.handler.BookManagerAuthenticationFailureHandler
import com.book.manager.presentation.handler.BookManagerAuthenticationSuccessHandler
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
class SecurityConfig(private val authenticationService: AuthenticationService) : WebSecurityConfigurerAdapter() {
    /**
     * 認証・認可に関する設定
     */
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            // ログインAPIに対してpermitAll()を指定し、未認証ユーザーを含む全てのアクセスを許可
            .mvcMatchers("/login").permitAll()
            // /adminから始まるAPIに対してhasAuthorityを使い管理者権限のユーザーのみアクセスを許可
            .mvcMatchers("/admin/**").hasAuthority(RoleType.ADMIN.toString())
            // anyRequest().authenticated()で上記以外のAPIは認証済みユーザーのみアクセスを許可
            .anyRequest().authenticated()
            .and()
            .csrf().disable()
            // フォームログイン(ユーザー名、パスワードでのログイン)を有効化
            .formLogin()
            // ログインAPIのパスを/loginに設定
            .loginProcessingUrl("/login")
            // ログインAPIに渡すユーザー名のパラメータ名をemailに設定
            .usernameParameter("email")
            // ログインAPIに渡すパスワードのパラメータ名をpassに設定
            .passwordParameter("pass")
            // 認証成功時に実行するハンドラーを設定
            .successHandler(BookManagerAuthenticationSuccessHandler())
            // 認証失敗時に実行するハンドラーを設定
            .failureHandler(BookManagerAuthenticationFailureHandler())
            .and()
            .exceptionHandling()
            // 未認証時のハンドラーを設定
            .authenticationEntryPoint(BookManagerAuthenticationEntryPoint())
            // 認可失敗時のハンドラーを設定
            .accessDeniedHandler(BookManagerAccessDeniedHandler())
            .and()
            .cors()
            // CORSの設定
            .configurationSource(corsConfigurationSource())
    }

    /**
     * 認証処理に関する設定
     */
    override fun configure(auth: AuthenticationManagerBuilder) {
        // 認証処理を実行するクラスの指定
        auth.userDetailsService(BookManagerUserDetailsService(authenticationService))
            // パスワード暗号化アルゴリズムの指定
            .passwordEncoder(BCryptPasswordEncoder())
    }

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL)
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL)
        corsConfiguration.addAllowedOrigin("http://localhost:8081")
        corsConfiguration.allowCredentials = true

        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)

        return corsConfigurationSource
    }
}
