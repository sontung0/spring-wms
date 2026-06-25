package nst.wms.auth.infrastructure;

import nst.wms.auth.domain.OAuthProviderCode;
import nst.wms.auth.domain.UnknownProviderException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthProviderRegistry {

    private final Map<OAuthProviderCode, OAuthProvider> providers;

    public OAuthProviderRegistry(List<OAuthProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(OAuthProvider::getCode, Function.identity()));
    }

    public OAuthProvider resolve(String code) {
        try {
            OAuthProviderCode providerCode = OAuthProviderCode.valueOf(code.toUpperCase());
            OAuthProvider provider = providers.get(providerCode);
            if (provider == null) {
                throw new UnknownProviderException(code);
            }
            return provider;
        } catch (IllegalArgumentException e) {
            throw new UnknownProviderException(code);
        }
    }
}
