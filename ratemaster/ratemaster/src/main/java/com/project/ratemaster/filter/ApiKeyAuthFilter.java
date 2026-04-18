package com.project.ratemaster.filter;

import com.project.ratemaster.model.ApiClient;
import com.project.ratemaster.repository.ApiClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiClientRepository apiClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path=request.getRequestURI();

        /* Skip filter for auth endpoints (register) */
        if(path.startsWith("/auth")){
            filterChain.doFilter(request,response);
            return;
        }

        /* 1.Extract API key from header */
        String apiKey= request.getHeader("X-API-Key");

        if(apiKey==null || apiKey.isBlank()){
            sendError(response,"Missing X-API-Key Header");
            return;
        }

        /* 2.Extract prefix (first 8 chars) for DB lookup */
        if(apiKey.length()<8){
            sendError(response,"Invalid API key format");
            return;
        }
        String prefix=apiKey.substring(0,8);

        /* 3.Find matching clients by prefix */
        List<ApiClient> clientList=apiClientRepository.findByApiKeyPrefixStartingWith(prefix);
        if(clientList.isEmpty()){
            sendError(response,"Invalid API key");
            return;
        }

        /* 4.Verify full key against hash */
        ApiClient matchedClient=null;
        for(ApiClient client:clientList){
            if(passwordEncoder.matches(apiKey, client.getApiKeyHash())){
                matchedClient=client;
                break;
            }
        }

        if(matchedClient==null){
            sendError(response,"Invalid API Key");
        }

        /* 5. check if client is active */
        if(!matchedClient.isActive()){
            sendError(response,"API key is deactivated");
            return;
        }

        /* 6.Store client info in request for rate limiter to use */
        request.setAttribute("clientId",matchedClient.getId());
        request.setAttribute("clientTier",matchedClient.getTier());
        request.setAttribute("clientEmail",matchedClient.getEmail());

        log.info("API Key Validated for client : {} tier: {}",matchedClient.getEmail()
                ,matchedClient.getTier());
        filterChain.doFilter(request,response);

    }

    private void sendError(HttpServletResponse response,String message)
    throws IOException{
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{ error : "+message+" }");
    }
}
