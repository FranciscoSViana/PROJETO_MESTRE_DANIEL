package io.github.franciscosviana.stmservicos.common.modelmapper;

import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        var modelMapper = new ModelMapper();

        // INPUT -> DOMAIN
        modelMapper.typeMap(CredenciadoInput.class, Credenciado.class)
                .addMappings(m -> m.skip(Credenciado::setTipoPessoa));

        // DOMAIN -> OUTPUT (Enum -> String com null-safe)
        Converter<TipoPessoa, String> tipoPessoaToString =
                ctx -> ctx.getSource() == null ? null : ctx.getSource().getDescricao();

        modelMapper.typeMap(Credenciado.class, CredenciadoOutput.class)
                .addMappings(m ->
                        m.using(tipoPessoaToString)
                                .map(Credenciado::getTipoPessoa, CredenciadoOutput::setTipoPessoa)
                );

        return modelMapper;
    }
}
