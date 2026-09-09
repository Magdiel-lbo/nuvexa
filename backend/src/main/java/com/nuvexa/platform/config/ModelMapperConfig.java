package com.nuvexa.platform.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    /**
     * STRICT em vez do padrão STANDARD: toda entidade que expõe {@code xxxId} num DTO de update
     * e {@code xxx} como relação (@ManyToOne) no model — padrão usado em todo {@code atualizar()}
     * desta base (Consulta.profissionalId, Prontuario.autorId, Avaliacao.avaliadorId,
     * PlanoAlimentar.autorId) — sofre com o matching "inteligente" do STANDARD: ele acha que
     * {@code profissionalId} bate com o caminho aninhado {@code profissional.id} e tenta setar o
     * id na entidade JPA já anexada, o que o Hibernate rejeita no flush
     * ("identifier ... was altered"), estourando 500 genérico. STRICT não flatten nomes para
     * caminhos aninhados — só mapeia propriedade-a-propriedade com o mesmo nome — eliminando essa
     * classe inteira de erro sem precisar de skip() por DTO.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
